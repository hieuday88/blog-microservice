# 🚀 Hệ Thống Blog Microservices

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?logo=docker)](https://www.docker.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1.svg?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-FF6600.svg?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📖 Giới Thiệu (Introduction)

Đây là hệ thống quản lý Blog được xây dựng dựa trên kiến trúc **Microservices**. Dự án minh họa cách phân tách một ứng dụng lớn thành các dịch vụ nhỏ độc lập, giao tiếp với nhau qua **RESTful API** và **Message Queue**, đảm bảo khả năng **mở rộng** (Scalability) và **bảo trì** (Maintainability).

### ✨ Các Tính Năng Chính

* 🔐 **Authentication:** Đăng ký, Đăng nhập, cấp phát và xác thực Token (JWT)
* 📝 **Post Management:** Thêm, Sửa, Xóa bài viết, Upload hình ảnh, Phân trang bài viết
* 💬 **Comment System:** Bình luận bài viết, xử lý bất đồng bộ (Asynchronous) thông qua **RabbitMQ**
* 🌐 **API Gateway:** Định tuyến tập trung, bảo mật và cấu hình CORS
* 📚 **Documentation:** Tích hợp Swagger UI (OpenAPI) cho từng dịch vụ
* 🔒 **Security:** Spring Security với JWT Authentication
* 🗄️ **Database per Service:** Mỗi service có database riêng biệt
* 📦 **Containerization:** Docker & Docker Compose support
* ☸️ **Kubernetes Ready:** Sẵn sàng deploy lên K8s

---

## 🏗️ Kiến Trúc Hệ Thống (Architecture)

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────┐
│         API Gateway (8080)              │
│     Spring Cloud Gateway                │
└────┬────────────┬────────────┬──────────┘
     │            │            │
     ▼            ▼            ▼
┌─────────┐  ┌─────────┐  ┌──────────┐
│  Auth   │  │  Post   │  │ Comment  │
│ Service │  │ Service │  │ Service  │
│  8081   │  │  8082   │  │  8083    │
└────┬────┘  └────┬────┘  └────┬─────┘
     │            │            │
     ▼            ▼            ▼
┌─────────┐  ┌─────────┐  ┌─────────┐
│ auth_db │  │ post_db │  │comment_db│
└─────────┘  └─────────┘  └─────────┘
                               │
                               ▼
                          ┌──────────┐
                          │ RabbitMQ │
                          │   5672   │
                          └──────────┘
```

### 📦 Các Service Chính

| Service | Port | Mô Tả | Công Nghệ Chính |
|:--------|:-----|:------|:----------------|
| 🌐 **API Gateway** | `8080` | Cổng truy cập duy nhất, điều hướng request | Spring Cloud Gateway |
| 🔐 **Auth Service** | `8081` | Quản lý User, Login, Register, JWT | Spring Security, JJWT |
| 📝 **Post Service** | `8082` | Quản lý bài viết (CRUD), Upload ảnh | JPA, MultipartFile |
| 💬 **Comment Service** | `8083` | Quản lý bình luận, tích hợp RabbitMQ | RabbitMQ (Producer/Consumer) |
| 🗄️ **MySQL Database** | `3306` | Lưu trữ dữ liệu | MySQL 8.0 |
| 📨 **Message Broker** | `5672` | Hàng đợi tin nhắn | RabbitMQ |

---

## 🛠️ Yêu Cầu Cài Đặt (Prerequisites)

Để chạy dự án này, máy tính của bạn cần cài đặt:

- ☕ **Java JDK 17** hoặc mới hơn
- 📦 **Maven 3.8+**
- 🐳 **Docker Desktop** (Khuyên dùng để chạy MySQL và RabbitMQ)
- 💻 **IntelliJ IDEA** hoặc IDE ưa thích
- 🔧 **Git** để clone repository

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy (Installation & Running)

### 📥 Bước 1: Clone Repository

```bash
git clone https://github.com/hieuday88/blog-microservice.git
cd blog-microservice
```

### 🐳 Cách 1: Chạy bằng Docker Compose (⭐ Khuyên Dùng - Nhanh Nhất)

#### 1️⃣ Build toàn bộ project:
```bash
mvn clean install -DskipTests
```

#### 2️⃣ Khởi động hệ thống:
```bash
docker-compose up -d --build
```

#### 3️⃣ Kiểm tra trạng thái:
```bash
docker-compose ps
```

#### 4️⃣ Xem logs (nếu cần):
```bash
docker-compose logs -f
```

#### 5️⃣ Dừng hệ thống:
```bash
docker-compose down
```

### 🔧 Cách 2: Chạy Thủ Công (Manual)

#### 1️⃣ Khởi động MySQL và RabbitMQ bằng Docker:

```bash
# MySQL
docker run -d \
  --name blog-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  mysql:8.0

# RabbitMQ
docker run -d \
  --name blog-rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

#### 2️⃣ Tạo các Database:

```sql
CREATE DATABASE auth_db;
CREATE DATABASE post_db;
CREATE DATABASE comment_db;
```

#### 3️⃣ Build project:

```bash
mvn clean install -DskipTests
```

#### 4️⃣ Chạy từng service (mở terminal riêng cho mỗi service):

```bash
# Terminal 1 - Auth Service
cd auth-service
mvn spring-boot:run

# Terminal 2 - Post Service
cd post-service
mvn spring-boot:run

# Terminal 3 - Comment Service
cd comment-service
mvn spring-boot:run

# Terminal 4 - API Gateway
cd api-gateway
mvn spring-boot:run
```

### ☸️ Cách 3: Deploy lên Kubernetes

```bash
kubectl apply -f k8s/
```

---

## 📚 Tài Liệu API (Swagger UI)

Sau khi hệ thống khởi động thành công, bạn có thể truy cập tài liệu API tại:

| Service | Swagger UI |
|:--------|:-----------|
| 🔐 Auth Service | [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) |
| 📝 Post Service | [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) |
| 💬 Comment Service | [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html) |

> 💡 **Lưu ý:** Để test các API bảo mật (có hình ổ khóa 🔒), bạn cần:
> 1. Gọi API `/api/auth/login` để lấy Token
> 2. Bấm nút **Authorize** 🔓 trên Swagger và nhập: `Bearer <your_token>`

---

## 🔌 API Endpoints

### 🔐 Authentication (Auth Service)

| Method | Endpoint | Mô Tả | Auth Required |
|:-------|:---------|:------|:--------------|
| 📝 POST | `/api/auth/register` | Đăng ký tài khoản mới | ❌ |
| 🔑 POST | `/api/auth/login` | Đăng nhập và lấy JWT token | ❌ |

### 👤 Users (Auth Service)

| Method | Endpoint | Mô Tả | Auth Required |
|:-------|:---------|:------|:--------------|
| 👁️ GET | `/api/users/me` | Lấy thông tin user hiện tại | ✅ |
| 👤 GET | `/api/users/{username}/profile` | Xem profile của user khác | ❌ |
| ✔️ GET | `/api/users/checkUsernameAvailability` | Kiểm tra username có khả dụng | ❌ |
| ✏️ PUT | `/api/users/setOrUpdateInfo` | Cập nhật thông tin cá nhân | ✅ |
| 👑 PUT | `/api/users/{username}/giveAdmin` | Cấp quyền Admin | ✅ Admin |
| 👤 PUT | `/api/users/{username}/takeAdmin` | Thu hồi quyền Admin | ✅ Admin |
| 🗑️ DELETE | `/api/users/{username}` | Xóa user | ✅ |

### 📝 Posts (Post Service)

| Method | Endpoint | Mô Tả | Auth Required |
|:-------|:---------|:------|:--------------|
| 📋 GET | `/api/posts` | Lấy tất cả bài viết (có phân trang) | ❌ |
| 📄 GET | `/api/posts/{id}` | Lấy chi tiết 1 bài viết | ❌ |
| 👤 GET | `/api/posts/user/{userId}` | Lấy bài viết của 1 user | ❌ |
| ➕ POST | `/api/posts` | Tạo bài viết mới (có upload ảnh) | ✅ |
| ✏️ PUT | `/api/posts/{id}` | Cập nhật bài viết | ✅ |
| 🗑️ DELETE | `/api/posts/{id}` | Xóa bài viết | ✅ |

**Query Parameters cho phân trang:**
- `page`: Số trang (mặc định: 0)
- `size`: Số bài viết mỗi trang (mặc định: 5)
- `sortBy`: Trường sắp xếp (mặc định: id)
- `sortDir`: Hướng sắp xếp (asc/desc, mặc định: asc)

**Ví dụ:** `/api/posts?page=0&size=10&sortBy=id&sortDir=desc`

### 💬 Comments (Comment Service)

| Method | Endpoint | Mô Tả | Auth Required |
|:-------|:---------|:------|:--------------|
| 📋 GET | `/api/posts/{postId}/comments` | Lấy tất cả comment của bài viết | ❌ |
| 📄 GET | `/api/posts/{postId}/comments/{commentId}` | Lấy chi tiết 1 comment | ❌ |
| ➕ POST | `/api/posts/{postId}/comments` | Tạo comment (gửi notification qua RabbitMQ) | ✅ |
| ✏️ PUT | `/api/posts/{postId}/comments/{commentId}` | Cập nhật comment | ✅ |
| 🗑️ DELETE | `/api/posts/{postId}/comments/{commentId}` | Xóa comment | ✅ |

---

## 📝 Ví Dụ Request/Response

### 1️⃣ Đăng Ký Tài Khoản

**Request:**
```bash
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "name": "Nguyễn Văn A",
  "username": "nguyenvana",
  "email": "nguyenvana@example.com",
  "password": "matkhau123"
}
```

**Response:**
```
Đăng ký thành công!
```

### 2️⃣ Đăng Nhập

**Request:**
```bash
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "nguyenvana",
  "password": "matkhau123"
}
```

**Response:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW52YW5hIiwiaWF0IjoxNjg5...
```

### 3️⃣ Tạo Bài Viết (Multipart Form-Data)

**Request:**
```bash
POST /api/posts
Authorization: Bearer {your-jwt-token}
Content-Type: multipart/form-data
```

**Form Data:**
- `title`: "Hướng dẫn xây dựng Microservices với Spring Boot"
- `content`: "Nội dung chi tiết về cách xây dựng hệ thống microservices..."
- `description`: "Bài viết hướng dẫn chi tiết"
- `authorId`: 1
- `image`: [file ảnh]

**Response:**
```json
{
  "id": 1,
  "title": "Hướng dẫn xây dựng Microservices với Spring Boot",
  "content": "Nội dung chi tiết về cách xây dựng hệ thống microservices...",
  "description": "Bài viết hướng dẫn chi tiết",
  "authorId": 1,
  "imageName": "a1b2c3d4-e5f6-7890-abcd-ef1234567890_image.jpg"
}
```

### 4️⃣ Tạo Comment

**Request:**
```bash
POST /api/posts/1/comments
Content-Type: application/json
```

```json
{
  "content": "Bài viết rất hay và bổ ích!",
  "userId": 2
}
```

**Response:**
```json
{
  "id": 1,
  "content": "Bài viết rất hay và bổ ích!",
  "postId": 1,
  "userId": 2
}
```

> 📨 **Lưu ý:** Khi tạo comment, hệ thống tự động gửi event qua RabbitMQ để thông báo!

---

## 🧪 Kịch Bản Kiểm Thử (Testing Flow)

### 🎯 Luồng Test Cơ Bản

1. **🔐 Đăng ký/Đăng nhập:**
   - Tạo tài khoản mới qua `/api/auth/register`
   - Login qua `/api/auth/login` để lấy JWT Token

2. **📝 Tạo bài viết:**
   - Dùng Token gọi API `POST /api/posts` (có kèm file ảnh)
   - Kiểm tra ảnh đã được lưu trong thư mục `uploads/`

3. **💬 Bình luận:**
   - Gọi API `POST /api/posts/{id}/comments`
   - Kiểm tra comment đã được lưu vào database

4. **📨 Kiểm tra RabbitMQ:**
   - Truy cập RabbitMQ Management: [http://localhost:15672](http://localhost:15672)
   - Login: `guest` / `guest`
   - Vào tab **Queues** → `notification_queue`
   - Quan sát biểu đồ message khi có bình luận mới

### 🔍 Kiểm Tra Health Check

```bash
# Kiểm tra Auth Service
curl http://localhost:8081/actuator/health

# Kiểm tra Post Service
curl http://localhost:8082/actuator/health

# Kiểm tra Comment Service
curl http://localhost:8083/actuator/health
```

---

## 📁 Cấu Trúc Dự Án (Project Structure)

```
blog-microservice/
├── 📁 api-gateway/              # API Gateway service
│   ├── src/
│   └── pom.xml
├── 📁 auth-service/             # Authentication & Authorization
│   ├── src/
│   │   └── main/
│   │       ├── java/com/blog/auth/
│   │       │   ├── controller/  # AuthController, UserController
│   │       │   ├── entity/      # User, Role
│   │       │   ├── security/    # JWT, SecurityConfig
│   │       │   └── repository/  # UserRepository, RoleRepository
│   │       └── resources/
│   │           └── application.properties
│   └── pom.xml
├── 📁 post-service/             # Post Management
│   ├── src/
│   │   └── main/
│   │       ├── java/com/blog/post/
│   │       │   ├── controller/  # PostController
│   │       │   ├── entity/      # Post
│   │       │   └── repository/  # PostRepository
│   │       └── resources/
│   └── pom.xml
├── 📁 comment-service/          # Comment Management
│   ├── src/
│   │   └── main/
│   │       ├── java/com/blog/comment/
│   │       │   ├── controller/  # CommentController
│   │       │   ├── entity/      # Comment
│   │       │   ├── config/      # RabbitMQConfig
│   │       │   ├── consumer/    # NotificationConsumer
│   │       │   └── repository/  # CommentRepository
│   │       └── resources/
│   └── pom.xml
├── 📁 k8s/                      # Kubernetes deployment files
├── 📁 uploads/                  # Thư mục lưu ảnh upload
├── 🐳 docker-compose.yml        # Docker Compose configuration
├── 📄 pom.xml                   # Maven parent POM
└── 📖 README.md                 # Tài liệu này
```

---

## ⚙️ Cấu Hình (Configuration)

### 🔐 JWT Configuration

File: `auth-service/src/main/resources/application.properties`

```properties
# JWT Secret Key
app.jwt-secret=DayLaKhoaBiMatCuaNhom8RatDaiVaKhoDoanDeBaoMatToken1234567890

# JWT Expiration (7 ngày = 604800000 milliseconds)
app.jwt-expiration-milliseconds=604800000
```

### 📨 RabbitMQ Configuration

File: `comment-service/src/main/resources/application.properties`

```properties
# RabbitMQ Connection
spring.rabbitmq.host=host.docker.internal
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Queue Configuration
rabbitmq.queue.name=notification_queue
rabbitmq.exchange.name=notification_exchange
rabbitmq.routing.key=notification_routing_key
```

### 🗄️ Database Configuration

**Auth Service:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/auth_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
```

**Post Service:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/post_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
```

**Comment Service:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/comment_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
```

---

## 🔧 Công Nghệ Sử Dụng (Tech Stack)

### Backend Framework
- ☕ **Java 17**
- 🍃 **Spring Boot 3.5.8**
- ☁️ **Spring Cloud 2025.0.0**
- 🔒 **Spring Security**
- 📊 **Spring Data JPA**
- 🌐 **Spring Cloud Gateway**

### Database & Messaging
- 🗄️ **MySQL 8.0**
- 📨 **RabbitMQ**
- 🔄 **Hibernate ORM**

### Security & Authentication
- 🔐 **JWT (JSON Web Token)**
- 🔑 **JJWT 0.11.5**
- 🛡️ **Spring Security**

### Documentation & Tools
- 📚 **SpringDoc OpenAPI 2.5.0**
- 🐳 **Docker & Docker Compose**
- ☸️ **Kubernetes**
- 🎯 **Lombok**
- 📦 **Maven**

---

## 🌟 Tính Năng Nổi Bật (Key Features)

| Tính Năng | Mô Tả |
|:----------|:------|
| ✅ Microservices Architecture | Tách biệt các service độc lập, dễ mở rộng |
| ✅ JWT Authentication | Xác thực bảo mật với JSON Web Token |
| ✅ Role-based Authorization | Phân quyền USER và ADMIN |
| ✅ RESTful API Design | Thiết kế API chuẩn REST |
| ✅ Message Queue | RabbitMQ cho giao tiếp bất đồng bộ |
| ✅ File Upload | Upload và lưu trữ ảnh cho bài viết |
| ✅ Pagination & Sorting | Phân trang và sắp xếp dữ liệu |
| ✅ Docker Support | Container hóa toàn bộ ứng dụng |
| ✅ Kubernetes Ready | Sẵn sàng deploy lên K8s |
| ✅ API Gateway Pattern | Cổng vào tập trung |
| ✅ Database per Service | Mỗi service có database riêng |
| ✅ OpenAPI/Swagger | Tài liệu API tự động |
| ✅ Exception Handling | Xử lý lỗi tập trung |
| ✅ Input Validation | Kiểm tra dữ liệu đầu vào |

---

## 🚨 Lưu Ý Quan Trọng (Important Notes)

> ⚠️ **Môi Trường Development:** Dự án này được thiết kế cho mục đích học tập và development.

> 🔒 **Security:** Trong môi trường production, cần:
> - Thay đổi JWT secret key
> - Sử dụng HTTPS/SSL
> - Cấu hình CORS chặt chẽ hơn
> - Thêm rate limiting
> - Sử dụng secrets management (Vault, AWS Secrets Manager)

> 📊 **Production Ready Features cần bổ sung:**
> - Service Discovery (Eureka, Consul)
> - Config Server (Spring Cloud Config)
> - Circuit Breaker (Resilience4j)
> - Distributed Tracing (Zipkin, Sleuth)
> - Centralized Logging (ELK Stack)
> - Monitoring (Prometheus, Grafana)
> - Database Migration (Flyway, Liquibase)

---

## 🐛 Troubleshooting

### ❌ Lỗi kết nối MySQL

**Triệu chứng:** `Communications link failure`

**Giải pháp:**
```bash
# Kiểm tra MySQL đang chạy
docker ps | grep mysql

# Restart MySQL container
docker restart blog-mysql
```

### ❌ Lỗi RabbitMQ connection refused

**Triệu chứng:** `Connection refused: connect`

**Giải pháp:**
```bash
# Kiểm tra RabbitMQ
docker ps | grep rabbitmq

# Restart RabbitMQ
docker restart blog-rabbitmq
```

### ❌ Port đã được sử dụng

**Triệu chứng:** `Port 8080 is already in use`

**Giải pháp:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

---

## 🤝 Đóng Góp (Contributing)

Mọi đóng góp để cải thiện dự án đều được hoan nghênh! 

### 📋 Quy Trình Đóng Góp

1. 🍴 Fork repository
2. 🌿 Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. 💾 Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. 📤 Push to branch (`git push origin feature/AmazingFeature`)
5. 🔀 Tạo Pull Request

### 🐛 Báo Lỗi

Nếu phát hiện lỗi, vui lòng tạo Issue với thông tin:
- Mô tả lỗi chi tiết
- Các bước tái hiện lỗi
- Screenshots (nếu có)
- Môi trường (OS, Java version, Docker version)

---

## 📄 Giấy Phép (License)

Dự án này được phát hành dưới giấy phép **MIT License**.

```
MIT License

Copyright (c) 2025 Blog Microservices Project

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🙏 Cảm Ơn (Acknowledgments)

- Spring Boot Team
- Spring Cloud Team
- RabbitMQ Community
- Docker Community
- Tất cả contributors đã đóng góp cho dự án

---

## 📊 Thống Kê Dự Án (Project Stats)

![GitHub repo size](https://img.shields.io/github/repo-size/hieuday88/blog-microservice)
![GitHub contributors](https://img.shields.io/github/contributors/hieuday88/blog-microservice)
![GitHub stars](https://img.shields.io/github/stars/hieuday88/blog-microservice?style=social)
![GitHub forks](https://img.shields.io/github/forks/hieuday88/blog-microservice?style=social)

---

<div align="center">

### ⭐ Nếu thấy dự án hữu ích, hãy cho một Star nhé! ⭐

**Made with ❤️ by hieuday88 and gglagVN**

*© 2025 - Blog Microservices Project*

</div>
