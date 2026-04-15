import { z } from 'zod'

export const createCategorySchema = z.object({
  name: z.string().min(2, 'Mínimo 2 caracteres'),
  description: z.string().optional(),
  queueId: z.string().optional(),
})

export type CreateCategoryFormData = z.infer<typeof createCategorySchema>
