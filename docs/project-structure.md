# Project Structure

```text
blog-microservice-main/
|-- backend/
|   |-- api-gateway/
|   |-- auth-service/
|   |-- post-service/
|   `-- comment-service/
|-- frontend/
|   |-- index.html
|   |-- css/
|   |-- js/
|   |-- auth/
|   |-- posts/
|   `-- admin/
|-- scripts/
|   |-- run-all.ps1
|   |-- run-full.ps1
|   |-- run-no-docker.ps1
|   `-- run-fast.ps1
|-- docs/
|-- storage/
|-- docker-compose.yml
|-- pom.xml
`-- README.md
```

Root chỉ giữ entrypoint cấp dự án. Code thực thi nằm trong `backend/` và `frontend/`; tài liệu nằm trong `docs/`; automation nằm trong `scripts/`.
