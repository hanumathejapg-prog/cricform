import { useContext } from 'react';
import { useParams } from 'react-router-dom';
import Navbar from '../components/Navbar';
import FormatTabs from '../components/FormatTabs';
import FormScoreGauge from '../components/FormScoreGauge';
import PerformanceBarChart from '../components/PerformanceBarChart';
import ScoreBreakdown from '../components/ScoreBreakdown';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import { FormatContext } from '../context/FormatContext';
import { usePlayerProfile } from '../hooks/usePlayerProfile';
import ErrorBoundary from '../components/ErrorBoundary';

export default function PlayerProfilePage() {
  const { playerId } = useParams();
  const { selectedFormat, setSelectedFormat } = useContext(FormatContext);
  const { profile, history, loading, error } = usePlayerProfile(playerId, selectedFormat);

  return (
    <ErrorBoundary>
      <Navbar />
      <main className="p-4 space-y-4">
        {loading && (
          <div>
            <div className="h-8 bg-gray-200 animate-pulse rounded mb-2" />
            <div className="h-24 bg-gray-200 animate-pulse rounded mb-2" />
            <LoadingSpinner />
          </div>
        )}
        {error && <ErrorMessage message={error} />}
        {profile && (
          <>
            <div>
              <h1 className="text-2xl font-bold">{profile.playerName}</h1>
              <p>{profile.team} • {profile.role}</p>
            </div>
            <div className="grid grid-cols-3 gap-3">
              <FormScoreGauge score={profile.t20iFormScore} format="T20I" insufficientData={profile.insufficientData} />
              <FormScoreGauge score={profile.odiFormScore} format="ODI" insufficientData={profile.insufficientData} />
              <FormScoreGauge score={profile.testFormScore} format="TEST" insufficientData={profile.insufficientData} />
            </div>
            <FormatTabs value={selectedFormat} onChange={setSelectedFormat} />
            <PerformanceBarChart data={history} />
            <ScoreBreakdown history={history} />
          </>
        )}
      </main>
    </ErrorBoundary>
  );
}
