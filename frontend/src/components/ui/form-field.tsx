import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import type { UseFormRegisterReturn, FieldError } from 'react-hook-form'
import type { InputHTMLAttributes } from 'react'

interface FormFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  registration: UseFormRegisterReturn
  error?: FieldError
}

export function FormField({ label, registration, error, ...props }: FormFieldProps) {
  return (
    <div className="space-y-2">
      <Label htmlFor={registration.name}>{label}</Label>
      <Input id={registration.name} {...registration} {...props} />
      {error && <p className="text-sm text-destructive">{error.message}</p>}
    </div>
  )
}