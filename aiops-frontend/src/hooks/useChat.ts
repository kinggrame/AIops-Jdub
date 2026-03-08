import { useMutation } from '@tanstack/react-query'
import { sendAgentChat } from '../api/aiops'

export function useChat() {
  return useMutation({
    mutationFn: sendAgentChat,
  })
}
