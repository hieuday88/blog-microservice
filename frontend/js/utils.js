import { checkBackend, checkService, imageUrl } from "./api.js";
import { getUser, hydrateUser, isAdmin, isLoggedIn, logout } from "./auth.js";
import { routes } from "./config.js";

export const $ = (selector, root = document) => root.querySelector(selector);
export const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

const sunIcon = `<svg aria-hidden="true" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>`;
const moonIcon = `<svg aria-hidden="true" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>`;

function getTheme() {
  return document.documentElement.getAttribute("data-theme") || localStorage.getItem("theme") || "dark";
}

function themeIcon(theme) {
  return theme === "light" ? moonIcon : sunIcon;
}

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

  const toggleBtn = $("#theme-toggle");
  if (toggleBtn) {
    toggleBtn.addEventListener("click", () => {
      const newTheme = getTheme() === "dark" ? "light" : "dark";
      document.documentElement.setAttribute("data-theme", newTheme);
      localStorage.setItem("theme", newTheme);
      toggleBtn.innerHTML = themeIcon(newTheme);
    });
  }

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
  const theme = getTheme();
  return `
    <div class="container header-inner">
      <a class="brand" href="/">
        <span class="brand-mark">B</span>
        <span>Blog Microservice</span>
      </a>
      <div class="header-actions">
        <button id="theme-toggle" class="btn secondary icon-btn" type="button" title="Chuyen che do giao dien" aria-label="Chuyen che do giao dien">
          ${themeIcon(theme)}
        </button>
        <nav id="app-nav" class="nav" aria-label="Primary"></nav>
      </div>
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
