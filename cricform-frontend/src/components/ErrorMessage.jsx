export default function ErrorMessage({ message = 'Failed to load data.' }) {
  return <div className="text-red-600 p-2">{message}</div>;
}
