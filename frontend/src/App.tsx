import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, NavLink } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import { Activity, LayoutDashboard, List, ActivitySquare, Workflow } from "lucide-react";
import { DashboardPage } from "./pages/DashboardPage";
import { WorkflowsListPage } from "./pages/WorkflowsListPage";
import { WorkflowDetailPage } from "./pages/WorkflowDetailPage";
import { JobsPage } from "./pages/JobsPage";
import { MonitoringPage } from "./pages/MonitoringPage";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
    },
  },
});

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <div className="flex h-screen bg-canvas text-zinc-200 font-sans selection:bg-accent/30 overflow-hidden">
          {/* Sidebar */}
          <aside className="w-64 border-r border-border bg-surface flex flex-col shrink-0">
            <div className="h-16 flex items-center px-6 border-b border-border mb-4">
              <div className="flex items-center gap-3">
                <div className="bg-accent p-1.5 rounded-lg flex items-center justify-center">
                  <Activity size={20} className="text-white" />
                </div>
                <span className="font-semibold text-lg tracking-tight">TaskFlow</span>
              </div>
            </div>

            <nav className="flex-1 px-4 space-y-1">
              <SidebarLink to="/" icon={<LayoutDashboard size={18} />} label="Dashboard" />
              <SidebarLink to="/workflows" icon={<Workflow size={18} />} label="Workflows" />
              <SidebarLink to="/jobs" icon={<List size={18} />} label="Jobs" />
              <SidebarLink to="/monitoring" icon={<ActivitySquare size={18} />} label="Monitoring" />
            </nav>

            <div className="p-4 border-t border-border mt-auto">
              <div className="text-xs text-zinc-500 text-center">TaskFlow Engine v1.0.0</div>
            </div>
          </aside>

          {/* Main Content */}
          <main className="flex-1 flex flex-col min-w-0 overflow-hidden relative">
            <div className="flex-1 overflow-auto p-8 scrollbar-thin">
              <div className="max-w-6xl mx-auto w-full">
                <Routes>
                  <Route path="/" element={<DashboardPage />} />
                  <Route path="/workflows" element={<WorkflowsListPage />} />
                  <Route path="/workflows/:id" element={<WorkflowDetailPage />} />
                  <Route path="/jobs" element={<JobsPage />} />
                  <Route path="/monitoring" element={<MonitoringPage />} />
                </Routes>
              </div>
            </div>
          </main>
        </div>
        <Toaster 
          position="bottom-right"
          toastOptions={{
            className: "!bg-raised !text-zinc-200 !border !border-border !shadow-lg",
            success: { iconTheme: { primary: "#3fb668", secondary: "#161a1e" } },
            error: { iconTheme: { primary: "#e5484d", secondary: "#161a1e" } },
          }}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
}

function SidebarLink({ to, icon, label }: { to: string; icon: React.ReactNode; label: string }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `flex items-center gap-3 px-3 py-2 rounded-md transition-colors text-sm font-medium ${
          isActive
            ? "bg-accent/10 text-accent"
            : "text-zinc-400 hover:text-zinc-200 hover:bg-raised/50"
        }`
      }
    >
      {icon}
      {label}
    </NavLink>
  );
}
