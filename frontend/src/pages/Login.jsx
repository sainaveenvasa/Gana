import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/api";

function Login() {
  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post("/api/auth/login", form);
      localStorage.setItem("user", JSON.stringify(response.data));
      alert(response.data.message);
      navigate("/");
    } catch (error) {
      alert(error?.response?.data?.message || "Login failed");
      console.error(error);
    }
  };

  return (
    <div className="form-page">
      <h1>Login</h1>
      <form onSubmit={handleLogin} className="form-box">
        <input
          type="email"
          name="email"
          placeholder="Enter email"
          value={form.email}
          onChange={handleChange}
        />
        <input
          type="password"
          name="password"
          placeholder="Enter password"
          autoComplete="current-password"
          value={form.password}
          onChange={handleChange}
        />
        <button type="submit">Login</button>
      </form>
    </div>
  );
}

export default Login;