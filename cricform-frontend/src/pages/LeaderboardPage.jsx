import { useContext } from 'react';
import Navbar from '../components/Navbar';
import StaleDataBanner from '../components/StaleDataBanner';
import FormatTabs from '../components/FormatTabs';
import LeaderboardTable from '../components/LeaderboardTable';
import SearchBar from '../components/SearchBar';
import { FormatContext } from '../context/FormatContext';
import { useLeaderboard } from '../hooks/useLeaderboard';
import ErrorBoundary from '../components/ErrorBoundary';

export default function LeaderboardPage() {
  const { selectedFormat, setSelectedFormat } = useContext(FormatContext);
  const batters = useLeaderboard('batters', selectedFormat);
  const bowlers = useLeaderboard('bowlers', selectedFormat);

  return (
    <ErrorBoundary>
      <Navbar />
      <main className="p-4 space-y-3">
        <StaleDataBanner stale={false} />
        <h1 className="text-2xl font-bold">CricForm — Player Form Tracker</h1>
        <FormatTabs value={selectedFormat} onChange={setSelectedFormat} />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <LeaderboardTable type="batters" format={selectedFormat} {...batters} />
          <LeaderboardTable type="bowlers" format={selectedFormat} {...bowlers} />
        </div>
        <SearchBar />
      </main>
    </ErrorBoundary>
  );
}
