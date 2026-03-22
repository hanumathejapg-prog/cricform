export default function FormatTabs({ value, onChange }) {
  return (
    <div className="flex gap-2 my-3">
      {['T20I', 'ODI', 'TEST'].map((fmt) => (
        <button
          key={fmt}
          className={`px-3 py-1 rounded ${value === fmt ? 'bg-blue-600 text-white' : 'bg-gray-100'}`}
          onClick={() => onChange(fmt)}
        >
          {fmt}
        </button>
      ))}
    </div>
  );
}
