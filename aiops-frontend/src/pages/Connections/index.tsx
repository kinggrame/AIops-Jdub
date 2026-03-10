import { useState, useEffect } from 'react'
import { Button, Card, Table, Tag, Space, Modal, Form, Input, Select, message, Popconfirm } from 'antd'
import { PageHeader } from '../../components/common/PageHeader'
import { fetchConnections, createConnection, deleteConnection, approveConnection, rejectConnection, connectAgent, testConnection, type ConnectionDTO, type CreateConnectionRequest } from '../../api/aiops'

const statusColors: Record<string, string> = {
  PENDING: 'orange',
  CONNECTED: 'green',
  DISCONNECTED: 'gray',
  REJECTED: 'red',
  EXPIRED: 'red'
}

const statusText: Record<string, string> = {
  PENDING: '待配对',
  CONNECTED: '已连接',
  DISCONNECTED: '已断开',
  REJECTED: '已拒绝',
  EXPIRED: '已过期'
}

export default function ConnectionsPage() {
  const [connections, setConnections] = useState<ConnectionDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [form] = Form.useForm()
  const [statusFilter, setStatusFilter] = useState<string>('')

  const loadConnections = async () => {
    setLoading(true)
    try {
      const data = await fetchConnections(statusFilter || undefined)
      setConnections(data || [])
    } catch (error) {
      message.error('加载连接列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadConnections()
  }, [statusFilter])

  const handleAdd = async (values: CreateConnectionRequest) => {
    try {
      await createConnection(values)
      message.success('添加成功')
      setModalVisible(false)
      form.resetFields()
      loadConnections()
    } catch (error) {
      message.error('添加失败')
    }
  }

  const handleDelete = async (id: string) => {
    try {
      await deleteConnection(id)
      message.success('删除成功')
      loadConnections()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleApprove = async (id: string) => {
    try {
      await approveConnection(id)
      message.success('审批通过')
      loadConnections()
    } catch (error) {
      message.error('操作失败')
    }
  }

  const handleReject = async (id: string) => {
    try {
      await rejectConnection(id)
      message.success('已拒绝')
      loadConnections()
    } catch (error) {
      message.error('操作失败')
    }
  }

  const handleConnect = async (id: string) => {
    try {
      await connectAgent(id)
      message.success('连接成功')
      loadConnections()
    } catch (error) {
      message.error('连接失败')
    }
  }

  const handleTest = async (id: string) => {
    try {
      const result = await testConnection(id)
      if (result) {
        message.success('连接测试成功')
      } else {
        message.error('连接测试失败')
      }
    } catch (error) {
      message.error('连接测试失败')
    }
  }

  const columns = [
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: '地址', dataIndex: 'endpoint', key: 'endpoint' },
    { title: '分组', dataIndex: 'groupName', key: 'groupName' },
    { 
      title: '状态', 
      dataIndex: 'status', 
      key: 'status',
      render: (status: string) => <Tag color={statusColors[status]}>{statusText[status]}</Tag>
    },
    { 
      title: '最后连接', 
      dataIndex: 'lastConnectedAt', 
      key: 'lastConnectedAt',
      render: (text: string) => text ? text.slice(0, 19).replace('T', ' ') : '-'
    },
    {
      title: '操作',
      key: 'action',
      render: (_: unknown, record: ConnectionDTO) => (
        <Space size="small">
          {record.status === 'PENDING' && (
            <>
              <Button type="link" size="small" onClick={() => handleConnect(record.id)}>配对</Button>
              <Button type="link" size="small" onClick={() => handleApprove(record.id)}>通过</Button>
              <Button type="link" size="small" danger onClick={() => handleReject(record.id)}>拒绝</Button>
            </>
          )}
          {record.status === 'CONNECTED' && (
            <>
              <Button type="link" size="small" onClick={() => handleTest(record.id)}>测试</Button>
              <Popconfirm title="确认断开?" onConfirm={() => handleDelete(record.id)}>
                <Button type="link" size="small" danger>断开</Button>
              </Popconfirm>
            </>
          )}
          <Popconfirm title="确认删除?" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <PageHeader 
          title="连接管理" 
          description="管理目标服务器Agent连接" 
        />
        <Button type="primary" onClick={() => setModalVisible(true)}>+ 添加Agent</Button>
      </div>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Select
            placeholder="筛选状态"
            allowClear
            style={{ width: 120 }}
            value={statusFilter}
            onChange={setStatusFilter}
            options={[
              { value: '', label: '全部' },
              { value: 'PENDING', label: '待配对' },
              { value: 'CONNECTED', label: '已连接' },
              { value: 'DISCONNECTED', label: '已断开' },
              { value: 'REJECTED', label: '已拒绝' },
            ]}
          />
        </Space>

        <Table
          columns={columns}
          dataSource={connections}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      <Modal
        title="添加Agent"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleAdd}>
          <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}>
            <Input placeholder="如: Web服务器1" />
          </Form.Item>
          <Form.Item name="endpoint" label="地址" rules={[{ required: true, message: '请输入地址' }]}>
            <Input placeholder="如: http://192.168.1.100:8089" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} placeholder="如: Nginx + Tomcat" />
          </Form.Item>
          <Form.Item name="pairingToken" label="配对Token" rules={[{ required: true, message: '请输入配对Token' }]}>
            <Input placeholder="从目标服务器Agent终端获取" />
          </Form.Item>
          <Form.Item name="groupName" label="分组">
            <Input placeholder="如: Web服务组" />
          </Form.Item>
          <Form.Item name="tags" label="标签">
            <Input placeholder="如: 生产,核心" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">添加</Button>
              <Button onClick={() => setModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
