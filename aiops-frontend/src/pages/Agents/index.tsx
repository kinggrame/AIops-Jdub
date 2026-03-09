import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { App, Button, Card, Drawer, Form, Input, Select, Space, Table, Tag, Typography } from 'antd'
import { useState } from 'react'
import { fetchAgentInstallPreview, fetchAgents, fetchTunnelStatus, reportAgent, registerAgent } from '../../api/aiops'
import { AsyncState } from '../../components/common/AsyncState'
import { PageHeader } from '../../components/common/PageHeader'
import { formatTime, getMetricValue } from '../../utils/format'

export default function AgentsPage() {
  const { message } = App.useApp()
  const queryClient = useQueryClient()
  const [drawerOpen, setDrawerOpen] = useState(false)
  const { data: agents = [], isLoading, isError, error, refetch } = useQuery({ queryKey: ['agents'], queryFn: fetchAgents })
  const { data: tunnelStatus } = useQuery({ queryKey: ['tunnel-status'], queryFn: fetchTunnelStatus, refetchInterval: 5000 })
  const { data: installPreview } = useQuery({ queryKey: ['agent-install-preview'], queryFn: fetchAgentInstallPreview, refetchInterval: 5000 })

  const registerMutation = useMutation({
    mutationFn: registerAgent,
    onSuccess: () => {
      message.success('Agent 注册成功')
      queryClient.invalidateQueries({ queryKey: ['agents'] })
    },
    onError: (err) => message.error(err instanceof Error ? err.message : '注册失败'),
  })

  const reportMutation = useMutation({
    mutationFn: reportAgent,
    onSuccess: () => {
      message.success('模拟上报已发送')
      queryClient.invalidateQueries({ queryKey: ['agents'] })
      queryClient.invalidateQueries({ queryKey: ['alerts'] })
      queryClient.invalidateQueries({ queryKey: ['command-results'] })
    },
    onError: (err) => message.error(err instanceof Error ? err.message : '上报失败'),
  })

  return (
    <Space direction="vertical" size={24} style={{ width: '100%' }}>
      <PageHeader title="客户端管理" description="注册新 Agent、查看最近心跳，并快速触发一次高负载模拟上报。" badge={`${agents.length} Agents`} />
      <Card className="glass-card" title="Tunnel 状态与快速接入">
        <Space direction="vertical" size={12} style={{ width: '100%' }}>
          <Space wrap>
            <Tag color={tunnelStatus?.running ? 'green' : tunnelStatus?.enabled ? 'gold' : 'default'}>
              {tunnelStatus?.running ? 'Tunnel 运行中' : tunnelStatus?.enabled ? 'Tunnel 已启用' : 'Tunnel 未启用'}
            </Tag>
            <Tag>{tunnelStatus?.command ?? 'cloudflared'}</Tag>
          </Space>
          <Typography.Text style={{ color: '#e2e8f0' }}>Target: {tunnelStatus?.targetUrl ?? 'http://127.0.0.1:8080'}</Typography.Text>
          <Typography.Text copyable={tunnelStatus?.publicUrl ? { text: tunnelStatus.publicUrl } : undefined} style={{ color: '#e2e8f0' }}>
            Public URL: {tunnelStatus?.publicUrl ?? '暂未识别，请先启动 tunnel'}
          </Typography.Text>
          <Typography.Paragraph style={{ color: 'rgba(226,232,240,0.72)', marginBottom: 0 }}>
            {installPreview?.note ?? tunnelStatus?.message}
          </Typography.Paragraph>
          <Typography.Text strong style={{ color: '#f8fafc' }}>Windows 启动命令</Typography.Text>
          <Typography.Paragraph copyable={installPreview ? { text: installPreview.windowsCommand } : undefined} style={{ marginBottom: 0, whiteSpace: 'pre-wrap', color: '#e2e8f0' }}>
            {installPreview?.windowsCommand ?? '等待后端返回安装命令...'}
          </Typography.Paragraph>
          <Typography.Text strong style={{ color: '#f8fafc' }}>Linux 启动命令</Typography.Text>
          <Typography.Paragraph copyable={installPreview ? { text: installPreview.linuxCommand } : undefined} style={{ marginBottom: 0, whiteSpace: 'pre-wrap', color: '#e2e8f0' }}>
            {installPreview?.linuxCommand ?? '等待后端返回安装命令...'}
          </Typography.Paragraph>
        </Space>
      </Card>
      <Space>
        <Button type="primary" onClick={() => setDrawerOpen(true)}>注册 Agent</Button>
      </Space>
      <Card className="glass-card">
        <AsyncState loading={isLoading} error={isError ? error : undefined} onRetry={refetch} empty={!agents.length} emptyDescription="还没有注册任何 Agent。">
          <Table
            rowKey="agentId"
            dataSource={agents}
            pagination={false}
            columns={[
              { title: '主机名', dataIndex: 'hostname' },
              { title: 'IP', dataIndex: 'ip' },
              { title: '能力', dataIndex: 'capabilities', render: (value: string[]) => value.map((item) => <Tag key={item}>{item}</Tag>) },
              { title: 'CPU', render: (_, item) => `${getMetricValue(item.latestMetrics, 'cpu', 'usage') ?? 0}%` },
              { title: '内存', render: (_, item) => `${getMetricValue(item.latestMetrics, 'memory', 'usage') ?? 0}%` },
              { title: '最后心跳', dataIndex: 'lastSeen', render: formatTime },
              {
                title: '操作',
                render: (_, item) => (
                  <Button
                    loading={reportMutation.isPending}
                    onClick={() => reportMutation.mutate({
                      agentId: item.agentId,
                      hostname: item.hostname,
                      metrics: { cpu: { usage: 95 }, memory: { usage: 88 } },
                      events: [{ type: 'threshold', metric: 'cpu.usage', value: 95, target: 'ai' }],
                    })}
                  >
                    模拟高负载上报
                  </Button>
                ),
              },
            ]}
          />
        </AsyncState>
      </Card>

      <Drawer title="注册 Agent" open={drawerOpen} onClose={() => setDrawerOpen(false)} destroyOnClose>
        <Form
          layout="vertical"
          initialValues={{ hostname: 'server-001', ip: '10.0.0.1', token: 'aiops-mvp-seed-demo-token', capabilities: ['cpu', 'memory', 'disk'] }}
          onFinish={(values) => {
            registerMutation.mutate(values)
            setDrawerOpen(false)
          }}
        >
          <Form.Item label="主机名" name="hostname" rules={[{ required: true, message: '请输入主机名' }]}><Input /></Form.Item>
          <Form.Item label="IP" name="ip" rules={[{ required: true, message: '请输入 IP' }]}><Input /></Form.Item>
          <Form.Item label="Token" name="token" rules={[{ required: true, message: '请输入 Token' }]}><Input /></Form.Item>
          <Form.Item label="能力" name="capabilities" rules={[{ required: true, message: '请至少选择一个能力' }]}>
            <Select mode="tags" options={[{ value: 'cpu' }, { value: 'memory' }, { value: 'disk' }]} />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={registerMutation.isPending}>提交</Button>
        </Form>
        <Typography.Paragraph style={{ marginTop: 16 }}>
          注册后可在列表中直接发起一次模拟上报，观察后端闭环状态变化。
        </Typography.Paragraph>
      </Drawer>
    </Space>
  )
}
