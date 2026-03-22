import { Link } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav className="p-4 bg-slate-900 text-white flex gap-4">
      <Link to="/">Leaderboard</Link>
      <Link to="/search">Search</Link>
    </nav>
  );
}
