import { deleteComment, deletePost, deleteUser, getComments, getPosts, getUsers, giveAdmin, takeAdmin } from "../api.js";
import { getUser, hydrateUser, isAdmin, requireAuth } from "../auth.js";
import { $, escapeHtml, initShell, setMessage } from "../utils.js";

await initShell();
if (!requireAuth(location.pathname + location.search)) {
  throw new Error("Cần đăng nhập.");
}
await hydrateUser();

const message = $("#admin-message");
const usersEl = $("#users-list");
const postsEl = $("#admin-posts-list");
const commentsEl = $("#comments-list");
const refreshBtn = $("#refresh-admin");
let users = [];
let posts = [];
let comments = [];

if (!isAdmin(getUser())) {
  $(".admin-only").innerHTML = `
    <section class="panel panel-pad empty">
      <div>
        <h1>Cần tài khoản quản trị</h1>
        <p class="muted">Đăng nhập bằng tài khoản có quyền quản trị để tiếp tục.</p>
        <a class="btn" href="/auth/login.html?next=/admin/dashboard.html">Đăng nhập quản trị</a>
      </div>
    </section>
  `;
} else {
  refreshBtn?.addEventListener("click", loadAdminData);
  document.addEventListener("click", handleAction);
  await loadAdminData();
}

async function loadAdminData() {
  setMessage(message, "", "error");
  refreshBtn.disabled = true;
  try {
    const [userList, postPage] = await Promise.all([getUsers(), getPosts(0, 30)]);
    users = userList || [];
    posts = postPage.content || [];
    const groups = await Promise.all(posts.map(async (post) => {
      const items = await getComments(post.id).catch(() => []);
      return items.map((comment) => ({ ...comment, postTitle: post.title }));
    }));
    comments = groups.flat();
    renderAdmin();
  } catch (error) {
    setMessage(message, error.message || "Không tải được dữ liệu quản trị.", "error");
  } finally {
    refreshBtn.disabled = false;
  }
}

async function handleAction(event) {
  const button = event.target.closest("[data-admin-action]");
  if (!button) {
    return;
  }
  const action = button.dataset.adminAction;
  const value = button.dataset.value;

  try {
    if (action === "give-admin") await giveAdmin(value);
    if (action === "take-admin") await takeAdmin(value);
    if (action === "delete-user") await deleteUser(value);
    if (action === "delete-post") await deletePost(value);
    if (action === "delete-comment") await deleteComment(button.dataset.postId, value);
    setMessage(message, "Thao tác thành công.", "success");
    await loadAdminData();
  } catch (error) {
    setMessage(message, error.message || "Thao tác thất bại.", "error");
  }
}

function renderAdmin() {
  $("#users-count").textContent = users.length;
  $("#admins-count").textContent = users.filter((user) => user.roles?.includes("ROLE_ADMIN")).length;
  $("#posts-count").textContent = posts.length;
  $("#comments-count").textContent = comments.length;

  usersEl.innerHTML = users.length ? users.map((user) => {
    const admin = user.roles?.includes("ROLE_ADMIN");
    const defaultAdmin = user.username === "admin";
    return `
      <article class="row">
        <div>
          <strong>${escapeHtml(user.username)}</strong>
          <p class="muted">${escapeHtml(user.email || "")}</p>
          <p class="muted">${escapeHtml((user.roles || []).join(", ") || "Chưa có vai trò")}</p>
        </div>
        <div class="actions">
          <button class="btn secondary" ${admin ? "disabled" : ""} data-admin-action="give-admin" data-value="${escapeHtml(user.username)}">Cấp quyền quản trị</button>
          <button class="btn secondary" ${!admin || defaultAdmin ? "disabled" : ""} data-admin-action="take-admin" data-value="${escapeHtml(user.username)}">Thu quyền quản trị</button>
          <button class="btn danger" ${defaultAdmin ? "disabled" : ""} data-admin-action="delete-user" data-value="${escapeHtml(user.username)}">Xóa</button>
        </div>
      </article>
    `;
  }).join("") : `<p class="muted">Chưa có người dùng.</p>`;

  postsEl.innerHTML = posts.length ? posts.map((post) => `
    <article class="row">
      <div>
        <strong><a href="/posts/detail.html?id=${post.id}">${escapeHtml(post.title)}</a></strong>
        <p class="muted">${escapeHtml(post.description || post.content || "")}</p>
      </div>
      <div class="actions">
        <a class="btn secondary" href="/posts/edit.html?id=${post.id}">Sửa</a>
        <button class="btn danger" data-admin-action="delete-post" data-value="${post.id}">Xóa</button>
      </div>
    </article>
  `).join("") : `<p class="muted">Chưa có bài viết.</p>`;

  commentsEl.innerHTML = comments.length ? comments.map((comment) => `
    <article class="row">
      <div>
        <p>${escapeHtml(comment.content)}</p>
        <p class="muted">Bài #${comment.postId}: ${escapeHtml(comment.postTitle || "")}</p>
      </div>
      <button class="btn danger" data-admin-action="delete-comment" data-post-id="${comment.postId}" data-value="${comment.id}">Xóa</button>
    </article>
  `).join("") : `<p class="muted">Chưa có bình luận.</p>`;
}
