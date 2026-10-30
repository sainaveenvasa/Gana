import React from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/api";

function Navbar() {
  const navigate = useNavigate();

  let user = null;
  try {
    const storedUser = localStorage.getItem("user");
    user = storedUser ? JSON.parse(storedUser) : null;
  } catch (error) {
    user = null;
  }

  const handleLogout = async () => {
    try {
      // Revoke the token on the server while we can still authenticate.
      await api.post("/api/auth/logout");
    } catch (error) {
      console.warn("Could not revoke token on the server", error);
    }
    localStorage.removeItem("user");
    navigate("/login");
  };

  return (
    <nav className="navbar">
      <h2>Gana Music</h2>
      <div className="nav-links">
        <Link to="/">Home</Link>
        <Link to="/playlists">Playlists</Link>

        {!user ? (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        ) : (
          <>
            <span className="welcome-text">{user.email}</span>
            <button onClick={handleLogout} className="logout-btn">
              Logout
            </button>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;