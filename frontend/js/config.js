export const API_BASE_URL = localStorage.getItem("blog-api-base-url") || "http://localhost:8080";
export const UPLOAD_BASE_URL = `${API_BASE_URL}/api/posts/images`;
export const TOKEN_KEY = "blog-token";

export const routes = {
  home: "/",
  login: "/auth/login.html",
  register: "/auth/register.html",
  admin: "/admin/dashboard.html",
  createPost: "/posts/create.html",
  editPost: (id) => `/posts/edit.html?id=${encodeURIComponent(id)}`,
  postDetail: (id) => `/posts/detail.html?id=${encodeURIComponent(id)}`,
};
