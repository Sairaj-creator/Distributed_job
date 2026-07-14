import React, { useState, useEffect } from 'react';
import { ArrowLeft, Activity } from 'lucide-react';

function WorkflowVisualizer({ workflowId, onBack }) {
  const [workflow, setWorkflow] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchWorkflowData();
    const interval = setInterval(fetchWorkflowData, 2000);
    return () => clearInterval(interval);
  }, [workflowId]);

  const fetchWorkflowData = async () => {
    try {
      const response = await fetch(`http://localhost:8081/workflows/${workflowId}`);
      if (!response.ok) throw new Error('Failed to fetch workflow data');
      const json = await response.json();
      setWorkflow(json);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="flex-center" style={{ height: '100%' }}>Loading workflow...</div>;
  }

  if (error || !workflow) {
    return (
      <div className="glass-card animate-fade-in" style={{ borderColor: 'var(--status-error)' }}>
        <h2 style={{ color: 'var(--status-error)' }}>Error Loading Workflow</h2>
        <p className="text-muted">{error}</p>
        <button onClick={onBack} style={{ marginTop: '1rem', padding: '0.5rem 1rem', background: 'var(--bg-tertiary)', border: 'none', color: 'white', borderRadius: '4px', cursor: 'pointer' }}>Go Back</button>
      </div>
    );
  }

  const jobs = workflow.jobs || [];

  const getStatusColor = (status) => {
    switch(status) {
      case 'SUCCEEDED': return 'var(--status-success)';
      case 'FAILED': return 'var(--status-error)';
      case 'RUNNING': return 'var(--status-running)';
      case 'SCHEDULED': return 'var(--status-scheduled)';
      case 'NO_RUNS': return 'var(--status-scheduled)';
      default: return 'var(--text-secondary)';
    }
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div className="flex-between">
        <div>
          <button 
            onClick={onBack}
            style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'transparent', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', marginBottom: '1rem', padding: 0 }}
          >
            <ArrowLeft size={16} /> Back to Dashboard
          </button>
          <h1 style={{ marginBottom: '0.5rem' }}>{workflow.name}</h1>
          <p className="text-muted">{workflow.description}</p>
        </div>
      </div>

      <div className="glass-card" style={{ padding: '2rem', minHeight: '400px' }}>
        <h3 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Activity size={18} /> Jobs & Dependencies
        </h3>
        
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1.5rem' }}>
           {jobs.map(job => (
             <div 
               key={job.jobId} 
               style={{ 
                 background: 'var(--bg-tertiary)', 
                 border: `1px solid ${getStatusColor(job.lastStatus)}`,
                 borderLeftWidth: '4px',
                 borderRadius: '8px',
                 padding: '1.25rem',
                 width: '300px',
                 display: 'flex',
                 flexDirection: 'column',
                 gap: '0.75rem',
                 boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
               }}
             >
               <div className="flex-between">
                 <h4 style={{ margin: 0, fontSize: '1.05rem' }}>{job.name}</h4>
                 <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                   <span style={{ fontSize: '0.7rem', color: getStatusColor(job.lastStatus), fontWeight: 'bold' }}>{job.lastStatus}</span>
                   <div style={{ width: '10px', height: '10px', borderRadius: '50%', background: getStatusColor(job.lastStatus), boxShadow: `0 0 8px ${getStatusColor(job.lastStatus)}` }} title={job.lastStatus} />
                 </div>
               </div>
               
               <div className="text-muted" style={{ fontSize: '0.8rem', fontFamily: 'monospace' }}>
                 {job.jobId}
               </div>
               
               {job.dependsOn && job.dependsOn.length > 0 && (
                 <div style={{ fontSize: '0.8rem', marginTop: '0.5rem', background: 'rgba(0,0,0,0.2)', padding: '0.75rem', borderRadius: '6px' }}>
                   <span style={{ color: 'var(--text-secondary)', display: 'block', marginBottom: '0.25rem' }}>Dependencies:</span>
                   <ul style={{ margin: 0, paddingLeft: '1.2rem', color: 'var(--text-primary)' }}>
                     {job.dependsOn.map(dep => <li key={dep}>{dep}</li>)}
                   </ul>
                 </div>
               )}
             </div>
           ))}
        </div>
      </div>
    </div>
  );
}

export default WorkflowVisualizer;
