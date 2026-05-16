# Implementation Plan

Mục tiêu hiện tại là giữ repo theo cấu trúc monorepo rõ ràng:

- `backend/`: toàn bộ Quarkus services.
- `frontend/`: giao diện tĩnh, không dùng Next.js hoặc npm.
- `scripts/`: lệnh chạy local.
- `docs/`: tài liệu kỹ thuật và kế hoạch.

## Backend

Các service được quản lý bởi Maven parent ở root:

```text
backend/api-gateway
backend/auth-service
backend/post-service
backend/comment-service
```

Parent `pom.xml` khai báo các module này trực tiếp để `mvn clean package -DskipTests` vẫn build toàn bộ hệ thống.

## Frontend

Frontend là static app:

```text
frontend/
|-- index.html
|-- css/
|-- js/
|-- auth/
|-- posts/
`-- admin/
```

Frontend gọi API Gateway tại `http://localhost:8080`.

## Scripts

Các script chạy local nằm trong `scripts/`:

```powershell
.\scripts\run-full.ps1
.\scripts\run-no-docker.ps1
.\scripts\run-fast.ps1
```

## Verification

1. Chạy `mvn clean package -DskipTests`.
2. Chạy `.\scripts\run-fast.ps1` khi jar đã có sẵn.
3. Mở `http://localhost:3002`.
4. Kiểm tra đăng nhập, danh sách bài viết, tạo/sửa bài viết, bình luận và dashboard quản trị.
