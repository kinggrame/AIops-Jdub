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

// Connection Types
export interface ConnectionDTO {
  id: string
  name: string
  description: string
  endpoint: string
  groupName: string
  tags: string
  status: 'PENDING' | 'CONNECTED' | 'DISCONNECTED' | 'REJECTED' | 'EXPIRED'
  pairingToken: string
  authorizationToken: string
  lastConnectedAt: string
  createdAt: string
}

export interface CreateConnectionRequest {
  name: string
  description: string
  endpoint: string
  pairingToken: string
  groupName?: string
  tags?: string
}

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

export async function generatePairingToken(hostname: string, ip: string, ttlMinutes = 10) {
  const response = await apiClient.post<ApiEnvelope<{ pairingToken: string; expiresInMinutes: number; note: string }>>('/agent/pairing-token', {
    hostname,
    ip,
    ttlMinutes
  })
  return response.data.data
}

// Connection Management APIs
export async function fetchConnections(status?: string) {
  const url = status ? `/connections?status=${status}` : '/connections'
  const response = await apiClient.get<ApiEnvelope<ConnectionDTO[]>>(url)
  return response.data.data
}

export async function fetchConnection(id: string) {
  const response = await apiClient.get<ApiEnvelope<ConnectionDTO>>(`/connections/${id}`)
  return response.data.data
}

export async function createConnection(payload: CreateConnectionRequest) {
  const response = await apiClient.post<ApiEnvelope<ConnectionDTO>>('/connections', payload)
  return response.data.data
}

export async function updateConnection(id: string, payload: Partial<CreateConnectionRequest>) {
  const response = await apiClient.put<ApiEnvelope<ConnectionDTO>>(`/connections/${id}`, payload)
  return response.data.data
}

export async function deleteConnection(id: string) {
  await apiClient.delete(`/connections/${id}`)
}

export async function approveConnection(id: string) {
  const response = await apiClient.post<ApiEnvelope<ConnectionDTO>>(`/connections/${id}/approve`, {})
  return response.data.data
}

export async function rejectConnection(id: string) {
  const response = await apiClient.post<ApiEnvelope<ConnectionDTO>>(`/connections/${id}/reject`, {})
  return response.data.data
}

export async function disconnectConnection(id: string) {
  const response = await apiClient.post<ApiEnvelope<ConnectionDTO>>(`/connections/${id}/disconnect`, {})
  return response.data.data
}

export async function connectAgent(id: string) {
  const response = await apiClient.post<ApiEnvelope<ConnectionDTO>>(`/connections/${id}/connect`, {})
  return response.data.data
}

export async function testConnection(id: string) {
  const response = await apiClient.post<ApiEnvelope<boolean>>(`/connections/${id}/test`, {})
  return response.data.data
}

// Server Management APIs
export interface ServerDTO {
  id: number
  name: string
  description: string
  endpoint: string
  groupName: string
  tags: string
  status: 'ONLINE' | 'OFFLINE' | 'MAINTENANCE'
  agentId: string
  latestMetrics: Record<string, unknown>
  createdAt: string
}

export interface CreateServerRequest {
  name: string
  description?: string
  endpoint: string
  groupName?: string
  tags?: string
  agentId?: string
}

export async function fetchServers(status?: string) {
  const url = status ? `/servers?status=${status}` : '/servers'
  const response = await apiClient.get<ApiEnvelope<ServerDTO[]>>(url)
  return response.data.data
}

export async function fetchServer(id: number) {
  const response = await apiClient.get<ApiEnvelope<ServerDTO>>(`/servers/${id}`)
  return response.data.data
}

export async function createServer(payload: CreateServerRequest) {
  const response = await apiClient.post<ApiEnvelope<ServerDTO>>('/servers', payload)
  return response.data.data
}

export async function updateServer(id: number, payload: Partial<CreateServerRequest>) {
  const response = await apiClient.put<ApiEnvelope<ServerDTO>>(`/servers/${id}`, payload)
  return response.data.data
}

export async function deleteServer(id: number) {
  await apiClient.delete(`/servers/${id}`)
}

export async function fetchServerMetrics(id: number) {
  const response = await apiClient.get<ApiEnvelope<Record<string, unknown>>>(`/servers/${id}/metrics`)
  return response.data.data
}

// Alert APIs
export interface AlertRule {
  id: number
  name: string
  description: string
  metricName: string
  operator: string
  threshold: number
  duration: number
  severity: 'CRITICAL' | 'WARNING' | 'INFO'
  status: 'ENABLED' | 'DISABLED'
  serverId?: number
  createdAt: string
}

export interface Alert {
  id: number
  ruleId: number
  ruleName: string
  serverId: number
  metricName: string
  value: number
  threshold: number
  severity: 'CRITICAL' | 'WARNING' | 'INFO'
  message: string
  status: 'FIRING' | 'ACKNOWLEDGED' | 'RESOLVED'
  firedAt: string
  resolvedAt?: string
}

export interface CreateRuleRequest {
  name: string
  description?: string
  metricName: string
  operator: string
  threshold: number
  duration?: number
  severity: string
  serverId?: number
}

export async function fetchAlertRules() {
  const response = await apiClient.get<ApiEnvelope<AlertRule[]>>('/alert/rules')
  return response.data.data
}

export async function createAlertRule(payload: CreateRuleRequest) {
  const response = await apiClient.post<ApiEnvelope<AlertRule>>('/alert/rules', payload)
  return response.data.data
}

export async function deleteAlertRule(id: number) {
  await apiClient.delete(`/alert/rules/${id}`)
}

export async function enableAlertRule(id: number) {
  const response = await apiClient.post<ApiEnvelope<AlertRule>>(`/alert/rules/${id}/enable`, {})
  return response.data.data
}

export async function disableAlertRule(id: number) {
  const response = await apiClient.post<ApiEnvelope<AlertRule>>(`/alert/rules/${id}/disable`, {})
  return response.data.data
}

export async function fetchAlerts(status?: string) {
  const url = status ? `/alerts?status=${status}` : '/alerts'
  const response = await apiClient.get<ApiEnvelope<Alert[]>>(url)
  return response.data.data
}

export async function fetchFiringAlerts() {
  const response = await apiClient.get<ApiEnvelope<Alert[]>>('/alerts/firing')
  return response.data.data
}

export async function acknowledgeAlert(id: number) {
  const response = await apiClient.post<ApiEnvelope<Alert>>(`/alerts/${id}/ack`, {})
  return response.data.data
}

export async function resolveAlert(id: number, resolution?: string) {
  const response = await apiClient.post<ApiEnvelope<Alert>>(`/alerts/${id}/resolve`, resolution || '')
  return response.data.data
}
