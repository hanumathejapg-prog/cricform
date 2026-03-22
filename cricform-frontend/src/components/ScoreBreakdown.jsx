export default function ScoreBreakdown({ history = [] }) {
  return (
    <table className="w-full border text-sm">
      <thead><tr><th>Match</th><th>Opp</th><th>Runs</th><th>Attack</th></tr></thead>
      <tbody>
        {history.map((h) => (
          <tr key={h.matchId}>
            <td>{h.matchId}</td><td>{h.opponent}</td><td>{h.runs}</td><td>{h.bowlingAttackScore}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
