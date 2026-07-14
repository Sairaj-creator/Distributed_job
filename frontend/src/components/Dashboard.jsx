import React, { useState, useEffect } from 'react';
import { Play, Pause, Server, CheckCircle, ActivitySquare } from 'lucide-react';

function Dashboard({ onSelectWorkflow }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDashboardData();
    const interval = setInterval(fetchDashboardData, 2000);
    return () => clearInterval(interval);
  }, []);

  const fetchDashboardData = async () => {
    try {
      const response = await fetch('http://localhost:8081/status');
      if (!response.ok) throw new Error('Failed to fetch data');
      const json = await response.json();
      setData(json);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="flex-center" style={{ height: '100%' }}>Loading dashboard...</div>;
  }

  if (error) {
    return (
      <div className="glass-card animate-fade-in" style={{ borderColor: 'var(--status-error)' }}>
        <h2 style={{ color: 'var(--status-error)', marginBottom: '0.5rem' }}>Connection Error</h2>
        <p className="text-muted">Ensure the TaskFlow engine is running on port 8080. ({error})</p>
      </div>
    );
  }

  const { summary, workflows } = data;

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '2.5rem' }}>
      <div>
        <h1 style={{ marginBottom: '0.5rem' }}>Overview</h1>
        <p className="text-muted">Real-time statistics of your orchestration engine.</p>
      </div>

      <div className="grid-cards">
        <KpiCard title="Total Workflows" value={summary.totalWorkflows} icon={<Server size={24} color="var(--accent-primary)" />} />
        <KpiCard title="Total Jobs" value={summary.totalJobs} icon={<ActivitySquare size={24} color="#8b5cf6" />} />
        <KpiCard title="Running Jobs" value={summary.runningJobs} icon={<Play size={24} color="var(--status-running)" />} />
        <KpiCard title="Success Rate" value={`${(parseFloat(summary.successRate) * 100).toFixed(0)}%`} icon={<CheckCircle size={24} color="var(--status-success)" />} />
      </div>

      <div>
        <h2 style={{ marginBottom: '1.5rem' }}>Active Workflows</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {workflows.map((wf) => (
            <div 
              key={wf.workflowId} 
              className="glass-card flex-between" 
              style={{ cursor: 'pointer', padding: '1.25rem 1.5rem' }}
              onClick={() => onSelectWorkflow(wf.workflowId)}
            >
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '0.5rem' }}>
                  <h3 style={{ margin: 0, fontSize: '1.1rem' }}>{wf.name}</h3>
                  <span className={`badge ${wf.paused ? 'error' : 'success'}`}>
                    {wf.paused ? 'PAUSED' : 'ACTIVE'}
                  </span>
                </div>
                <div className="text-muted" style={{ display: 'flex', gap: '1.5rem' }}>
                  <span>ID: {wf.workflowId}</span>
                  <span>Jobs: {wf.jobCount}</span>
                  <span>Schedule: {wf.scheduleType} ({wf.scheduleSpec})</span>
                </div>
              </div>
              <div style={{ background: 'var(--border-light)', padding: '0.5rem', borderRadius: '50%' }}>
                {wf.paused ? <Pause size={20} color="var(--text-secondary)" /> : <Play size={20} color="var(--status-success)" />}
              </div>
            </div>
          ))}
          {workflows.length === 0 && (
            <div className="glass-card flex-center text-muted" style={{ padding: '3rem' }}>
              No workflows registered in the database.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function KpiCard({ title, value, icon }) {
  return (
    <div className="glass-card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', padding: '1.5rem' }}>
      <div style={{ background: 'var(--bg-tertiary)', padding: '1rem', borderRadius: '12px' }}>
        {icon}
      </div>
      <div>
        <div className="text-muted" style={{ fontSize: '0.9rem', marginBottom: '0.25rem' }}>{title}</div>
        <div style={{ fontSize: '1.8rem', fontWeight: 700 }}>{value}</div>
      </div>
    </div>
  );
}

export default Dashboard;
