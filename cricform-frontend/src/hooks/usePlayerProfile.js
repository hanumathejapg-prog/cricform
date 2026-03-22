import { useEffect, useState } from 'react';
import { getPlayerHistory, getPlayerProfile } from '../api/cricformApi';

export function usePlayerProfile(playerId, format) {
  const [profile, setProfile] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    Promise.all([
      getPlayerProfile(playerId).then((r) => setProfile(r.data)),
      getPlayerHistory(playerId, format, 10).then((r) => setHistory(r.data))
    ])
      .catch(() => setError('Failed to load player profile'))
      .finally(() => setLoading(false));
  }, [playerId, format]);

  return { profile, history, loading, error };
}
