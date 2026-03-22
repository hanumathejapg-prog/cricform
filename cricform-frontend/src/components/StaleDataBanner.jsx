export default function StaleDataBanner({ stale }) {
  if (!stale) return null;
  return <div className="bg-yellow-100 text-yellow-800 p-2 rounded mb-3">Data may be stale.</div>;
}
