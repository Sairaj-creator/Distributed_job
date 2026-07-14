import React, { useState } from 'react';
import { Activity, LayoutDashboard } from 'lucide-react';
import Dashboard from './components/Dashboard';
import WorkflowVisualizer from './components/WorkflowVisualizer';
import './index.css';

function App() {
  const [activeView, setActiveView] = useState('dashboard');
  const [selectedWorkflowId, setSelectedWorkflowId] = useState(null);

  const navigateToWorkflow = (id) => {
    setSelectedWorkflowId(id);
    setActiveView('workflow');
  };

  const navigateToDashboard = () => {
    setSelectedWorkflowId(null);
    setActiveView('dashboard');
  };

  return (
    <div className="app-container">
      <aside className="sidebar">
        <div className="logo flex-between" style={{ marginBottom: '2.5rem', padding: '0 0.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ background: 'var(--accent-primary)', padding: '0.6rem', borderRadius: '10px', display: 'flex' }}>
              <Activity size={22} color="white" />
            </div>
            <h1 style={{ margin: 0, fontSize: '1.4rem' }}>TaskFlow</h1>
          </div>
        </div>
        
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <button 
            onClick={navigateToDashboard}
            style={{ 
              display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.85rem 1.2rem',
              background: activeView === 'dashboard' ? 'var(--bg-tertiary)' : 'transparent',
              border: 'none', borderRadius: 'var(--radius-sm)', color: activeView === 'dashboard' ? 'var(--text-primary)' : 'var(--text-secondary)',
              cursor: 'pointer', textAlign: 'left', fontWeight: 500, transition: 'all 0.2s ease', fontSize: '1rem'
            }}
          >
            <LayoutDashboard size={20} />
            Dashboard
          </button>
        </nav>
        
        <div style={{ marginTop: 'auto', padding: '1rem', color: 'var(--text-muted)', fontSize: '0.8rem', textAlign: 'center' }}>
          TaskFlow Engine v1.0.0
        </div>
      </aside>

      <main className="main-content">
        {activeView === 'dashboard' && <Dashboard onSelectWorkflow={navigateToWorkflow} />}
        {activeView === 'workflow' && <WorkflowVisualizer workflowId={selectedWorkflowId} onBack={navigateToDashboard} />}
      </main>
    </div>
  );
}

export default App;
