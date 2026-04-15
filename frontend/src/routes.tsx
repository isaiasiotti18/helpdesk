import { createBrowserRouter, Navigate } from 'react-router-dom'
import { LoginPage } from '@/pages/LoginPage'
import { RegisterPage } from '@/pages/RegisterPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { TicketsPage } from '@/pages/TicketsPage'
import { CreateTicketPage } from '@/pages/CreateTicketPage'
import { TicketDetailPage } from '@/pages/TicketDetailPage'
import { ChatPage } from '@/pages/ChatPage'
import { CategoriesPage } from '@/pages/CategoriesPage'
import { CannedResponsesPage } from '@/pages/CannedResponsesPage'
import { MetricsDashboardPage } from '@/pages/MetricsDashboardPage'
import { ProtectedRoute } from '@/components/layout/ProtectedRoute'
import { AppLayout } from '@/components/layout/AppLayout'

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: '/', element: <DashboardPage /> },
          { path: '/tickets', element: <TicketsPage /> },
          { path: '/tickets/new', element: <CreateTicketPage /> },
          { path: '/tickets/:id', element: <TicketDetailPage /> },
          { path: '/chat/:ticketId', element: <ChatPage /> },
          { path: '/canned-responses', element: <CannedResponsesPage /> },
          { path: '/categories', element: <CategoriesPage /> },
          { path: '/metrics', element: <MetricsDashboardPage /> },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/" replace />,
  },
])