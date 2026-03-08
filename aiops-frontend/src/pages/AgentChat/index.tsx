import { useQuery } from '@tanstack/react-query'
import { App, Button, Card, Col, Empty, Form, Input, Row, Select, Space, Tag, Typography } from 'antd'
import { useState } from 'react'
import { fetchAgents } from '../../api/aiops'
import { AsyncState } from '../../components/common/AsyncState'
import { PageHeader } from '../../components/common/PageHeader'
import { useChat } from '../../hooks/useChat'

export default function AgentChatPage() {
  const { message } = App.useApp()
  const { data: agents = [], isLoading, isError, error, refetch } = useQuery({ queryKey: ['agents'], queryFn: fetchAgents })
  const chatMutation = useChat()
  const [messages, setMessages] = useState<Array<{ role: 'user' | 'assistant'; content: string; badge?: string }>>([
    { role: 'assistant', content: '你好，我已经接入分析、知识检索与命令建议链路。' },
  ])

  return (
    <Space direction="vertical" size={24} style={{ width: '100%' }}>
      <PageHeader title="Agent 对话" description="选择 Agent 类型和目标服务器，对后端分析服务发起运维问答。" badge="Analysis" />
      <Row gutter={[16, 16]}>
        <Col xs={24} xl={16}>
          <Card className="glass-card chat-card" title="对话流">
            <AsyncState loading={isLoading} error={isError ? error : undefined} onRetry={refetch} empty={!messages.length} emptyDescription="还没有对话消息。">
              <Space direction="vertical" size={16} style={{ width: '100%' }}>
                {messages.map((item, index) => (
                  <div key={`${item.role}-${index}`} className={`message-bubble ${item.role}`}>
                    <Space direction="vertical" size={4}>
                      <Space>
                        <Tag color={item.role === 'assistant' ? 'cyan' : 'gold'}>{item.role === 'assistant' ? 'Agent' : 'User'}</Tag>
                        {item.badge ? <Tag>{item.badge}</Tag> : null}
                      </Space>
                      <Typography.Text style={{ color: '#e2e8f0' }}>{item.content}</Typography.Text>
                    </Space>
                  </div>
                ))}
              </Space>
            </AsyncState>
          </Card>
        </Col>
        <Col xs={24} xl={8}>
          <Card className="glass-card" title="发起分析">
            <Form
              layout="vertical"
              initialValues={{ agentType: 'analysis', message: 'cpu usage is high', targetAgent: agents[0]?.agentId }}
              onFinish={async (values) => {
                if (!values.targetAgent) {
                  message.warning('请先选择目标服务器')
                  return
                }
                setMessages((current) => [...current, { role: 'user', content: values.message, badge: values.agentType }])
                const result = await chatMutation.mutateAsync({
                  agentType: values.agentType,
                  message: values.message,
                  metrics: { agentId: values.targetAgent, cpu: { usage: 92 }, memory: { usage: 73 } },
                  events: [],
                })
                setMessages((current) => [...current, { role: 'assistant', content: result.reply, badge: result.provider }])
                message.success('已收到分析结果')
              }}
            >
              <Form.Item label="Agent 类型" name="agentType" rules={[{ required: true, message: '请选择 Agent 类型' }]}><Select options={[{ value: 'data' }, { value: 'analysis' }, { value: 'report' }]} /></Form.Item>
              <Form.Item label="目标服务器" name="targetAgent" rules={[{ required: true, message: '请选择目标服务器' }]}>
                <Select notFoundContent={<Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无可选 Agent" />} options={agents.map((agent) => ({ value: agent.agentId, label: agent.hostname }))} />
              </Form.Item>
              <Form.Item label="消息" name="message" rules={[{ required: true, message: '请输入消息内容' }]}><Input.TextArea rows={6} /></Form.Item>
              <Button type="primary" htmlType="submit" block loading={chatMutation.isPending}>发送</Button>
            </Form>
          </Card>
        </Col>
      </Row>
    </Space>
  )
}
