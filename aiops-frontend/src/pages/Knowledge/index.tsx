import { useMutation } from '@tanstack/react-query'
import { App, Button, Card, Input, List, Space, Tag, Typography } from 'antd'
import { useState } from 'react'
import { searchKnowledge, searchLogs } from '../../api/aiops'
import { AsyncState } from '../../components/common/AsyncState'
import { PageHeader } from '../../components/common/PageHeader'

export default function KnowledgePage() {
  const { message } = App.useApp()
  const [query, setQuery] = useState('cpu nginx restart')
  const [logs, setLogs] = useState<string[]>([])
  const knowledgeMutation = useMutation({ mutationFn: (value: string) => searchKnowledge(value, 5) })

  return (
    <Space direction="vertical" size={24} style={{ width: '100%' }}>
      <PageHeader title="知识检索" description="从后端知识库与日志检索接口获取上下文，用于运维分析与 RAG 联调。" badge="RAG" />
      <Card className="glass-card">
        <Space.Compact style={{ width: '100%' }}>
          <Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="输入关键字，例如 cpu nginx restart" />
          <Button
            type="primary"
            loading={knowledgeMutation.isPending}
            onClick={async () => {
              const result = await knowledgeMutation.mutateAsync(query)
              const logResults = await searchLogs(query, 5)
              setLogs(logResults.map((item) => item.message))
              message.success('知识检索完成')
              return result
            }}
          >
            搜索
          </Button>
        </Space.Compact>
      </Card>
      <Card className="glass-card" title="知识结果">
        <AsyncState loading={knowledgeMutation.isPending} error={knowledgeMutation.error} empty={!(knowledgeMutation.data?.length)} emptyDescription="请输入关键字后开始检索。">
          <List
            dataSource={knowledgeMutation.data ?? []}
            renderItem={(item) => (
              <List.Item>
                <List.Item.Meta
                  title={<Space><span>{item.title}</span><Tag color="blue">{item.category}</Tag><Tag>{item.score.toFixed(1)}</Tag></Space>}
                  description={item.content}
                />
              </List.Item>
            )}
          />
        </AsyncState>
      </Card>
      <Card className="glass-card" title="相关日志">
        <AsyncState empty={!logs.length} emptyDescription="暂无相关日志。">
          <List dataSource={logs} renderItem={(item) => <List.Item><Typography.Text>{item}</Typography.Text></List.Item>} />
        </AsyncState>
      </Card>
    </Space>
  )
}
