import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { App, Button, Card, Drawer, Form, Input, InputNumber, Select, Space, Table, Tag } from 'antd'
import { useState } from 'react'
import { createAlert, fetchAlerts } from '../../api/aiops'
import { AsyncState } from '../../components/common/AsyncState'
import { PageHeader } from '../../components/common/PageHeader'
import { formatTime } from '../../utils/format'

export default function AlertsPage() {
  const { message } = App.useApp()
  const queryClient = useQueryClient()
  const [drawerOpen, setDrawerOpen] = useState(false)
  const { data: alerts = [], isLoading, isError, error, refetch } = useQuery({ 
    queryKey: ['alerts'], 
    queryFn: () => fetchAlerts() as any,
    initialData: [] 
  })
  const mutation = useMutation({
    mutationFn: createAlert,
    onSuccess: () => {
      message.success('测试告警已创建')
      queryClient.invalidateQueries({ queryKey: ['alerts'] })
    },
    onError: (err) => message.error(err instanceof Error ? err.message : '创建失败'),
  })

  return (
    <Space direction="vertical" size={24} style={{ width: '100%' }}>
      <PageHeader title="告警管理" description="查看后端自动评估产生的告警，并手工补充测试告警。" badge={`${alerts.length} Alerts`} />
      <Button type="primary" onClick={() => setDrawerOpen(true)}>新增测试告警</Button>
      <Card className="glass-card">
        <AsyncState loading={isLoading} error={isError ? error : undefined} onRetry={refetch} empty={!alerts.length} emptyDescription="当前还没有告警记录。">
          <Table
            rowKey="id"
            dataSource={alerts}
            pagination={false}
            columns={[
              { title: '主机', dataIndex: 'hostname' },
              { title: '指标', dataIndex: 'metric' },
              { title: '严重级别', dataIndex: 'severity', render: (value: string) => <Tag color={value === 'critical' ? 'red' : 'gold'}>{value}</Tag> },
              { title: '当前值', dataIndex: 'currentValue' },
              { title: '阈值', dataIndex: 'threshold' },
              { title: '状态', dataIndex: 'status', render: (value: string) => <Tag>{value}</Tag> },
              { title: '时间', dataIndex: 'createdAt', render: formatTime },
            ]}
          />
        </AsyncState>
      </Card>
      <Drawer title="新增测试告警" open={drawerOpen} onClose={() => setDrawerOpen(false)} destroyOnClose>
        <Form
          layout="vertical"
          initialValues={{ hostname: 'server-001', source: 'manual', metric: 'cpu.usage', severity: 'warning', currentValue: 86, threshold: 80, description: 'manual alert', status: 'open' }}
          onFinish={(values) => {
            mutation.mutate(values)
            setDrawerOpen(false)
          }}
        >
          <Form.Item name="hostname" label="主机" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="source" label="来源" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="metric" label="指标" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="severity" label="级别" rules={[{ required: true }]}><Select options={[{ value: 'warning' }, { value: 'critical' }]} /></Form.Item>
          <Form.Item name="currentValue" label="当前值"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="threshold" label="阈值"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="description" label="描述"><Input.TextArea rows={4} /></Form.Item>
          <Form.Item name="status" label="状态"><Select options={[{ value: 'open' }, { value: 'resolved' }]} /></Form.Item>
          <Button type="primary" htmlType="submit" loading={mutation.isPending}>创建</Button>
        </Form>
      </Drawer>
    </Space>
  )
}
