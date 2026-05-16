import { login, register } from "../auth.js";
import { $, getParam, initShell, setMessage } from "../utils.js";

await initShell();

const form = $("#auth-form");
const message = $("#auth-message");
const emailField = $("#email-field");
const modeInput = $("#auth-mode");
const submitBtn = $("#auth-submit");
const loginTab = $("#tab-login");
const registerTab = $("#tab-register");
let mode = document.body.dataset.mode || "login";

setMode(mode);

loginTab?.addEventListener("click", () => setMode("login"));
registerTab?.addEventListener("click", () => setMode("register"));

form?.addEventListener("submit", async (event) => {
  event.preventDefault();
  setMessage(message, "", "error");
  submitBtn.disabled = true;
  submitBtn.textContent = "Đang xử lý...";

  const data = new FormData(form);
  try {
    if (mode === "login") {
      await login(data.get("username"), data.get("password"));
    } else {
      await register(data.get("username"), data.get("email"), data.get("password"));
    }
    location.href = getParam("next") || "/";
  } catch (error) {
    setMessage(message, error.message || "Xác thực thất bại.", "error");
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = mode === "login" ? "Đăng nhập" : "Tạo tài khoản";
  }
});

function setMode(nextMode) {
  mode = nextMode;
  modeInput.value = mode;
  document.title = mode === "login" ? "Đăng nhập | Blog Microservice" : "Đăng ký | Blog Microservice";
  emailField.classList.toggle("hidden", mode === "login");
  $("#email").required = mode === "register";
  submitBtn.textContent = mode === "login" ? "Đăng nhập" : "Tạo tài khoản";
  loginTab.classList.toggle("active", mode === "login");
  registerTab.classList.toggle("active", mode === "register");
}
