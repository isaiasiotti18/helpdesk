import { useState } from 'react'
import { Outlet, NavLink } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { useNotificationSocket } from '@/hooks/useNotificationSocket'
import { ErrorBoundary } from '@/components/layout/ErrorBoundary'
import { NotificationBell } from '@/components/layout/NotificationBell'
import { SearchBar } from '@/components/layout/SearchBar'
import { Button } from '@/components/ui/button'
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet'
import { ConfirmDialog } from '../ui/confirm-dialog'

function SidebarContent({ onNavigate }: { onNavigate?: () => void }) {
  const { user, handleLogout } = useAuth()

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `block px-4 py-2 rounded-md text-sm transition-colors ${isActive
      ? 'bg-primary text-primary-foreground'
      : 'text-muted-foreground hover:bg-muted'
    }`

  return (
    <div className="flex flex-col h-full">
      <div className="p-6 border-b">
        <h1 className="text-xl font-bold">HelpDesk</h1>
      </div>

      <nav className="flex-1 p-4 space-y-1">
        <NavLink to="/" end className={linkClass} onClick={onNavigate}>
          Dashboard
        </NavLink>
        <NavLink to="/tickets" end className={linkClass} onClick={onNavigate}>
          Tickets
        </NavLink>
        <NavLink to="/tickets/new" className={linkClass} onClick={onNavigate}>
          Novo Ticket
        </NavLink>
        {(user?.role === 'AGENT' || user?.role === 'ADMIN') && (
          <NavLink to="/canned-responses" className={linkClass} onClick={onNavigate}>
            Respostas Prontas
          </NavLink>
        )}
        {user?.role === 'ADMIN' && (
          <NavLink to="/categories" className={linkClass} onClick={onNavigate}>
            Categorias
          </NavLink>
        )}
        {user?.role === 'ADMIN' && (
          <NavLink to="/metrics" className={linkClass} onClick={onNavigate}>
            Métricas
          </NavLink>
        )}
      </nav>

      <div className="p-4 border-t">
        <div className="mb-3">
          <p className="text-sm font-medium">{user?.name}</p>
          <p className="text-xs text-muted-foreground">{user?.role}</p>
        </div>
        <ConfirmDialog
          trigger={
            <Button variant="outline" size="sm" className="w-full">
              Sair
            </Button>
          }
          title="Sair da conta?"
          description="Você será redirecionado para a tela de login."
          confirmLabel="Sair"
          confirmVariant="default"
          onConfirm={handleLogout}
        />
      </div>
    </div>
  )
}

export function AppLayout() {
  const [open, setOpen] = useState(false)
  useNotificationSocket()

  return (
    <div className="flex h-screen">
      {/* Sidebar desktop */}
      <aside className="hidden md:flex md:w-64 border-r bg-card flex-col">
        <SidebarContent />
      </aside>

      {/* Sidebar mobile */}
      <Sheet open={open} onOpenChange={setOpen}>
        <div className="flex flex-col flex-1">
          {/* Header desktop */}
          <header className="hidden md:flex items-center justify-between gap-4 px-6 py-3 border-b bg-card">
            <SearchBar />
            <NotificationBell />
          </header>

          {/* Header mobile */}
          <header className="flex md:hidden items-center gap-3 p-4 border-b bg-card">
            <SheetTrigger
              render={
                <Button variant="outline" size="icon" className="shrink-0">
                  <MenuIcon />
                </Button>
              }
            />
            <h1 className="text-lg font-bold">HelpDesk</h1>
            <div className="flex-1" />
            <SearchBar />
            <NotificationBell />
          </header>

          <SheetContent side="left" className="w-64 p-0">
            <SidebarContent onNavigate={() => setOpen(false)} />
          </SheetContent>

          <main className="flex-1 overflow-auto bg-muted/40">
            <ErrorBoundary>
              <Outlet />
            </ErrorBoundary>
          </main>
        </div>
      </Sheet>
    </div>
  )
}

function MenuIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <line x1="4" x2="20" y1="12" y2="12" />
      <line x1="4" x2="20" y1="6" y2="6" />
      <line x1="4" x2="20" y1="18" y2="18" />
    </svg>
  )
}