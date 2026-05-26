import React from "react";

function PlayerBar({ currentSong }) {
  if (!currentSong) return null;

  return (
    <div className="player-bar">
      <div>
        <strong>{currentSong.name}</strong> - {currentSong.artist_name}
      </div>

      <audio key={currentSong.id} controls autoPlay>
        <source src={currentSong.audio} type="audio/mpeg" />
        Your browser does not support the audio element.
      </audio>
    </div>
  );
}

export default PlayerBar;