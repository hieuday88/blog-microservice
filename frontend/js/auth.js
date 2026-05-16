import { TOKEN_KEY } from "./config.js";
import { getCurrentUser, login as loginRequest, register as registerRequest } from "./api.js";

let currentUser = null;

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getUser() {
  return currentUser;
}

export function isLoggedIn() {
  return Boolean(getToken());
}

export function isAdmin(user = currentUser) {
  return Boolean(user?.roles?.some((role) => role === "ROLE_ADMIN" || role.name === "ROLE_ADMIN"));
}

export async function hydrateUser() {
  if (!getToken()) {
    currentUser = null;
    return null;
  }

  try {
    currentUser = await getCurrentUser();
    return currentUser;
  } catch {
    logout();
    return null;
  }
}

export async function login(username, password) {
  const data = await loginRequest(username, password);
  localStorage.setItem(TOKEN_KEY, data.token);
  document.cookie = `${TOKEN_KEY}=${data.token}; path=/; max-age=604800; SameSite=Lax`;
  currentUser = await getCurrentUser();
  window.dispatchEvent(new CustomEvent("auth:changed", { detail: currentUser }));
  return currentUser;
}

export async function register(username, email, password) {
  await registerRequest(username, email, password);
  return login(username, password);
}

export function logout() {
  localStorage.removeItem(TOKEN_KEY);
  document.cookie = `${TOKEN_KEY}=; path=/; max-age=0; SameSite=Lax`;
  currentUser = null;
  window.dispatchEvent(new CustomEvent("auth:changed"));
}

export function requireAuth(next = location.pathname + location.search) {
  if (!getToken()) {
    location.href = `/auth/login.html?next=${encodeURIComponent(next)}`;
    return false;
  }
  return true;
}
