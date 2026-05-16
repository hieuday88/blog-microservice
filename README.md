# Hệ Thống Blog Microservices

[![Java](https://img.shields.io/badge/Java-17+-orange.svg?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.17-4695EB.svg?logo=quarkus&logoColor=white)](https://quarkus.io/)
[![Maven](https://img.shields.io/badge/Maven-Multi--Module-C71A36.svg?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?logo=docker&logoColor=white)](https://www.docker.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1.svg?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-FF6600.svg?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Frontend](https://img.shields.io/badge/Frontend-HTML%20%7C%20CSS%20%7C%20JS-F7DF1E.svg?logo=javascript&logoColor=black)](frontend/)

Hệ thống blog theo kiến trúc microservices, backend dùng Quarkus, database dùng MySQL, message broker dùng RabbitMQ, frontend dùng HTML/CSS/JavaScript thuần.

## 1. Yêu Cầu Môi Trường

Cần cài các công cụ sau:

| Công cụ | Phiên bản khuyến nghị | Dùng để |
| --- | --- | --- |
| Java JDK | 17 trở lên | Build và chạy Quarkus services |
| Maven | 3.9 trở lên | Build multi-module project |
| Python | 3.10 trở lên | Chạy static frontend server |
| Docker Desktop | Khuyến nghị | Chạy MySQL và RabbitMQ nhanh bằng Docker Compose |
| PowerShell | Windows PowerShell hoặc PowerShell 7 | Chạy các script trong `scripts/` |

Kiểm tra nhanh:

```powershell
java -version
mvn -version
python --version
docker --version
```

Nếu không dùng Docker, bạn cần tự chạy:

- MySQL ở port `3306`
- RabbitMQ ở port `5672`
- RabbitMQ Management UI ở port `15672` nếu muốn mở dashboard

## 2. Cấu Trúc Dự Án

```text
blog-microservice-main/
|-- backend/
|   |-- api-gateway/        # Gateway HTTP, định tuyến /api/**, xử lý JWT/CORS
|   |-- auth-service/       # Đăng ký, đăng nhập, người dùng, phân quyền
|   |-- post-service/       # CRUD bài viết và ảnh
|   `-- comment-service/    # CRUD bình luận, RabbitMQ event
|-- frontend/               # Giao diện HTML/CSS/JavaScript thuần
|-- scripts/                # Script chạy local
|-- docs/                   # Tài liệu kỹ thuật và kế hoạch
|-- storage/                # Dữ liệu local/legacy không thuộc source code
|-- docker-compose.yml      # MySQL, RabbitMQ và Docker build context
|-- pom.xml                 # Maven parent multi-module
`-- README.md
```

Tài liệu liên quan:

- [Tech stack](docs/QUARKUS_TECH_STACK.md)
- [Kế hoạch triển khai](docs/implementation_plan.md)
- [Cấu trúc dự án](docs/project-structure.md)

## 3. Cách Chạy Nhanh

Chạy đầy đủ từ đầu, bao gồm Docker, build và mở services:

```powershell
.\scripts\run-full.ps1
```

Chạy khi MySQL và RabbitMQ đã có sẵn, nhưng vẫn build lại Java:

```powershell
.\scripts\run-no-docker.ps1
```

Chạy nhanh từ jar đã build sẵn, bỏ qua Docker và build:

```powershell
.\scripts\run-fast.ps1
```

Chạy script chính với tùy chọn:

```powershell
.\scripts\run-all.ps1
.\scripts\run-all.ps1 -SkipDocker
.\scripts\run-all.ps1 -SkipBuild
.\scripts\run-all.ps1 -NoBrowser
.\scripts\run-all.ps1 -FrontendPort 3002
```

## 4. Địa Chỉ Truy Cập

| Thành phần | URL |
| --- | --- |
| Frontend | http://localhost:3002 |
| API Gateway | http://localhost:8080 |
| Auth Service | http://localhost:8081 |
| Post Service | http://localhost:8082 |
| Comment Service | http://localhost:8083 |
| RabbitMQ UI | http://localhost:15672 |

Tài khoản quản trị mặc định:

```text
Username: admin
Password: admin
```

## 5. Build Backend

Build toàn bộ backend từ root:

```powershell
mvn clean package -DskipTests
```

Build một service cụ thể:

```powershell
mvn -pl backend/api-gateway package -DskipTests
mvn -pl backend/auth-service package -DskipTests
mvn -pl backend/post-service package -DskipTests
mvn -pl backend/comment-service package -DskipTests
```

Output jar chạy được nằm tại:

```text
backend/<service>/target/quarkus-app/quarkus-run.jar
```

Ví dụ chạy riêng API Gateway:

```powershell
cd backend\api-gateway
java -jar target\quarkus-app\quarkus-run.jar
```

## 6. Chạy Frontend Riêng

Frontend không dùng npm, Next.js hoặc bundler.

```powershell
cd frontend
python -m http.server 3002
```

Mở:

```text
http://localhost:3002
```

Frontend mặc định gọi API Gateway tại:

```text
http://localhost:8080
```

Nếu muốn đổi API Gateway URL trong trình duyệt:

```javascript
localStorage.setItem("blog-api-base-url", "http://localhost:8080")
location.reload()
```

JWT được lưu trong localStorage với key:

```text
blog-token
```

## 7. Chạy Hạ Tầng Bằng Docker

Chạy MySQL và RabbitMQ:

```powershell
docker compose up -d mysql-db rabbitmq
```

Chạy toàn bộ container backend:

```powershell
docker compose up --build
```

Dừng container:

```powershell
docker compose down
```

Dừng và xóa volume database:

```powershell
docker compose down -v
```

## 8. Biến Môi Trường Chính

Các service có default config, nhưng có thể override bằng biến môi trường.

### Auth Service

| Biến | Mặc định |
| --- | --- |
| `AUTH_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/auth_db?...` |
| `AUTH_DATASOURCE_USERNAME` | Theo `-MysqlUsername`, mặc định `root` |
| `AUTH_DATASOURCE_PASSWORD` | Theo `-MysqlPassword`, mặc định `123456` |
| `JWT_SECRET` | Giá trị mặc định trong `application.properties` |

### Post Service

| Biến | Mặc định |
| --- | --- |
| `POST_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/post_db?...` |
| `POST_DATASOURCE_USERNAME` | Theo `-MysqlUsername`, mặc định `root` |
| `POST_DATASOURCE_PASSWORD` | Theo `-MysqlPassword`, mặc định `123456` |

### Comment Service

| Biến | Mặc định |
| --- | --- |
| `COMMENT_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/comment_db?...` |
| `COMMENT_DATASOURCE_USERNAME` | Theo `-MysqlUsername`, mặc định `root` |
| `COMMENT_DATASOURCE_PASSWORD` | Theo `-MysqlPassword`, mặc định `123456` |
| `RABBITMQ_HOST` | `localhost` |

### MySQL Khi Chạy Bằng Script

Nếu MySQL của bạn dùng username/password khác, mở file:

```powershell
scripts\run-all.ps1
```

Sửa block cấu hình ở đầu file:

```powershell
$DefaultMysqlUsername = "root"
$DefaultMysqlPassword = "123456"
```

Sau đó chỉ cần chạy:

```powershell
.\scripts\run-full.ps1
```

hoặc:

```powershell
.\scripts\run-fast.ps1
```

Script vẫn hỗ trợ truyền tham số nếu cần ghi đè tạm thời:

```powershell
.\scripts\run-all.ps1 -MysqlUsername root -MysqlPassword 123456
```

Nếu dùng Docker Compose, password này cũng được truyền thành `MYSQL_ROOT_PASSWORD`.

### API Gateway

| Biến | Mặc định |
| --- | --- |
| `AUTH_SERVICE_URL` | `http://127.0.0.1:8081` |
| `POST_SERVICE_URL` | `http://127.0.0.1:8082` |
| `COMMENT_SERVICE_URL` | `http://127.0.0.1:8083` |
| `QUARKUS_HTTP_CORS_ORIGINS` | Cho phép `localhost:3002` và `127.0.0.1:3002` |

## 9. API Chính

Tất cả request frontend đi qua API Gateway:

```text
http://localhost:8080
```

### Auth

```http
POST /api/auth/register
POST /api/auth/login
```

Ví dụ login:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin"}'
```

### User

```http
GET /api/users/me
GET /api/users
PUT /api/users/{username}/giveAdmin
PUT /api/users/{username}/takeAdmin
DELETE /api/users/{username}
```

### Post

```http
GET /api/posts?page=0&size=9&sortBy=id&sortDir=desc
GET /api/posts/{id}
POST /api/posts
PUT /api/posts/{id}
DELETE /api/posts/{id}
GET /api/posts/images/{filename}
```

Tạo bài viết dùng JSON. Nếu có ảnh, frontend gửi:

```json
{
  "title": "Tiêu đề",
  "description": "Mô tả",
  "content": "Nội dung",
  "authorId": 1,
  "authorUsername": "admin",
  "imageBase64": "data:image/png;base64,...",
  "imageFileName": "image.png"
}
```

### Comment

```http
GET /api/posts/{postId}/comments
POST /api/posts/{postId}/comments
PUT /api/posts/{postId}/comments/{commentId}
DELETE /api/posts/{postId}/comments/{commentId}
```

## 10. Luồng Sử Dụng Cơ Bản

1. Chạy `.\scripts\run-full.ps1`.
2. Mở `http://localhost:3002`.
3. Đăng nhập bằng `admin` / `admin`.
4. Tạo bài viết mới.
5. Mở chi tiết bài viết và thêm bình luận.
6. Vào trang quản trị để quản lý người dùng, bài viết và bình luận.

## 11. Lỗi Thường Gặp

### Maven không xóa được `quarkus-run.jar`

Nguyên nhân: service Java đang chạy và khóa file trong `target`.

Cách xử lý:

```powershell
Get-NetTCPConnection -LocalPort 8080,8081,8082,8083
Stop-Process -Id <PID> -Force
mvn clean package -DskipTests
```

### Frontend gọi API bị `403 Forbidden`

Kiểm tra frontend đang chạy bằng origin nào:

- Đúng: `http://localhost:3002`
- Cũng được hỗ trợ: `http://127.0.0.1:3002`

Nếu vẫn lỗi, restart API Gateway để nhận cấu hình CORS mới.

### API Gateway không gọi được service

Kiểm tra các service backend đã chạy chưa:

```powershell
Get-NetTCPConnection -LocalPort 8080,8081,8082,8083
```

### Docker báo không đọc được config

Nếu thấy cảnh báo:

```text
Error loading config file: open C:\Users\<user>\.docker\config.json: Access is denied
```

Đây là lỗi quyền Docker config trên máy local. Compose config của dự án vẫn có thể hợp lệ, nhưng Docker Desktop/config người dùng cần được sửa quyền nếu lệnh Docker thật sự thất bại.

## 12. Ghi Chú Phát Triển

- Không sửa trực tiếp file trong `target/`.
- Không cần `npm install` vì frontend không dùng Node package.
- Khi sửa backend config, cần rebuild hoặc restart service tương ứng.
- Khi sửa CSS/JS frontend, hard refresh trình duyệt bằng `Ctrl + F5` nếu thấy giao diện vẫn cũ.
- Root repo chỉ giữ entrypoint cấp dự án; code nằm trong `backend/` và `frontend/`.
