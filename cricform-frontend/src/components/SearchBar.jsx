import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function SearchBar({ onSearch }) {
  const [value, setValue] = useState('');
  const navigate = useNavigate();

  const submit = (e) => {
    e.preventDefault();
    onSearch?.(value);
    navigate(`/search?q=${encodeURIComponent(value)}`);
  };

  return (
    <form onSubmit={submit} className="flex gap-2 mt-4">
      <input className="border p-2 rounded flex-1" value={value} onChange={(e) => setValue(e.target.value)} placeholder="Search any player..." />
      <button className="bg-blue-600 text-white px-4 rounded">Search</button>
    </form>
  );
}
