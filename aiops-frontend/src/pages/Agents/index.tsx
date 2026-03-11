import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { App, Button, Card, Drawer, Form, Input, Space, Table, Tag, Typography } from 'antd'
import { useState } from 'react'
import { fetchAgentInstallPreview, fetchAgents, fetchTunnelStatus, generatePairingToken, reportAgent } from '../../api/aiops'
import { AsyncState } from '../../components/common/AsyncState'
import { PageHeader } from '../../components/common/PageHeader'
import { formatTime, getMetricValue } from '../../utils/format'

export default function AgentsPage() {
  const { message } = App.useApp()
  const queryClient = useQueryClient()
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [pairingData, setPairingData] = useState<{ pairingToken: string; serverUrl: string } | null>(null)
  const { data: agents = [], isLoading, isError, error, refetch } = useQuery({ queryKey: ['agents'], queryFn: fetchAgents })
  const { data: tunnelStatus } = useQuery({ queryKey: ['tunnel-status'], queryFn: fetchTunnelStatus, refetchInterval: 5000 })
  const { data: installPreview } = useQuery({ queryKey: ['agent-install-preview'], queryFn: fetchAgentInstallPreview, refetchInterval: 5000 })

  const generateMutation = useMutation({
    mutationFn: async ({ hostname, ip }: { hostname: string; ip: string }) => {
      const result = await generatePairingToken(hostname, ip, 30)
      const serverUrl = tunnelStatus?.publicUrl || 'http://localhost:8080'
      return { pairingToken: result.pairingToken, serverUrl }
    },
    onSuccess: (data) => {
      setPairingData(data)
      message.success('配对 Token 已生成')
    },
    onError: (err) => message.error(err instanceof Error ? err.message : '生成配对 Token 失败'),
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

  const getWindowsCommand = (token: string, serverUrl: string) => 
    `set AIOPS_PAIRING_TOKEN=${token} && set AIOPS_SERVER_URL=${serverUrl} && cd aiops-agent && go run ./cmd -c config.yaml`

  const getLinuxCommand = (token: string, serverUrl: string) =>
    `export AIOPS_PAIRING_TOKEN='${token}' && export AIOPS_SERVER_URL='${serverUrl}' && cd aiops-agent && go run ./cmd -c config.yaml`

  return (
    <Space direction="vertical" size={24} style={{ width: '100%' }}>
      <PageHeader title="客户端管理" description="生成配对 Token 并复制到目标服务器启动 Agent。" badge={`${agents.length} Agents`} />
      <Card className="glass-card" title="Tunnel 状态与接入地址">
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
        </Space>
      </Card>
      <Space>
        <Button type="primary" onClick={() => { setPairingData(null); setDrawerOpen(true); }}>生成配对 Token</Button>
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

      <Drawer title="生成配对 Token" open={drawerOpen} onClose={() => setDrawerOpen(false)} destroyOnClose width={500}>
        {pairingData ? (
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Typography.Paragraph type="success">
              配对 Token 已生成，请在 30 分钟内使用！
            </Typography.Paragraph>
            <Typography.Text strong>配对 Token</Typography.Text>
            <Typography.Paragraph copyable style={{ background: '#f5f5f5', padding: 8, fontFamily: 'monospace' }}>
              {pairingData.pairingToken}
            </Typography.Paragraph>
            <Typography.Text strong>Windows 启动命令</Typography.Text>
            <Typography.Paragraph copyable style={{ background: '#f5f5f5', padding: 8, whiteSpace: 'pre-wrap', fontSize: 12 }}>
              {getWindowsCommand(pairingData.pairingToken, pairingData.serverUrl)}
            </Typography.Paragraph>
            <Typography.Text strong>Linux 启动命令</Typography.Text>
            <Typography.Paragraph copyable style={{ background: '#f5f5f5', padding: 8, whiteSpace: 'pre-wrap', fontSize: 12 }}>
              {getLinuxCommand(pairingData.pairingToken, pairingData.serverUrl)}
            </Typography.Paragraph>
            <Button block onClick={() => setDrawerOpen(false)}>关闭</Button>
          </Space>
        ) : (
          <Form
            layout="vertical"
            initialValues={{ hostname: 'server-001', ip: '127.0.0.1' }}
            onFinish={(values) => {
              generateMutation.mutate(values)
            }}
          >
            <Form.Item label="目标主机名" name="hostname" rules={[{ required: true, message: '请输入主机名' }]}>
              <Input placeholder="例如: web-server-01" />
            </Form.Item>
            <Form.Item label="目标 IP" name="ip" rules={[{ required: true, message: '请输入 IP' }]}>
              <Input placeholder="例如: 192.168.1.100" />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={generateMutation.isPending} block>
              生成配对 Token
            </Button>
          </Form>
        )}
        <Typography.Paragraph type="secondary" style={{ marginTop: 16 }}>
          说明：生成配对 Token 后，将启动命令复制到目标服务器上运行。Agent 会使用此 Token 完成注册，并自动获取长期访问凭证。
        </Typography.Paragraph>
      </Drawer>
    </Space>
  )
}
