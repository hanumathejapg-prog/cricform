import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';

export default function PerformanceBarChart({ data = [] }) {
  return (
    <div style={{ width: '100%', height: 260 }}>
      <ResponsiveContainer>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="matchId" />
          <YAxis />
          <Tooltip />
          <Bar dataKey="weightedScore" fill="#2563eb" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
