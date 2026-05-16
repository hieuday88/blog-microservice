package com.blog.gateway;

import com.blog.gateway.util.JwtUtil;
import io.quarkus.vertx.web.Route;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class GatewayRoutes {
    private static final Logger LOG = Logger.getLogger(GatewayRoutes.class);
    private static final String AUTHORIZATION_PREFIX = "Bearer ";

    @Inject
    Vertx vertx;

    @Inject
    JwtUtil jwtUtil;

    @ConfigProperty(name = "services.auth.url")
    String authServiceUrl;

    @ConfigProperty(name = "services.post.url")
    String postServiceUrl;

    @ConfigProperty(name = "services.comment.url")
    String commentServiceUrl;

    WebClient client;

    @PostConstruct
    void init() {
        client = WebClient.create(vertx);
    }

    @Route(path = "/api/*", methods = {
            Route.HttpMethod.GET,
            Route.HttpMethod.POST,
            Route.HttpMethod.PUT,
            Route.HttpMethod.DELETE,
            Route.HttpMethod.OPTIONS
    })
    void proxy(RoutingContext context) {
        String path = context.normalizedPath();
        String method = context.request().method().name();

        // 1. Xử lý CORS Preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            context.response().setStatusCode(204).end();
            return;
        }

        // 2. Xác định Service đích
        String targetBase = targetBase(path);
        if (targetBase == null) {
            context.response().setStatusCode(404).end("No route for " + path);
            return;
        }

        // 3. Kiểm tra bảo mật (JWT)
        if (requiresAuthentication(path, method) && !hasValidToken(context)) {
            context.response().setStatusCode(401).end("Invalid or missing token");
            return;
        }

        // 4. Chuẩn bị Request gửi tới Microservice
        String targetUrl = targetBase + context.request().uri();
        LOG.info("Proxying " + context.request().method() + " " + context.request().path() + " -> " + targetUrl);
        HttpRequest<Buffer> request = client.requestAbs(context.request().method(), targetUrl)
                .timeout(10000); // Thêm timeout 10s để tránh treo Gateway

        copyHeaders(context.request().headers(), request);

        Buffer sendBody = context.body() != null && context.body().buffer() != null
                ? context.body().buffer()
                : Buffer.buffer();
        LOG.info("Body size: " + sendBody.length() + " bytes, Content-Type: " + context.request().getHeader("content-type"));
        request.putHeader("content-length", String.valueOf(sendBody.length()));
        request.sendBuffer(sendBody)
                .onSuccess(response -> {
                    context.response().setStatusCode(response.statusCode());
                    copyResponseHeaders(response.headers(), context);
                    Buffer responseBody = response.bodyAsBuffer();
                    if (responseBody != null) {
                        context.response().end(responseBody);
                    } else {
                        context.response().end();
                    }
                })
                .onFailure(error -> {
                    LOG.error("Proxy error to " + targetUrl + ": " + error.getMessage());
                    context.response()
                            .setStatusCode(502)
                            .end("Gateway proxy error: " + error.getMessage());
                });
    }

    private String targetBase(String path) {
        if (path.startsWith("/api/auth") || path.startsWith("/api/users")) {
            return authServiceUrl;
        }
        if (path.matches("^/api/posts/[^/]+/comments.*")) {
            return commentServiceUrl;
        }
        if (path.startsWith("/api/comments")) {
            return commentServiceUrl;
        }
        if (path.startsWith("/api/posts")) {
            return postServiceUrl;
        }
        return null;
    }

    private boolean requiresAuthentication(String path, String method) {
        if (path.startsWith("/api/posts/images")) {
            return false;
        }
        if (path.startsWith("/api/users/changePassword") || path.startsWith("/api/users/setOrUpdateInfo")) {
            return true;
        }
        return (path.startsWith("/api/posts") || path.startsWith("/api/comments"))
                && !"GET".equalsIgnoreCase(method);
    }

    private boolean hasValidToken(RoutingContext context) {
        String authorization = context.request().getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(AUTHORIZATION_PREFIX)) {
            return false;
        }
        return jwtUtil.isValid(authorization.substring(AUTHORIZATION_PREFIX.length()));
    }

    private void copyHeaders(MultiMap headers, HttpRequest<Buffer> request) {
        headers.forEach(header -> {
            String key = header.getKey();
            // Loại bỏ các header gây lỗi hoặc không cần thiết khi chuyển tiếp
            if (!isHopByHopHeader(key)
                    && !"host".equalsIgnoreCase(key)
                    && !"content-length".equalsIgnoreCase(key)
                    && !"origin".equalsIgnoreCase(key)
                    && !"access-control-request-method".equalsIgnoreCase(key)
                    && !"access-control-request-headers".equalsIgnoreCase(key)) {
                request.putHeader(key, header.getValue());
            }
        });
    }

    private void copyResponseHeaders(MultiMap headers, RoutingContext context) {
        context.response().putHeader("X-Proxied-By", "API-Gateway");
        headers.forEach(header -> {
            String key = header.getKey();
            if (!isHopByHopHeader(key) && !"content-length".equalsIgnoreCase(key)) {
                context.response().putHeader(key, header.getValue());
            }
        });
    }

    private boolean isHopByHopHeader(String key) {
        return "connection".equalsIgnoreCase(key)
                || "keep-alive".equalsIgnoreCase(key)
                || "proxy-authenticate".equalsIgnoreCase(key)
                || "proxy-authorization".equalsIgnoreCase(key)
                || "te".equalsIgnoreCase(key)
                || "trailer".equalsIgnoreCase(key)
                || "transfer-encoding".equalsIgnoreCase(key)
                || "upgrade".equalsIgnoreCase(key);
    }
}
