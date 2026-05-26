import React from "react";

function SongCard({ song, onPlay }) {
  return (
    <div className="song-card">
      <img
        src={song.image || "https://via.placeholder.com/150?text=Music"}
        alt={song.name}
        className="song-image"
        onError={(e) => {
          e.target.src = "https://via.placeholder.com/150?text=Music";
        }}
      />
      <h3>{song.name}</h3>
      <p>{song.artist_name}</p>
      <button onClick={() => onPlay(song)}>Play</button>
    </div>
  );
}

export default SongCard;