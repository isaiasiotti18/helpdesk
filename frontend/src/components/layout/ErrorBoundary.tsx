import { Component, type ReactNode } from 'react'
import { Button } from '@/components/ui/button'

interface Props {
  children: ReactNode
  fallback?: ReactNode
}

interface State {
  hasError: boolean
  error: Error | null
}

// O que faz: captura qualquer erro de rendering nos componentes filhos. 
// Mostra uma tela de fallback ao invés de tela branca. Botão "Tentar novamente" reseta o estado e re-renderiza.
// Error boundaries precisam ser class components — React não suporta getDerivedStateFromError em hooks.

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null })
  }

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) return this.props.fallback

      return (
        <div className="flex flex-col items-center justify-center min-h-[400px] gap-4 p-8">
          <h2 className="text-xl font-semibold">Algo deu errado</h2>
          <p className="text-sm text-muted-foreground max-w-md text-center">
            {this.state.error?.message ?? 'Erro inesperado na aplicação.'}
          </p>
          <div className="flex gap-3">
            <Button onClick={this.handleReset}>Tentar novamente</Button>
            <Button variant="outline" onClick={() => window.location.href = '/'}>
              Voltar ao início
            </Button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}