import { z } from 'zod'

export const createTicketSchema = z.object({
  title: z.string().min(3, 'Mínimo 3 caracteres'),
  description: z.string().optional(),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH', 'URGENT'], {
    required_error: 'Selecione a prioridade',
  }),
  categoryId: z.string().optional(),
})

export type CreateTicketFormData = z.infer<typeof createTicketSchema>