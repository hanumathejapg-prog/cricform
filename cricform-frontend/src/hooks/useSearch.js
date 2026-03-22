import { useEffect, useState } from 'react';
import { searchPlayers } from '../api/cricformApi';

export function useSearch(query) {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!query) return;
    setLoading(true);
    searchPlayers(query)
      .then((res) => setResults(res.data))
      .finally(() => setLoading(false));
  }, [query]);

  return { results, loading };
}
