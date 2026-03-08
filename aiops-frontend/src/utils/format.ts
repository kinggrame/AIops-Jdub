import dayjs from 'dayjs'

export function formatPercent(value?: number) {
  if (value === undefined || Number.isNaN(value)) {
    return '--'
  }
  return `${value.toFixed(1)}%`
}

export function formatTime(value?: string) {
  if (!value) {
    return '--'
  }
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

export function getMetricValue(metrics: Record<string, unknown>, group: string, key: string) {
  const entry = metrics[group] as Record<string, number> | undefined
  return entry?.[key]
}
