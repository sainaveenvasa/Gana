import React, { useEffect, useState } from "react";
import api from "../api/api";

function Playlists() {
  const [playlists, setPlaylists] = useState([]);
  const [form, setForm] = useState({
    name: "",
    description: "",
  });

  useEffect(() => {
    fetchPlaylists();
  }, []);

  const fetchPlaylists = async () => {
    try {
      const response = await api.get("/api/playlists");
      setPlaylists(response.data);
    } catch (error) {
      console.error("Error fetching playlists", error);
    }
  };

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const createPlaylist = async (e) => {
    e.preventDefault();
    try {
      await api.post("/api/playlists", form);
      setForm({ name: "", description: "" });
      fetchPlaylists();
    } catch (error) {
      console.error("Error creating playlist", error);
    }
  };

  return (
    <div className="page">
      <h1>Playlists</h1>

      <form onSubmit={createPlaylist} className="form-box">
        <input
          type="text"
          name="name"
          placeholder="Playlist name"
          maxLength={100}
          required
          value={form.name}
          onChange={handleChange}
        />
        <input
          type="text"
          name="description"
          placeholder="Playlist description"
          maxLength={500}
          value={form.description}
          onChange={handleChange}
        />
        <button type="submit">Create Playlist</button>
      </form>

      <div className="playlist-list">
        {playlists.map((playlist) => (
          <div key={playlist.id} className="playlist-card">
            <h3>{playlist.name}</h3>
            <p>{playlist.description}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Playlists;