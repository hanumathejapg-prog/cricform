import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const api = axios.create({ baseURL: BASE_URL });

export const getLeaderboard = (type, format) =>
  api.get(`/api/leaderboard/${type}?format=${format}`);

export const getPlayerProfile = (playerId) =>
  api.get(`/api/players/${playerId}/profile`);

export const getPlayerHistory = (playerId, format, window) =>
  api.get(`/api/players/${playerId}/history?format=${format}&window=${window}`);

export const searchPlayers = (query) =>
  api.get(`/api/search?q=${encodeURIComponent(query)}&limit=10`);

export const getPlayerFormScore = (playerId, format) =>
  api.get(`/api/players/${playerId}/form?format=${format}`);
