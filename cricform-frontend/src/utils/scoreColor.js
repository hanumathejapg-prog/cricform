export function scoreColor(score) {
  if (score == null) return '#9E9E9E';
  if (score < 30) return '#EF5350';
  if (score < 50) return '#FF7043';
  if (score < 65) return '#FDD835';
  if (score < 80) return '#66BB6A';
  return '#2E7D32';
}
