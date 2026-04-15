import { z } from 'zod'

export const createCannedSchema = z.object({
  title: z.string().min(2, 'Mínimo 2 caracteres'),
  content: z.string().min(5, 'Mínimo 5 caracteres'),
  shortcut: z.string().optional(),
  category: z.string().optional(),
  isShared: z.boolean().optional(),
})

export type CreateCannedFormData = z.infer<typeof createCannedSchema>
