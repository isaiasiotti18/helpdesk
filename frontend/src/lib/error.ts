import axios from 'axios'

export function getApiError(error: unknown): string {
    if (axios.isAxiosError(error)) {
        const message = error.response?.data?.error
        if (message && typeof message === 'string') return message

        if (error.response?.status === 403) return 'Acesso negado'
        if (error.response?.status === 404) return 'Não encontrado'
        if (error.response?.status === 409) return 'Conflito — tente novamente'
        if (error.response?.status === 429) return 'Muitas tentativas — aguarde'
    }
    return 'Erro inesperado'
}