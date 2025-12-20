FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Lấy file jar đã build từ thư mục target
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]