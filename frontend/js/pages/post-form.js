import { createPost, getPost, imageUrl, updatePost } from "../api.js";
import { getUser, hydrateUser, requireAuth } from "../auth.js";
import { $, getParam, initShell, setMessage } from "../utils.js";

await initShell();
if (!requireAuth()) {
  throw new Error("Cần đăng nhập.");
}
await hydrateUser();

const mode = document.body.dataset.mode || "create";
const id = getParam("id");
const form = $("#post-form");
const message = $("#post-form-message");
const imageInput = $("#image");
const imageLabel = $("#image-label");
const preview = $("#image-preview");
const submitBtn = $("#post-submit");
let currentPost = null;

if (mode === "edit") {
  if (!id) {
    setMessage(message, "Thiếu mã bài viết.", "error");
  } else {
    await loadPost();
  }
}

imageInput?.addEventListener("change", () => {
  const file = imageInput.files?.[0];
  imageLabel.textContent = file ? file.name : "Chọn ảnh bài viết";
  if (file) {
    preview.innerHTML = `<img src="${URL.createObjectURL(file)}" alt="Xem trước ảnh">`;
    preview.classList.remove("hidden");
  }
});

form?.addEventListener("submit", async (event) => {
  event.preventDefault();
  const user = getUser();
  if (!user) {
    setMessage(message, "Không tìm thấy người dùng hiện tại.", "error");
    return;
  }

  const data = new FormData(form);
  const input = {
    id,
    title: data.get("title"),
    description: data.get("description"),
    content: data.get("content"),
    authorId: mode === "create" ? user.id : currentPost?.authorId,
    authorUsername: user.username,
    image: imageInput.files?.[0] || null,
  };

  submitBtn.disabled = true;
  submitBtn.textContent = mode === "create" ? "Đang đăng..." : "Đang lưu...";
  try {
    const saved = mode === "create" ? await createPost(input) : await updatePost(input);
    location.href = `/posts/detail.html?id=${saved.id || id}`;
  } catch (error) {
    setMessage(message, error.message || "Lưu bài viết thất bại.", "error");
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = mode === "create" ? "Đăng bài" : "Lưu thay đổi";
  }
});

async function loadPost() {
  try {
    currentPost = await getPost(id);
    $("#title").value = currentPost.title || "";
    $("#description").value = currentPost.description || "";
    $("#content").value = currentPost.content || "";
    if (currentPost.imageName) {
      preview.innerHTML = `<img src="${imageUrl(currentPost.imageName)}" alt="${currentPost.title || "Ảnh bài viết"}">`;
      preview.classList.remove("hidden");
    }
  } catch (error) {
    setMessage(message, error.message || "Không tải được bài viết.", "error");
  }
}
