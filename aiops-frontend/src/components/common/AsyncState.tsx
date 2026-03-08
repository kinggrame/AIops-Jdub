import { Button, Empty, Result, Skeleton, Space } from 'antd'
import type { ReactNode } from 'react'

interface AsyncStateProps {
  loading?: boolean
  error?: unknown
  empty?: boolean
  emptyDescription?: string
  onRetry?: () => void
  children: ReactNode
}

export function AsyncState({ loading, error, empty, emptyDescription, onRetry, children }: AsyncStateProps) {
  if (loading) {
    return <Skeleton active paragraph={{ rows: 6 }} />
  }

  if (error) {
    return (
      <Result
        status="error"
        title="数据加载失败"
        subTitle={error instanceof Error ? error.message : '请求异常，请稍后重试。'}
        extra={onRetry ? <Button onClick={onRetry}>重新加载</Button> : undefined}
      />
    )
  }

  if (empty) {
    return (
      <Space style={{ width: '100%', justifyContent: 'center', padding: 24 }}>
        <Empty description={emptyDescription ?? '暂无数据'} />
      </Space>
    )
  }

  return <>{children}</>
}
