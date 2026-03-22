import { scoreColor } from '../utils/scoreColor';

export default function FormScoreGauge({ score, format, insufficientData }) {
  const display = insufficientData ? 'N/A' : Math.round(score ?? 0);
  const color = insufficientData ? '#9E9E9E' : scoreColor(score ?? 0);

  return (
    <div className="border rounded p-3 text-center" style={{ borderColor: color }}>
      <div className="text-sm text-gray-500">{format}</div>
      <div className="text-2xl font-bold" style={{ color }}>{display}</div>
    </div>
  );
}
