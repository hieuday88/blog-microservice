# Quarkus Tech Stack

## Backend

- Java 17+
- Quarkus 3.17
- Jakarta REST / RESTEasy Reactive
- Hibernate ORM with Panache
- MySQL JDBC
- RabbitMQ với SmallRye Reactive Messaging
- Vert.x WebClient trong API Gateway
- JJWT cho JWT HMAC shared-secret
- SmallRye OpenAPI và Health

## Frontend

- HTML tĩnh
- CSS thuần
- JavaScript ES modules
- Fetch API
- JWT lưu trong `localStorage` với khóa `blog-token`

## Services

| Service | Port | Vai trò |
| --- | ---: | --- |
| API Gateway | 8080 | Định tuyến `/api/**`, CORS, kiểm tra JWT cho request ghi |
| Auth Service | 8081 | Đăng ký, đăng nhập, thông tin người dùng, phân quyền |
| Post Service | 8082 | CRUD bài viết, ảnh bài viết |
| Comment Service | 8083 | CRUD bình luận, phát event RabbitMQ |
| MySQL | 3306 | Database |
| RabbitMQ | 5672 | Message broker |

## Local Frontend Origin

Frontend nên chạy ở:

```text
http://localhost:3002
```

Gateway cũng cho phép:

```text
http://127.0.0.1:3002
```
