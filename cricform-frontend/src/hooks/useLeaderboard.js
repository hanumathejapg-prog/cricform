import { useEffect, useState } from 'react';
import { getLeaderboard } from '../api/cricformApi';

export function useLeaderboard(type, format) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    getLeaderboard(type, format)
      .then((res) => setData(res.data))
      .catch(() => setError('Failed to load leaderboard'))
      .finally(() => setLoading(false));
  }, [type, format]);

  return { data, loading, error };
}
