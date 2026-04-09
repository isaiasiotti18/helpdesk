import { useNavigate } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/stores/authStore'
import { loginRequest, registerRequest, meRequest, logoutRequest } from '@/api/auth'
import { disconnectWebSocket } from '@/lib/websocket'
import { toast } from 'sonner'

export function useAuth() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { isAuthenticated, login, logout, setUser } = useAuthStore()

  const { data: user } = useQuery({
    queryKey: ['auth', 'me'],
    queryFn: async () => {
      try {
        const user = await meRequest()
        setUser(user)
        return user
      } catch {
        logout()
        return null
      }
    },
    enabled: isAuthenticated,
    retry: false,
  })

  async function handleLogin(email: string, password: string) {
    const response = await loginRequest(email, password)
    login(response.user, response.accessToken, response.refreshToken)
    queryClient.setQueryData(['auth', 'me'], response.user)
    toast.success(`Bem-vindo, ${response.user.name}`)
    navigate('/')
  }


  async function handleRegister(name: string, email: string, password: string) {
    const response = await registerRequest(name, email, password)
    login(response.user, response.accessToken, response.refreshToken)
    queryClient.setQueryData(['auth', 'me'], response.user)
    toast.success('Conta criada com sucesso')
    navigate('/')
  }

  async function handleLogout() {
    try {
      await logoutRequest() // revoga refresh tokens no backend
    } catch {
      // ignora erro — pode ser token expirado
    }
    disconnectWebSocket()
    logout()
    queryClient.clear()
    navigate('/login')
  }

  return {
    user: user ?? null,
    isAuthenticated,
    handleLogin,
    handleRegister,
    handleLogout,
  }
}