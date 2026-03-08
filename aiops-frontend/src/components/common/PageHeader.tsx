import { Space, Tag, Typography } from 'antd'

interface PageHeaderProps {
  title: string
  description: string
  badge?: string
}

export function PageHeader({ title, description, badge }: PageHeaderProps) {
  return (
    <Space direction="vertical" size={4} style={{ width: '100%' }}>
      <Space align="center">
        <Typography.Title level={2} style={{ margin: 0, color: '#f5f7ff' }}>
          {title}
        </Typography.Title>
        {badge ? <Tag color="gold">{badge}</Tag> : null}
      </Space>
      <Typography.Paragraph style={{ margin: 0, color: 'rgba(226,232,240,0.78)' }}>
        {description}
      </Typography.Paragraph>
    </Space>
  )
}
