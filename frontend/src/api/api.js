import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

/**
 * Attach the JWT (saved at login) to every outgoing request.
 * Login.jsx stores the server's AuthResponse under the "user" key as JSON:
 *   { message, email, role, token, tokenType, expiresInMs }
 */
api.interceptors.request.use((config) => {
  try {
    const raw = localStorage.getItem("user");
    if (raw) {
      const user = JSON.parse(raw);
      if (user?.token) {
        config.headers.Authorization = `Bearer ${user.token}`;
      }
    }
  } catch (e) {
    // malformed localStorage entry — ignore, request will go unauthenticated
    console.warn("Could not read auth token from localStorage", e);
  }
  return config;
});

/**
 * If the backend rejects the token (expired or invalid), clear it and
 * bounce the user to the login page.
 */
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      localStorage.removeItem("user");
      // Avoid redirect loop on the login page itself
      if (!window.location.pathname.startsWith("/login")) {
        window.location.assign("/login");
      }
    }
    return Promise.reject(err);
  }
);

export default api;
