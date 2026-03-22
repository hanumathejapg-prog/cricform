import { useMemo } from 'react';
import { Link, useLocation } from 'react-router-dom';
import Navbar from '../components/Navbar';
import SearchBar from '../components/SearchBar';
import LoadingSpinner from '../components/LoadingSpinner';
import { useSearch } from '../hooks/useSearch';
import ErrorBoundary from '../components/ErrorBoundary';

export default function SearchPage() {
  const location = useLocation();
  const query = useMemo(() => new URLSearchParams(location.search).get('q') || '', [location.search]);
  const { results, loading } = useSearch(query);

  return (
    <ErrorBoundary>
      <Navbar />
      <main className="p-4 space-y-4">
        <SearchBar />
        {loading && <LoadingSpinner />}
        <ul className="space-y-2">
          {results.map((p) => (
            <li key={p.id} className="border p-2 rounded">
              <Link className="text-blue-600" to={`/player/${p.id}`}>{p.name}</Link> — {p.team}
            </li>
          ))}
        </ul>
      </main>
    </ErrorBoundary>
  );
}
