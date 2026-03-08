import { Suspense, lazy } from 'react'
import { Spin } from 'antd'
import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/layout/AppLayout'
import { useAuthStore } from './store'

const LoginPage = lazy(() => import('./pages/Login'))
const DashboardPage = lazy(() => import('./pages/Dashboard'))
const AgentsPage = lazy(() => import('./pages/Agents'))
const AgentChatPage = lazy(() => import('./pages/AgentChat'))
const AlertsPage = lazy(() => import('./pages/Alerts'))
const KnowledgePage = lazy(() => import('./pages/Knowledge'))
const SettingsPage = lazy(() => import('./pages/Settings'))

function PageLoader() {
  return <Spin size="large" style={{ display: 'flex', justifyContent: 'center', padding: 80 }} />
}

function ProtectedRoutes() {
  const loggedIn = useAuthStore((state) => state.loggedIn)

  if (!loggedIn) {
    return <Navigate to="/login" replace />
  }

  return (
    <Suspense fallback={<PageLoader />}>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/agents" element={<AgentsPage />} />
          <Route path="/agent" element={<AgentChatPage />} />
          <Route path="/alerts" element={<AlertsPage />} />
          <Route path="/knowledge" element={<KnowledgePage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Route>
      </Routes>
    </Suspense>
  )
}

export default function App() {
  return (
    <Suspense fallback={<PageLoader />}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/*" element={<ProtectedRoutes />} />
      </Routes>
    </Suspense>
  )
}
