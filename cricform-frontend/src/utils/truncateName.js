export function truncateName(name, len = 20) {
  if (!name) return '';
  return name.length > len ? `${name.slice(0, len - 1)}…` : name;
}
