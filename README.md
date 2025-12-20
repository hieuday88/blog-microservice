# 🚀 Blog Microservices System

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-orange.svg)](https://www.rabbitmq.com/)

## 📖 Giới thiệu (Introduction)

Đây là hệ thống quản lý Blog được xây dựng dựa trên kiến trúc **Microservices**. Dự án minh họa cách phân tách một ứng dụng lớn thành các dịch vụ nhỏ độc lập, giao tiếp với nhau qua RESTful API và Message Queue, đảm bảo khả năng mở rộng (Scalability) và bảo trì (Maintainability).

### ✨ Các tính năng chính
* **Authentication:** Đăng ký, Đăng nhập, cấp phát và xác thực Token (JWT).
* **Post Management:** Thêm, Sửa, Xóa bài viết, Upload hình ảnh, Phân trang bài viết.
* **Comment System:** Bình luận bài viết, xử lý bất đồng bộ (Asynchronous) thông qua **RabbitMQ**.
* **API Gateway:** Định tuyến tập trung, bảo mật và cấu hình CORS.
* **Documentation:** Tích hợp Swagger UI (OpenAPI) cho từng dịch vụ.

---

## 🏗️ Kiến trúc hệ thống (Architecture)

Hệ thống bao gồm các module chính sau:

| Service Name | Port | Mô tả | Công nghệ chính |
| :--- | :--- | :--- | :--- |
| **API Gateway** | `8080` | Cổng truy cập duy nhất, điều hướng request | Spring Cloud Gateway |
| **Auth Service** | `8081` | Quản lý User, Login, Register, JWT | Spring Security, JJWT |
| **Post Service** | `8082` | Quản lý bài viết (CRUD), Upload ảnh | JPA, MultipartFile |
| **Comment Service**| `8083` | Quản lý bình luận, tích hợp RabbitMQ | RabbitMQ (Producer/Consumer) |
| **Database** | `3306` | Lưu trữ dữ liệu | MySQL |
| **Message Broker** | `5672` | Hàng đợi tin nhắn | RabbitMQ |

---

## 🛠️ Yêu cầu cài đặt (Prerequisites)

Để chạy dự án này, máy tính của bạn cần cài đặt:
1.  **Java JDK 17** hoặc mới hơn.
2.  **Maven** 3.8+.
3.  **Docker Desktop** (Khuyên dùng để chạy MySQL và RabbitMQ).
4.  **IntelliJ IDEA** (hoặc IDE ưa thích).

---

## 🚀 Hướng dẫn chạy (Installation & Running)

Có 2 cách để chạy dự án:

### Cách 1: Chạy bằng Docker Compose (Khuyên dùng - Nhanh nhất)

1.  **Build toàn bộ project:**
    Tại thư mục gốc, mở Terminal và chạy lệnh:
    ```bash
    mvn clean install -DskipTests
    ```

2.  **Khởi động hệ thống:**
    ```bash
    docker-compose up -d --build
    ```

3.  **Kiểm tra:**
    Các container sẽ tự động khởi chạy. Truy cập `http://localhost:8080` để test.

### Cách 2: Chạy thủ công (Manual)

Nếu không dùng Docker Compose cho các Service Java, bạn cần:
1.  Khởi động MySQL và RabbitMQ (có thể dùng Docker lẻ hoặc cài trên máy).
2.  Tạo Database tên `blog_db` (hoặc tên trong file config).
3.  Cập nhật cấu hình `application.yml` trong từng service để trỏ đúng vào MySQL/RabbitMQ.
4.  Chạy lần lượt các file `Application.java` theo thứ tự:
    * `AuthService`
    * `PostService`
    * `CommentService`
    * `ApiGateway`

---

## 📚 Tài liệu API (Swagger UI)

Sau khi hệ thống khởi động thành công, bạn có thể truy cập tài liệu API tại các đường dẫn sau:

* **Auth Service:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
* **Post Service:** [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
* **Comment Service:** [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)

> **Lưu ý:** Để test các API bảo mật (có hình ổ khóa), bạn cần:
> 1. Gọi API `/api/auth/login` để lấy Token.
> 2. Bấm nút **Authorize** trên Swagger và nhập: `Bearer <your_token>`.

---

## 🧪 Kịch bản kiểm thử (Testing Flow)

1.  **Đăng ký/Đăng nhập:** Tạo tài khoản mới -> Login lấy Token.
2.  **Tạo bài viết:** Dùng Token gọi API `POST /api/posts` (có kèm file ảnh).
3.  **Bình luận:** Gọi API `POST /api/posts/{id}/comments`.
4.  **Kiểm tra RabbitMQ:**
    * Truy cập: [http://localhost:15672](http://localhost:15672) (User/Pass: guest/guest).
    * Vào tab **Queues**, quan sát biểu đồ khi có bình luận mới.

---

## 🤝 Đóng góp (Contributing)

Mọi đóng góp để cải thiện dự án đều được hoan nghênh. Vui lòng tạo Pull Request hoặc mở Issue nếu phát hiện lỗi.

## 📄 License

Dự án này được thực hiện cho mục đích học tập.

---
*© 2025 - Blog Microservices Project*
