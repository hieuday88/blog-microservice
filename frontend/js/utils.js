import { checkBackend, checkService, imageUrl } from "./api.js";
import { getUser, hydrateUser, isAdmin, isLoggedIn, logout } from "./auth.js";
import { routes } from "./config.js";

export const $ = (selector, root = document) => root.querySelector(selector);
export const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

export function escapeHtml(value = "") {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

export function formatDate(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  return new Intl.DateTimeFormat("vi-VN", { dateStyle: "medium" }).format(date);
}

export function getParam(name) {
  return new URLSearchParams(location.search).get(name);
}

export function setMessage(target, text, type = "error") {
  if (!target) {
    return;
  }
  target.textContent = text || "";
  target.className = text ? `notice ${type}` : "hidden";
}

export async function initShell() {
  const header = $("#app-header");
  const footer = $("#app-footer");
  if (header) {
    header.innerHTML = renderHeader();
  }
  if (footer) {
    footer.innerHTML = `<div class="container">Giao diện HTML/CSS/JavaScript tĩnh cho Blog Microservice.</div>`;
  }

  await hydrateUser();
  renderNav();

  window.addEventListener("auth:changed", renderNav);
  document.addEventListener("click", (event) => {
    const action = event.target.closest("[data-action]");
    if (!action) {
      return;
    }
    if (action.dataset.action === "logout") {
      logout();
      location.href = routes.login;
    }
  });
}

export async function renderBackendStatus(target) {
  if (!target) {
    return;
  }
  
  target.innerHTML = `
    <h2 class="section-title">Hệ thống Microservices</h2>
    <div class="service-list">
      <div class="service-item" id="status-gateway">
        <span class="dot"></span> <span>API Gateway</span>
      </div>
      <div class="service-item" id="status-auth">
        <span class="dot"></span> <span>Auth Service</span>
      </div>
      <div class="service-item" id="status-post">
        <span class="dot"></span> <span>Post Service</span>
      </div>
      <div class="service-item" id="status-comment">
        <span class="dot"></span> <span>Comment Service</span>
      </div>
    </div>
  `;

  const services = [
    { id: "gateway", probe: () => checkBackend() },
    { id: "auth", probe: () => checkService("auth") },
    { id: "post", probe: () => checkService("post") },
    { id: "comment", probe: () => checkService("comment") }
  ];

  for (const svc of services) {
    const el = $(`#status-${svc.id}`, target);
    if (!el) continue;
    
    try {
      await svc.probe();
      el.classList.add("online");
    } catch {
      el.classList.add("offline");
    }
  }
}

export function renderPostCard(post) {
  const media = post.imageName
    ? `<img src="${imageUrl(post.imageName)}" alt="${escapeHtml(post.title)}">`
    : `<span>Chưa có ảnh</span>`;

  return `
    <article class="post-card panel">
      <a class="post-media" href="${routes.postDetail(post.id)}">${media}</a>
      <div class="post-body">
        <h2 class="post-title"><a href="${routes.postDetail(post.id)}">${escapeHtml(post.title)}</a></h2>
        <p class="post-desc">${escapeHtml(post.description || post.content || "")}</p>
        <div class="post-meta">
          <span>${escapeHtml(post.authorUsername || `Tác giả #${post.authorId || "không rõ"}`)}</span>
          <span>${formatDate(post.createdAt)}</span>
        </div>
      </div>
    </article>
  `;
}

function renderHeader() {
  return `
    <div class="container header-inner">
      <a class="brand" href="/">
        <span class="brand-mark">B</span>
        <span>Blog Microservice</span>
      </a>
      <nav id="app-nav" class="nav" aria-label="Primary"></nav>
    </div>
  `;
}

function renderNav() {
  const nav = $("#app-nav");
  if (!nav) {
    return;
  }

  if (!isLoggedIn()) {
    nav.innerHTML = `<a class="btn" href="${routes.login}">Đăng nhập</a>`;
    return;
  }

  const user = getUser();
  nav.innerHTML = `
    <span class="status-pill">${escapeHtml(user?.username || "Đã đăng nhập")}</span>
    <a class="btn secondary" href="${routes.createPost}">Viết bài</a>
    ${isAdmin(user) ? `<a class="btn secondary" href="${routes.admin}">Quản trị</a>` : ""}
    <button class="btn ghost icon-btn" type="button" data-action="logout" title="Đăng xuất" aria-label="Đăng xuất">X</button>
  `;
}
