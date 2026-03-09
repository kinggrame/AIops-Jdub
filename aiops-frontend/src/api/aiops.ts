import apiClient from './index'
import type {
  AgentInfo,
  ApprovalItem,
  AlertItem,
  ApiEnvelope,
  ChatRequest,
  ChatResponse,
  CommandResult,
  KnowledgeItem,
  LogEntry,
  ReportResult,
  TunnelStatus,
  AgentInstallPreview,
} from '../types'

export async function fetchAgents() {
  const response = await apiClient.get<ApiEnvelope<AgentInfo[]>>('/agent/clients')
  return response.data.data
}

export async function fetchAlerts() {
  const response = await apiClient.get<ApiEnvelope<AlertItem[]>>('/alerts')
  return response.data.data
}

export async function fetchCommandResults() {
  const response = await apiClient.get<ApiEnvelope<CommandResult[]>>('/agent/command/results')
  return response.data.data
}

export async function searchKnowledge(query: string, topK = 5) {
  const response = await apiClient.post<ApiEnvelope<KnowledgeItem[]>>('/knowledge/search', { query, topK })
  return response.data.data
}

export async function searchLogs(query: string, limit = 8) {
  const response = await apiClient.post<ApiEnvelope<LogEntry[]>>('/metrics/logs/search', { query, limit })
  return response.data.data
}

export async function sendAgentChat(payload: ChatRequest) {
  const response = await apiClient.post<ApiEnvelope<ChatResponse>>('/agent/chat', payload)
  return response.data.data
}

export async function fetchApprovals() {
  const response = await apiClient.get<ApiEnvelope<ApprovalItem[]>>('/approvals')
  return response.data.data
}

export async function decideApproval(payload: { approvalId: string; reviewer: string; decision: 'approve' | 'reject' }) {
  const response = await apiClient.post<ApiEnvelope<unknown>>('/approvals/decision', payload)
  return response.data.data
}

export async function createAlert(payload: Omit<AlertItem, 'id' | 'createdAt'>) {
  const response = await apiClient.post<ApiEnvelope<AlertItem>>('/alerts', payload)
  return response.data.data
}

export async function registerAgent(payload: {
  hostname: string
  ip: string
  token: string
  capabilities: string[]
}) {
  const response = await apiClient.post<ApiEnvelope<AgentInfo>>('/agent/register', payload)
  return response.data.data
}

export async function reportAgent(payload: {
  agentId: string
  hostname: string
  metrics: Record<string, unknown>
  events: Array<Record<string, unknown>>
}) {
  const response = await apiClient.post<ApiEnvelope<ReportResult>>('/agent/report', payload)
  return response.data.data
}

export async function fetchTunnelStatus() {
  const response = await apiClient.get<ApiEnvelope<TunnelStatus>>('/metrics/tunnel')
  return response.data.data
}

export async function fetchAgentInstallPreview() {
  const response = await apiClient.get<ApiEnvelope<AgentInstallPreview>>('/metrics/tunnel/install-preview')
  return response.data.data
}
