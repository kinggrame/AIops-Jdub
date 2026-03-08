import { Card, Col, List, Row, Space, Statistic, Table, Tag, Typography } from 'antd'
import { AsyncState } from '../../components/common/AsyncState'
import { LazyChart } from '../../components/common/LazyChart'
import { PageHeader } from '../../components/common/PageHeader'
import { MetricCard } from '../../components/common/MetricCard'
import { useDashboardData } from '../../hooks/useDashboardData'
import { formatTime, getMetricValue } from '../../utils/format'

export default function DashboardPage() {
  const { agents, alerts, commandResults, isLoading, isError, refetchAll } = useDashboardData()

  const agentList = agents.data ?? []
  const alertList = alerts.data ?? []
  const commandList = commandResults.data ?? []

  const cpuAvg = agentList.length
    ? agentList.reduce((sum, agent) => sum + (getMetricValue(agent.latestMetrics, 'cpu', 'usage') ?? 0), 0) / agentList.length
    : 0
  const memoryAvg = agentList.length
    ? agentList.reduce((sum, agent) => sum + (getMetricValue(agent.latestMetrics, 'memory', 'usage') ?? 0), 0) / agentList.length
    : 0

  const chartData = ['critical', 'warning', 'open', 'resolved'].map((item) => ({
    type: item,
    value: alertList.filter((alert) => alert.severity === item || alert.status === item).length,
  }))

  return (
    <Space direction="vertical" size={24} style={{ width: '100%' }}>
      <PageHeader title="态势总览" description="聚合在线 Agent、告警趋势与自动化命令执行状态。" badge="Live" />
      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}><MetricCard title="平均 CPU" value={cpuAvg} color="#22c55e" /></Col>
        <Col xs={24} md={8}><MetricCard title="平均内存" value={memoryAvg} color="#38bdf8" /></Col>
        <Col xs={24} md={8}>
          <Card className="glass-card metric-card">
            <Statistic title="在线 Agent" value={agentList.length} suffix="台" valueStyle={{ color: '#f59e0b' }} />
            <Typography.Text style={{ color: 'rgba(226,232,240,0.72)' }}>
              命令记录 {commandList.length} 条
            </Typography.Text>
          </Card>
        </Col>
      </Row>

      <AsyncState loading={isLoading} error={isError ? agents.error ?? alerts.error ?? commandResults.error : undefined} onRetry={refetchAll} empty={!agentList.length && !alertList.length} emptyDescription="后端尚未收到 Agent 数据，请先启动 aiops-agent。">
        <Row gutter={[16, 16]}>
          <Col xs={24} xl={14}>
            <Card className="glass-card" title="告警态势分布">
              <LazyChart data={chartData} />
            </Card>
          </Col>
          <Col xs={24} xl={10}>
            <Card className="glass-card" title="最近命令执行">
              <AsyncState empty={!commandList.length} emptyDescription="暂时还没有命令执行记录。">
                <List
                  dataSource={commandList.slice(0, 6)}
                  renderItem={(item) => (
                    <List.Item>
                      <List.Item.Meta
                        title={<Space><Tag color="processing">{item.status}</Tag><span>{item.commandId}</span></Space>}
                        description={`${item.agentId} · ${formatTime(item.timestamp)}`}
                      />
                    </List.Item>
                  )}
                />
              </AsyncState>
            </Card>
          </Col>
        </Row>

        <Card className="glass-card" title="在线 Agent 实时视图">
          <AsyncState empty={!agentList.length} emptyDescription="当前没有在线 Agent，请去客户端管理页注册或启动 agent。">
            <Table
              rowKey="agentId"
              pagination={false}
              dataSource={agentList}
              columns={[
                { title: '主机', dataIndex: 'hostname' },
                { title: 'IP', dataIndex: 'ip' },
                { title: 'CPU', render: (_, item) => `${getMetricValue(item.latestMetrics, 'cpu', 'usage') ?? 0}%` },
                { title: '内存', render: (_, item) => `${getMetricValue(item.latestMetrics, 'memory', 'usage') ?? 0}%` },
                { title: '最后心跳', dataIndex: 'lastSeen', render: formatTime },
              ]}
            />
          </AsyncState>
        </Card>
      </AsyncState>
    </Space>
  )
}
