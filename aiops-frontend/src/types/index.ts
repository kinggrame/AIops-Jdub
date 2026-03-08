export interface ApiEnvelope<T> {
  success: boolean;
  data: T;
  message: string;
  timestamp: string;
}

export interface AgentInfo {
  agentId: string;
  hostname: string;
  ip: string;
  token: string;
  capabilities: string[];
  registeredAt: string;
  lastSeen: string;
  latestMetrics: Record<string, unknown>;
}

export interface AlertItem {
  id: string;
  hostname: string;
  source: string;
  metric: string;
  severity: string;
  currentValue: number;
  threshold: number;
  description: string;
  status: string;
  createdAt: string;
}

export interface KnowledgeItem {
  id: string;
  title: string;
  content: string;
  category: string;
  keywords: string[];
  score: number;
}

export interface ChatRequest {
  conversationId?: string;
  agentType: 'data' | 'analysis' | 'report';
  message: string;
  metrics?: Record<string, unknown>;
  events?: Array<Record<string, unknown>>;
}

export interface ChatResponse {
  conversationId: string;
  agentType: string;
  provider: string;
  reply: string;
  details: Record<string, unknown>;
}

export interface CommandDispatch {
  commandId: string;
  agentId: string;
  action: string;
  params: Record<string, unknown>;
  status: string;
  createdAt: string;
}

export interface ReportResult {
  agentId: string;
  stored: boolean;
  alerts: AlertItem[];
  status: string;
  analysis: Record<string, unknown>;
  command?: CommandDispatch | null;
}

export interface CommandResult {
  commandId: string;
  agentId: string;
  status: string;
  output: string;
  timestamp: string;
}

export interface LogEntry {
  id: string;
  agentId: string;
  hostname: string;
  level: string;
  message: string;
  metadata: Record<string, unknown>;
  timestamp: string;
}
