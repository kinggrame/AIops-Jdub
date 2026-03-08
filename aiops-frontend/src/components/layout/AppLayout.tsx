import { AlertOutlined, DashboardOutlined, DatabaseOutlined, MessageOutlined, RobotOutlined, SettingOutlined } from '@ant-design/icons'
import { Button, Layout, Menu, Space, Tag, Typography } from 'antd'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '../../store'

const { Header, Sider, Content } = Layout

const items = [
  { key: '/', icon: <DashboardOutlined />, label: <Link to="/">态势总览</Link> },
  { key: '/agents', icon: <RobotOutlined />, label: <Link to="/agents">客户端管理</Link> },
  { key: '/agent', icon: <MessageOutlined />, label: <Link to="/agent">Agent 对话</Link> },
  { key: '/alerts', icon: <AlertOutlined />, label: <Link to="/alerts">告警管理</Link> },
  { key: '/knowledge', icon: <DatabaseOutlined />, label: <Link to="/knowledge">知识检索</Link> },
  { key: '/settings', icon: <SettingOutlined />, label: <Link to="/settings">系统设置</Link> },
]

export function AppLayout() {
  const location = useLocation()
  const { username, logout } = useAuthStore()

  return (
    <Layout className="app-shell">
      <Sider width={248} className="app-sider">
        <div className="brand-block">
          <Tag color="cyan">AIOps MVP</Tag>
          <Typography.Title level={3} style={{ color: '#f8fafc', margin: 0 }}>
            智能运维中枢
          </Typography.Title>
          <Typography.Text style={{ color: 'rgba(226,232,240,0.72)' }}>
            Observe. Diagnose. Dispatch.
          </Typography.Text>
        </div>
        <Menu theme="dark" mode="inline" selectedKeys={[location.pathname]} items={items} className="app-menu" />
      </Sider>
      <Layout>
        <Header className="app-header">
          <Space style={{ width: '100%', justifyContent: 'space-between' }}>
            <Space direction="vertical" size={0}>
              <Typography.Text style={{ color: '#94a3b8' }}>AIOps Platform</Typography.Text>
              <Typography.Title level={4} style={{ color: '#f8fafc', margin: 0 }}>
                实时可观测与自动响应面板
              </Typography.Title>
            </Space>
            <Space>
              <Tag color="processing">在线</Tag>
              <Typography.Text style={{ color: '#e2e8f0' }}>{username}</Typography.Text>
              <Button onClick={logout}>退出</Button>
            </Space>
          </Space>
        </Header>
        <Content className="app-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
