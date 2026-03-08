import { LockOutlined, UserOutlined } from '@ant-design/icons'
import { Button, Card, Form, Input, Space, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store'

export default function LoginPage() {
  const navigate = useNavigate()
  const login = useAuthStore((state) => state.login)

  return (
    <div className="login-screen">
      <Card className="login-card">
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <div>
            <Typography.Title level={2}>智能运维平台</Typography.Title>
            <Typography.Paragraph>
              登录后进入统一运维中枢，查看 Agent、告警、分析与知识检索结果。
            </Typography.Paragraph>
          </div>
          <Form
            layout="vertical"
            initialValues={{ username: 'admin', password: 'admin123' }}
            onFinish={(values) => {
              login(values.username)
              navigate('/')
            }}
          >
            <Form.Item label="用户名" name="username" rules={[{ required: true }]}>
              <Input prefix={<UserOutlined />} />
            </Form.Item>
            <Form.Item label="密码" name="password" rules={[{ required: true }]}>
              <Input.Password prefix={<LockOutlined />} />
            </Form.Item>
            <Button type="primary" htmlType="submit" block size="large">
              登录控制台
            </Button>
          </Form>
        </Space>
      </Card>
    </div>
  )
}
