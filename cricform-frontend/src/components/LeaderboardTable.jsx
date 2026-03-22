import { Link } from 'react-router-dom';
import { scoreColor } from '../utils/scoreColor';
import LoadingSpinner from './LoadingSpinner';
import ErrorMessage from './ErrorMessage';

export default function LeaderboardTable({ type, format, data = [], loading, error }) {
  if (loading) {
    return (
      <div className="p-2">
        {[...Array(5)].map((_, i) => <div key={i} className="h-8 bg-gray-200 animate-pulse mb-2 rounded" />)}
        <LoadingSpinner />
      </div>
    );
  }
  if (error) return <ErrorMessage message={error} />;

  return (
    <table className="w-full border text-sm">
      <thead><tr><th>Rank</th><th>Player</th><th>Team</th><th>Score</th><th>Trend</th></tr></thead>
      <tbody>
        {data.map((row) => (
          <tr key={`${type}-${format}-${row.playerId}`}>
            <td>{row.rank}</td>
            <td><Link className="text-blue-600" to={`/player/${row.playerId}`}>{row.playerName}</Link></td>
            <td>{row.team}</td>
            <td><span style={{ color: scoreColor(row.formScore) }}>{row.formScore?.toFixed?.(1) ?? 'N/A'}</span></td>
            <td>{row.trend ?? 'STABLE'}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
