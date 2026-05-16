import { createComment, getComments, getPost, imageUrl, updateComment } from "../api.js";
import { getUser } from "../auth.js";
import { $, escapeHtml, formatDate, getParam, initShell, setMessage } from "../utils.js";

await initShell();

const id = getParam("id");
const article = $("#post-article");
const commentsEl = $("#comments");
const commentForm = $("#comment-form");
const commentContent = $("#comment-content");
const message = $("#post-message");
const commentMessage = $("#comment-message");
let post = null;
let comments = [];

if (!id) {
  setMessage(message, "Thiếu mã bài viết.", "error");
} else {
  await loadPost();
  await loadComments();
}

commentForm?.addEventListener("submit", async (event) => {
  event.preventDefault();
  const content = commentContent.value.trim();
  if (!content) {
    return;
  }

  const user = getUser();
  try {
    const created = await createComment(id, content, user?.id, user?.username);
    comments = [created, ...comments];
    commentContent.value = "";
    renderComments();
  } catch (error) {
    setMessage(commentMessage, error.message || "Không gửi được bình luận.", "error");
  }
});

commentsEl?.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-edit-comment]");
  if (!button) {
    return;
  }
  const commentId = Number(button.dataset.editComment);
  const current = comments.find((item) => item.id === commentId);
  const next = prompt("Sửa bình luận", current?.content || "");
  if (!next?.trim()) {
    return;
  }
  try {
    const updated = await updateComment(id, commentId, next.trim());
    comments = comments.map((item) => item.id === commentId ? updated : item);
    renderComments();
  } catch (error) {
    setMessage(commentMessage, error.message || "Không sửa được bình luận.", "error");
  }
});

async function loadPost() {
  article.innerHTML = `<div class="notice">Đang tải bài viết...</div>`;
  try {
    post = await getPost(id);
    renderPost();
  } catch (error) {
    setMessage(message, error.message || "Không tải được bài viết.", "error");
    article.innerHTML = "";
  }
}

function renderPost() {
  const user = getUser();
  const canEdit = user && post && user.id === post.authorId;
  article.innerHTML = `
    <div class="article-header">
      <span class="eyebrow">${escapeHtml(post.authorUsername || `Tác giả #${post.authorId || "không rõ"}`)}</span>
      <div class="actions">
        ${canEdit ? `<a class="btn secondary" href="/posts/edit.html?id=${post.id}">Sửa</a>` : ""}
      </div>
    </div>
    <h1 class="article-title">${escapeHtml(post.title)}</h1>
    <p class="lead">${escapeHtml(post.description || "")}</p>
    <p class="muted">${formatDate(post.createdAt)}</p>
    <div class="post-media" style="margin-top: 24px;">
      ${post.imageName ? `<img src="${imageUrl(post.imageName)}" alt="${escapeHtml(post.title)}">` : `<span>Chưa có ảnh</span>`}
    </div>
    <div class="article-content">
      ${(post.content || "").split("\n").map((line) => `<p>${escapeHtml(line)}</p>`).join("")}
    </div>
  `;
}

async function loadComments() {
  commentsEl.innerHTML = `<div class="notice">Đang tải bình luận...</div>`;
  try {
    comments = await getComments(id);
    renderComments();
  } catch (error) {
    setMessage(commentMessage, error.message || "Không tải được bình luận.", "error");
    commentsEl.innerHTML = "";
  }
}

function renderComments() {
  const user = getUser();
  commentsEl.innerHTML = comments.length
    ? comments.map((comment) => `
      <article class="comment">
        <p>${escapeHtml(comment.content)}</p>
        <div class="post-meta">
          <span>${escapeHtml(comment.username || `Người dùng #${comment.userId || "không rõ"}`)}</span>
          ${user?.id === comment.userId ? `<button class="btn ghost" data-edit-comment="${comment.id}" type="button">Sửa</button>` : ""}
        </div>
      </article>
    `).join("")
    : `<p class="muted">Chưa có bình luận.</p>`;
}
