import { getPosts } from "../api.js";
import { isLoggedIn } from "../auth.js";
import { $, initShell, renderBackendStatus, renderPostCard, setMessage } from "../utils.js";

await initShell();
await renderBackendStatus($("#backend-status"));

const postsEl = $("#posts");
const messageEl = $("#home-message");
const refreshBtn = $("#refresh-posts");
const createBtn = $("#create-post-link");
const postCount = $("#post-count");
const jwtState = $("#jwt-state");

if (createBtn && !isLoggedIn()) {
  createBtn.href = "/auth/login.html";
  createBtn.textContent = "Đăng nhập để xem bài";
}

if (jwtState) {
  jwtState.textContent = isLoggedIn() ? "Bật" : "Tắt";
}

refreshBtn?.addEventListener("click", loadPosts);
await loadPosts();

async function loadPosts() {
  if (!isLoggedIn()) {
    postsEl.innerHTML = `
      <div class="empty panel panel-pad">
        <div>
          <h2>Cần đăng nhập để xem bài viết</h2>
          <p class="muted">Cổng API đang bảo vệ các API bài viết và bình luận khi cần JWT.</p>
          <a class="btn" href="/auth/login.html">Đăng nhập</a>
        </div>
      </div>
    `;
    return;
  }

  setMessage(messageEl, "", "error");
  refreshBtn.disabled = true;
  postsEl.innerHTML = `<div class="notice">Đang tải bài viết...</div>`;

  try {
    const page = await getPosts(0, 12);
    const posts = page.content || [];
    postCount.textContent = posts.length;
    postsEl.innerHTML = posts.length
      ? posts.map(renderPostCard).join("")
      : `<div class="empty panel panel-pad"><div><h2>Chưa có bài viết</h2><p class="muted">Tạo bài đầu tiên để kiểm tra dịch vụ bài viết.</p></div></div>`;
  } catch (error) {
    setMessage(messageEl, error.message || "Không tải được bài viết.", "error");
    postsEl.innerHTML = "";
  } finally {
    refreshBtn.disabled = false;
  }
}
