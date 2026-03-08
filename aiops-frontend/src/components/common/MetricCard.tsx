import { Card, Progress, Statistic } from 'antd'

interface MetricCardProps {
  title: string
  value: number
  suffix?: string
  color: string
}

export function MetricCard({ title, value, suffix = '%', color }: MetricCardProps) {
  return (
    <Card className="glass-card metric-card">
      <Statistic title={title} value={value} suffix={suffix} valueStyle={{ color }} />
      <Progress percent={Math.round(value)} strokeColor={color} showInfo={false} trailColor="rgba(255,255,255,0.08)" />
    </Card>
  )
}
