import React, { useEffect, useState } from "react";
import api from "../api/api";
import SongCard from "../components/SongCard";
import PlayerBar from "../components/PlayerBar";

function Home() {
  const [songs, setSongs] = useState([]);
  const [search, setSearch] = useState("");
  const [currentSong, setCurrentSong] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchJamendoSongs();
  }, []);

  const fetchJamendoSongs = async () => {
    try {
      setLoading(true);
      const response = await api.get("/api/jamendo/popular?limit=20");
      setSongs(response.data);
    } catch (error) {
      console.error("Error fetching Jamendo songs", error);
      setSongs([]);
    } finally {
      setLoading(false);
    }
  };

  const searchJamendoSongs = async () => {
    try {
      if (search.trim() === "") {
        fetchJamendoSongs();
        return;
      }

      setLoading(true);
      const response = await api.get(
        `/api/jamendo/search?keyword=${encodeURIComponent(search)}&limit=20`
      );
      setSongs(response.data);
    } catch (error) {
      console.error("Error searching Jamendo songs", error);
      setSongs([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1>Discover Music</h1>

      <div className="search-box">
        <input
          type="text"
          placeholder="Search by title or artist"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <button onClick={searchJamendoSongs}>Search</button>
        <button onClick={fetchJamendoSongs}>Popular</button>
      </div>

      {loading && <p>Loading songs...</p>}

      {!loading && songs.length === 0 && <p>No songs found.</p>}

      <div className="songs-grid">
        {songs.map((song) => (
          <SongCard key={song.id} song={song} onPlay={setCurrentSong} />
        ))}
      </div>

      <PlayerBar currentSong={currentSong} />
    </div>
  );
}

export default Home;