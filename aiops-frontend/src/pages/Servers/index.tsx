import { useState, useEffect } from 'react'
import { Button, Card, Table, Tag, Space, Modal, Form, Input, Select, message, Popconfirm } from 'antd'
import { PageHeader } from '../../components/common/PageHeader'
import { fetchServers, createServer, deleteServer, type ServerDTO, type CreateServerRequest } from '../../api/aiops'

const statusColors: Record<string, string> = {
  ONLINE: 'green',
  OFFLINE: 'gray',
  MAINTENANCE: 'orange'
}

const statusText: Record<string, string> = {
  ONLINE: '在线',
  OFFLINE: '离线',
  MAINTENANCE: '维护中'
}

export default function ServersPage() {
  const [servers, setServers] = useState<ServerDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [form] = Form.useForm()
  const [statusFilter, setStatusFilter] = useState<string>('')

  const loadServers = async () => {
    setLoading(true)
    try {
      const data = await fetchServers(statusFilter || undefined)
      setServers(data || [])
    } catch (error) {
      message.error('加载服务器列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadServers()
  }, [statusFilter])

  const handleAdd = async (values: CreateServerRequest) => {
    try {
      await createServer(values)
      message.success('添加成功')
      setModalVisible(false)
      form.resetFields()
      loadServers()
    } catch (error) {
      message.error('添加失败')
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await deleteServer(id)
      message.success('删除成功')
      loadServers()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const columns = [
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: '地址', dataIndex: 'endpoint', key: 'endpoint' },
    { title: '分组', dataIndex: 'groupName', key: 'groupName' },
    { title: '标签', dataIndex: 'tags', key: 'tags' },
    { 
      title: '状态', 
      dataIndex: 'status', 
      key: 'status',
      render: (status: string) => <Tag color={statusColors[status]}>{statusText[status]}</Tag>
    },
    { 
      title: '最新指标', 
      key: 'metrics',
      render: (_: unknown, record: ServerDTO) => {
        const metrics = record.latestMetrics
        if (!metrics) return '-'
        return (
          <Space direction="vertical" size={0}>
            {metrics['cpu.usage'] && <span>CPU: {metrics['cpu.usage']}%</span>}
            {metrics['memory.usage'] && <span>内存: {metrics['memory.usage']}%</span>}
          </Space>
        )
      }
    },
    {
      title: '操作',
      key: 'action',
      render: (_: unknown, record: ServerDTO) => (
        <Space size="small">
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
          title="服务器管理" 
          description="管理运维目标服务器" 
        />
        <Button type="primary" onClick={() => setModalVisible(true)}>+ 添加服务器</Button>
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
              { value: 'ONLINE', label: '在线' },
              { value: 'OFFLINE', label: '离线' },
              { value: 'MAINTENANCE', label: '维护中' },
            ]}
          />
        </Space>

        <Table
          columns={columns}
          dataSource={servers}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      <Modal
        title="添加服务器"
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
          <Form.Item name="groupName" label="分组">
            <Input placeholder="如: Web服务组" />
          </Form.Item>
          <Form.Item name="tags" label="标签">
            <Input placeholder="如: 生产,核心" />
          </Form.Item>
          <Form.Item name="agentId" label="关联Agent ID">
            <Input placeholder="可选" />
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
