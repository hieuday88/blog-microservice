# Kế hoạch triển khai Light và Dark Mode

Bản kế hoạch này chi tiết hóa cách thức xây dựng hệ thống giao diện hỗ trợ cả hai chế độ Sáng (Light Mode) và Tối (Dark Mode) cho Blog Microservice. Hiện tại ứng dụng đang có giao diện mặc định là chế độ Tối (Dark Mode), chúng ta sẽ giữ nguyên thiết kế cao cấp hiện tại làm mặc định và bổ sung thêm chế độ Sáng (Light Mode) hài hòa, sang trọng cùng nút chuyển chế độ tại góc trên thanh Header.

---

## User Review Required

> [!IMPORTANT]
> - Giao diện chế độ Sáng (Light Mode) được thiết kế theo tone màu Slate hiện đại pha chút xanh biển dịu mắt để ăn khớp với các điểm nhấn thương hiệu (Brand colors).
> - Để loại bỏ hoàn toàn hiện tượng chớp nháy màn hình (theme flicker) lúc tải trang, chúng ta sẽ chèn một dòng script siêu nhẹ đồng bộ ngay tại thẻ `<head>` của các trang HTML tĩnh.

---

## Proposed Changes

### 1. Style System

#### [MODIFY] [main.css](file:///c:/Users/hieuk/Desktop/Advanced_Java/blog-microservice/frontend/css/main.css)

Chúng ta sẽ điều chỉnh `:root` mặc định thành chế độ tối, và thêm bộ biến đè khi thẻ `<html>` mang thuộc tính `data-theme="light"`.

```css
:root {
  color-scheme: dark;
  --bg: #0b1220;
  --bg-gradient: radial-gradient(circle at 20% -10%, rgba(34, 211, 238, 0.18), transparent 28rem),
                 linear-gradient(135deg, #0b1220 0%, #111827 45%, #08111f 100%);
  --grid-color: rgba(255, 255, 255, 0.035);
  --panel: rgba(17, 24, 39, 0.84);
  --panel-strong: rgba(31, 41, 55, 0.92);
  --line: rgba(148, 163, 184, 0.22);
  --text: #f8fafc;
  --muted: #94a3b8;
  --brand: #22d3ee;
  --brand-strong: #4f46e5;
  --accent: #a7f3d0;
  --danger: #fb7185;
  --warning: #fbbf24;
  --shadow: 0 18px 50px rgba(0, 0, 0, 0.32);
  --radius: 8px;
  --header-bg: rgba(11, 18, 32, 0.88);
  --eyebrow-bg: rgba(31, 41, 55, 0.7);
  --btn-danger-bg: rgba(251, 113, 133, 0.12);
  --btn-danger-border: rgba(251, 113, 133, 0.45);
  --btn-danger-text: #fecdd3;
  --pill-bg: rgba(15, 23, 42, 0.72);
  --input-bg: rgba(15, 23, 42, 0.78);
  --notice-error-border: rgba(251, 113, 133, 0.45);
  --notice-error-text: #fecdd3;
  --notice-success-border: rgba(34, 211, 238, 0.45);
  --notice-success-text: #a5f3fc;
  --service-item-bg: rgba(15, 23, 42, 0.4);
}

[data-theme="light"] {
  color-scheme: light;
  --bg: #f8fafc;
  --bg-gradient: radial-gradient(circle at 20% -10%, rgba(14, 165, 233, 0.12), transparent 28rem),
                 linear-gradient(135deg, #f8fafc 0%, #f1f5f9 45%, #e2e8f0 100%);
  --grid-color: rgba(15, 23, 42, 0.03);
  --panel: rgba(255, 255, 255, 0.85);
  --panel-strong: rgba(241, 245, 249, 0.95);
  --line: rgba(148, 163, 184, 0.3);
  --text: #0f172a;
  --muted: #64748b;
  --brand: #0284c7;
  --brand-strong: #4f46e5;
  --accent: #059669;
  --danger: #e11d48;
  --warning: #d97706;
  --shadow: 0 18px 50px rgba(15, 23, 42, 0.06);
  --header-bg: rgba(248, 250, 252, 0.88);
  --eyebrow-bg: rgba(226, 232, 240, 0.7);
  --btn-danger-bg: rgba(225, 29, 72, 0.08);
  --btn-danger-border: rgba(225, 29, 72, 0.3);
  --btn-danger-text: #be123c;
  --pill-bg: rgba(226, 232, 240, 0.72);
  --input-bg: rgba(255, 255, 255, 0.9);
  --notice-error-border: rgba(225, 29, 72, 0.3);
  --notice-error-text: #be123c;
  --notice-success-border: rgba(13, 148, 136, 0.3);
  --notice-success-text: #0f766e;
  --service-item-bg: rgba(226, 232, 240, 0.4);
}
```

Bên cạnh đó, cập nhật các thuộc tính CSS cứng trong `main.css` thành các biến tương ứng:
- `body` background và grid.
- `.eyebrow` background.
- `.header` background.
- `.btn.danger` background, border, color.
- `.status-pill` background.
- `.field` color.
- `.input, .textarea, .select` background.
- `.notice` background.
- `.notice.error`, `.notice.success` border và color.
- `.stat` background.

#### [MODIFY] [components.css](file:///c:/Users/hieuk/Desktop/Advanced_Java/blog-microservice/frontend/css/components.css)

Cập nhật các màu nền/màu chữ cứng trong `components.css`:
- `.post-media` background.
- `.tabs` background.
- `.article-content` color.
- `.comment` background.
- `.file-drop` background.
- `.preview` background.
- `.service-item` background.

---

### 2. Header Layout & Toggle Logic

#### [MODIFY] [utils.js](file:///c:/Users/hieuk/Desktop/Advanced_Java/blog-microservice/frontend/js/utils.js)

Chúng ta sẽ khai báo hai biểu tượng SVG tuyệt đẹp cho Mặt Trời (Sun) và Mặt Trăng (Moon) và tạo nút bấm `#theme-toggle` nằm sát thanh điều hướng.

```javascript
const sunIcon = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>`;

const moonIcon = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>`;

function renderHeader() {
  const theme = localStorage.getItem("theme") || "dark";
  const icon = theme === "light" ? moonIcon : sunIcon;
  return `
    <div class="container header-inner">
      <a class="brand" href="/">
        <span class="brand-mark">B</span>
        <span>Blog Microservice</span>
      </a>
      <div style="display: flex; align-items: center; gap: 12px;">
        <button id="theme-toggle" class="btn secondary icon-btn" type="button" aria-label="Chuyển chế độ giao diện">
          ${icon}
        </button>
        <nav id="app-nav" class="nav" aria-label="Primary"></nav>
      </div>
    </div>
  `;
}
```

Bên cạnh đó, gắn bộ lắng nghe sự kiện bấm nút `#theme-toggle` bên trong hàm `initShell()`:

```javascript
  const toggleBtn = $("#theme-toggle");
  if (toggleBtn) {
    toggleBtn.addEventListener("click", () => {
      const currentTheme = document.documentElement.getAttribute("data-theme") || "dark";
      const newTheme = currentTheme === "dark" ? "light" : "dark";
      document.documentElement.setAttribute("data-theme", newTheme);
      localStorage.setItem("theme", newTheme);
      toggleBtn.innerHTML = newTheme === "light" ? moonIcon : sunIcon;
    });
  }
```

---

### 3. Flicker Mitigation in HTML

Chúng ta sẽ chèn đoạn script ngắn, hiệu quả vào thẻ `<head>` của tất cả 7 tệp HTML tĩnh:

```html
    <script>
      document.documentElement.setAttribute('data-theme', localStorage.getItem('theme') || 'dark');
    </script>
```

#### [MODIFY] Danh sách tệp HTML:
- [index.html](file:///c:/Users/hieuk/Desktop/Advanced_Java/blog-microservice/frontend/index.html)
- [posts/create.html](file:///c:/Users/hieuk/Desktop/Advanced_Java/blog-microservice/frontend/posts/create.html)
- [posts/detail.html](file:///c:/Users/hieuk/Desktop/Advanced_Java/blog-microservice/frontend/posts/detail.html)
- [posts/edit.html](file:///c:/Users/hieuk/Desktop/Advanced_Java/blog-microservice/frontend/posts/edit.html)
- [auth/login.html](file:///c:/Users/hieuk/Desktop/Advanced_Java/blog-microservice/frontend/auth/login.html)
- [auth/register.html](file:///c:/Users/hieuk/Desktop/Advanced_Java/blog-microservice/frontend/auth/register.html)
- [admin/dashboard.html](file:///c:/Users/hieuk/Desktop/Advanced_Java/blog-microservice/frontend/admin/dashboard.html)

---

## Verification Plan

### Manual Verification
1. Mở trang chủ Blog Microservice, kiểm tra xem giao diện mặc định có tiếp tục hiển thị dưới dạng Dark Mode chuẩn chỉnh như cũ không.
2. Kiểm tra nút chuyển chế độ tại góc trên cùng bên phải Header.
3. Bấm vào nút, xác nhận toàn bộ trang chuyển đổi sang chế độ Sáng (Light Mode) dịu mắt, sắc nét, có tính thẩm mỹ cao.
4. Kiểm tra sự thay đổi của tất cả các thành phần: thẻ bài viết (post-cards), hộp thông báo (notifications), biểu mẫu (forms), danh sách trạng thái dịch vụ, các ô đếm bài viết.
5. F5 tải lại trang ở chế độ Light Mode, xác nhận không xảy ra hiện tượng chớp nháy giao diện (Flicker).
6. Di chuyển qua các trang Tạo bài viết, Sửa bài viết, Đăng nhập, Đăng ký và Quản trị để xác minh tính nhất quán của chế độ giao diện hoạt động chính xác.
