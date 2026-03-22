export default function PlayerCard({ player }) {
  if (!player) return null;
  return (
    <div className="border p-3 rounded">
      <div className="font-semibold">{player.playerName}</div>
      <div>{player.team} • {player.role}</div>
    </div>
  );
}
