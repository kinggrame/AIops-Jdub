import { Suspense, lazy } from 'react'
import { Skeleton } from 'antd'

const ColumnChart = lazy(async () => {
  const module = await import('@ant-design/charts')
  return { default: module.Column }
})

interface LazyChartProps {
  data: Array<{ type: string; value: number }>
}

export function LazyChart({ data }: LazyChartProps) {
  return (
    <Suspense fallback={<Skeleton active paragraph={{ rows: 8 }} />}>
      <ColumnChart data={data} xField="type" yField="value" colorField="type" height={280} />
    </Suspense>
  )
}
