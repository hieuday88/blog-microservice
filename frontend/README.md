# Giao diện tĩnh Blog Microservice

Đây là giao diện HTML/CSS/JavaScript thuần. Giao diện dùng API Gateway tại `http://localhost:8080` và lưu JWT trong `localStorage` với khóa `blog-token`.

Chạy từ thư mục này trên cổng `3002` để cấu hình CORS hiện tại của Quarkus cho phép gửi request:

```powershell
cd frontend
python -m http.server 3002
```

Mở `http://localhost:3002`.

Nếu API Gateway chạy ở địa chỉ khác, đặt `localStorage.blog-api-base-url` trong console của trình duyệt trước khi tải trang.
