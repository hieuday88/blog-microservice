import { API_BASE_URL, TOKEN_KEY, UPLOAD_BASE_URL } from "./config.js";

async function request(path, options = {}) {
  const headers = new Headers(options.headers || {});
  const token = localStorage.getItem(TOKEN_KEY);

  if (!headers.has("Content-Type") && options.body && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return response.json();
  }

  return response.text();
}

async function readError(response) {
  const text = await response.text().catch(() => "");
  if (!text) {
    return `Yêu cầu thất bại với mã ${response.status}`;
  }
  try {
    const data = JSON.parse(text);
    return data.message || text;
  } catch {
    return text;
  }
}

export function imageUrl(imageName) {
  return imageName ? `${UPLOAD_BASE_URL}/${imageName}` : "";
}

export async function checkBackend() {
  const username = `frontend_probe_${Date.now()}`;
  return request(`/api/users/checkUsernameAvailability?username=${encodeURIComponent(username)}`);
}

export async function checkService(service) {
  // We use endpoints that are known to exist and be public (GET)
  const probes = {
    auth: "/api/users/checkUsernameAvailability?username=probe",
    post: "/api/posts?page=0&size=1",
    comment: "/api/posts/0/comments" // This exists in CommentController and returns 200 [] even if ID 0 doesn't exist
  };

  try {
    await request(probes[service]);
    return true;
  } catch (error) {
    console.warn(`Health check failed for ${service}:`, error.message);
    return false;
  }
}

export function login(username, password) {
  return request("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
}

export function register(username, email, password) {
  return request("/api/auth/register", {
    method: "POST",
    body: JSON.stringify({ username, email, password }),
  });
}

export function getCurrentUser() {
  return request("/api/users/me");
}

export function getUsers() {
  return request("/api/users");
}

export function giveAdmin(username) {
  return request(`/api/users/${encodeURIComponent(username)}/giveAdmin`, { method: "PUT" });
}

export function takeAdmin(username) {
  return request(`/api/users/${encodeURIComponent(username)}/takeAdmin`, { method: "PUT" });
}

export function deleteUser(username) {
  return request(`/api/users/${encodeURIComponent(username)}`, { method: "DELETE" });
}

export function getPosts(page = 0, size = 9) {
  return request(`/api/posts?page=${page}&size=${size}&sortBy=id&sortDir=desc`);
}

export function getPost(id) {
  return request(`/api/posts/${encodeURIComponent(id)}`);
}

export async function createPost(input) {
  const imagePayload = await fileToImagePayload(input.image);
  return request("/api/posts", {
    method: "POST",
    body: JSON.stringify({
      title: input.title,
      description: input.description,
      content: input.content,
      authorId: input.authorId,
      authorUsername: input.authorUsername,
      ...imagePayload,
    }),
  });
}

export async function updatePost(input) {
  const imagePayload = await fileToImagePayload(input.image);
  return request(`/api/posts/${encodeURIComponent(input.id)}`, {
    method: "PUT",
    body: JSON.stringify({
      title: input.title,
      description: input.description,
      content: input.content,
      authorId: input.authorId,
      ...imagePayload,
    }),
  });
}

export function deletePost(id) {
  return request(`/api/posts/${encodeURIComponent(id)}`, { method: "DELETE" });
}

export function getComments(postId) {
  return request(`/api/posts/${encodeURIComponent(postId)}/comments`);
}

export function createComment(postId, content, userId, username) {
  return request(`/api/posts/${encodeURIComponent(postId)}/comments`, {
    method: "POST",
    body: JSON.stringify({ content, userId, username }),
  });
}

export function updateComment(postId, commentId, content) {
  return request(`/api/posts/${encodeURIComponent(postId)}/comments/${encodeURIComponent(commentId)}`, {
    method: "PUT",
    body: JSON.stringify({ content }),
  });
}

export function deleteComment(postId, commentId) {
  return request(`/api/posts/${encodeURIComponent(postId)}/comments/${encodeURIComponent(commentId)}`, {
    method: "DELETE",
  });
}

export function changePassword(currentPassword, newPassword) {
  return request("/api/users/changePassword", {
    method: "PUT",
    body: JSON.stringify({ currentPassword, newPassword }),
  });
}

function fileToImagePayload(image) {
  if (!image) {
    return Promise.resolve({});
  }

  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = () => reject(new Error("Không đọc được tệp ảnh."));
    reader.onloadend = () => resolve({
      imageBase64: reader.result,
      imageFileName: image.name,
    });
    reader.readAsDataURL(image);
  });
}
