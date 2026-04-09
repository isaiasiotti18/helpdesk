import api from './client'
import type { ApiResponse, AuthResponse, User } from '@/types'

export async function loginRequest(email: string, password: string) {
  const { data } = await api.post<ApiResponse<AuthResponse>>('/auth/login', {
    email,
    password,
  })
  return data.data
}

export async function registerRequest(name: string, email: string, password: string) {
  const { data } = await api.post<ApiResponse<AuthResponse>>('/auth/register', {
    name,
    email,
    password,
  })
  return data.data
}

export async function meRequest() {
  const { data } = await api.get<ApiResponse<User>>('/auth/me')
  return data.data
}

export async function logoutRequest() {
  await api.post('/auth/logout')
}