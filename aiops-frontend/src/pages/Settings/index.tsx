import { Card, Col, Form, Input, InputNumber, Row, Select, Space, Switch, Typography } from 'antd'
import { PageHeader } from '../../components/common/PageHeader'

export default function SettingsPage() {
  return (
    <Space direction="vertical" size={24} style={{ width: '100%' }}>
      <PageHeader title="系统设置" description="集中查看 LLM 开关、阈值与命令白名单配置，当前作为 MVP 配置视图。" badge="Config" />
      <Row gutter={[16, 16]}>
        <Col xs={24} xl={12}>
          <Card className="glass-card" title="LLM 配置">
            <Form layout="vertical" initialValues={{ provider: 'ollama', openAiEnabled: false, anthropicEnabled: false }}>
              <Form.Item label="默认 Provider" name="provider"><Select options={[{ value: 'ollama' }, { value: 'openai' }, { value: 'anthropic' }]} /></Form.Item>
              <Form.Item label="启用 OpenAI" name="openAiEnabled" valuePropName="checked"><Switch /></Form.Item>
              <Form.Item label="启用 Claude" name="anthropicEnabled" valuePropName="checked"><Switch /></Form.Item>
            </Form>
          </Card>
        </Col>
        <Col xs={24} xl={12}>
          <Card className="glass-card" title="阈值与命令白名单">
            <Form layout="vertical" initialValues={{ cpuThreshold: 90, memoryThreshold: 85, commands: 'restart_service,get_logs,clear_cache' }}>
              <Form.Item label="CPU 阈值" name="cpuThreshold"><InputNumber style={{ width: '100%' }} /></Form.Item>
              <Form.Item label="内存阈值" name="memoryThreshold"><InputNumber style={{ width: '100%' }} /></Form.Item>
              <Form.Item label="白名单命令" name="commands"><Input.TextArea rows={5} /></Form.Item>
            </Form>
            <Typography.Paragraph style={{ marginBottom: 0 }}>
              后续可以直接接入后端配置接口，将当前页面改为真实可保存设置。
            </Typography.Paragraph>
          </Card>
        </Col>
      </Row>
    </Space>
  )
}
