import { useDashboardMetrics } from '@/hooks/useDashboardMetrics'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { EmptyState } from '@/components/layout/EmptyState'

function StatCard({ label, value, variant }: { label: string; value: string | number; variant?: string }) {
  return (
    <Card>
      <CardContent className="p-4">
        <p className="text-sm text-muted-foreground">{label}</p>
        <p className={`text-2xl font-bold mt-1 ${variant === 'danger' ? 'text-destructive' : ''}`}>
          {value}
        </p>
      </CardContent>
    </Card>
  )
}

function formatMinutes(minutes: number | null): string {
  if (minutes === null) return '—'
  if (minutes < 60) return `${Math.round(minutes)}min`
  const hours = Math.floor(minutes / 60)
  const mins = Math.round(minutes % 60)
  return `${hours}h ${mins}min`
}

export function MetricsDashboardPage() {
  const { data, isLoading } = useDashboardMetrics()

  if (isLoading) return <LoadingSpinner />
  if (!data) return <EmptyState title="Sem dados" description="Nenhuma métrica disponível ainda." />

  const { ticketCounts, performance, agentLoad, ticketsPerDay } = data

  return (
    <div className="p-6 space-y-8">
      <h1 className="text-2xl font-bold">Dashboard de Métricas</h1>

      {/* KPIs */}
      <section>
        <h2 className="text-lg font-semibold mb-4">Visão Geral</h2>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          <StatCard label="Abertos" value={ticketCounts.open} />
          <StatCard label="Em Atendimento" value={ticketCounts.inProgress} />
          <StatCard label="Resolvidos" value={ticketCounts.resolved} />
          <StatCard label="Fechados" value={ticketCounts.closed} />
          <StatCard label="SLA Estourado" value={ticketCounts.slaBbreached} variant="danger" />
          <StatCard label="Total" value={ticketCounts.total} />
        </div>
      </section>

      {/* Performance */}
      <section>
        <h2 className="text-lg font-semibold mb-4">Performance</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card>
            <CardContent className="p-4">
              <p className="text-sm text-muted-foreground">Tempo Médio 1ª Resposta</p>
              <p className="text-2xl font-bold mt-1">
                {formatMinutes(performance.avgFirstResponseMinutes)}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <p className="text-sm text-muted-foreground">Tempo Médio Resolução</p>
              <p className="text-2xl font-bold mt-1">
                {formatMinutes(performance.avgResolutionMinutes)}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <p className="text-sm text-muted-foreground">SLA Compliance</p>
              <p className={`text-2xl font-bold mt-1 ${
                performance.slaCompliancePercent !== null && performance.slaCompliancePercent < 80
                  ? 'text-destructive'
                  : 'text-emerald-600'
              }`}>
                {performance.slaCompliancePercent !== null
                  ? `${performance.slaCompliancePercent.toFixed(1)}%`
                  : '—'}
              </p>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Agent Load */}
      <section>
        <h2 className="text-lg font-semibold mb-4">Carga por Agente</h2>
        {agentLoad.length === 0 ? (
          <p className="text-sm text-muted-foreground">Nenhum agente com tickets atribuídos.</p>
        ) : (
          <div className="space-y-2">
            {agentLoad.map((agent) => (
              <Card key={agent.agentId}>
                <CardContent className="p-4 flex items-center justify-between">
                  <div>
                    <p className="font-medium">{agent.agentName}</p>
                    <p className="text-xs text-muted-foreground">
                      {agent.resolvedToday} resolvido(s) hoje
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-32 h-2 bg-muted rounded-full overflow-hidden">
                      <div
                        className="h-full bg-primary rounded-full transition-all"
                        style={{ width: `${Math.min(agent.activeTickets * 20, 100)}%` }}
                      />
                    </div>
                    <Badge variant={agent.activeTickets >= 5 ? 'destructive' : 'secondary'}>
                      {agent.activeTickets} ativo(s)
                    </Badge>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </section>

      {/* Tickets per Day (simple bar chart with CSS) */}
      <section>
        <h2 className="text-lg font-semibold mb-4">Tickets por Dia (últimos 30 dias)</h2>
        <Card>
          <CardContent className="p-4">
            <div className="flex items-end gap-1 h-40">
              {ticketsPerDay.map((day) => {
                const maxCount = Math.max(...ticketsPerDay.map(d => d.count), 1)
                const heightPercent = (day.count / maxCount) * 100

                return (
                  <div key={day.date} className="flex-1 flex flex-col items-center gap-1">
                    <span className="text-xs text-muted-foreground">
                      {day.count > 0 ? day.count : ''}
                    </span>
                    <div
                      className="w-full bg-primary/80 rounded-t transition-all hover:bg-primary"
                      style={{ height: `${Math.max(heightPercent, 2)}%` }}
                      title={`${day.date}: ${day.count} tickets`}
                    />
                  </div>
                )
              })}
            </div>
            <div className="flex justify-between mt-2">
              <span className="text-xs text-muted-foreground">
                {ticketsPerDay[0]?.date.slice(5)}
              </span>
              <span className="text-xs text-muted-foreground">
                {ticketsPerDay[ticketsPerDay.length - 1]?.date.slice(5)}
              </span>
            </div>
          </CardContent>
        </Card>
      </section>
    </div>
  )
}
