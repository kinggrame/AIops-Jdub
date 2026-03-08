package collector

import (
	"math/rand"
	"runtime"
	"time"
)

type Snapshot struct {
	CPU       MetricGroup `json:"cpu"`
	Memory    MetricGroup `json:"memory"`
	Disk      MetricGroup `json:"disk"`
	Load      MetricGroup `json:"load"`
	Network   MetricGroup `json:"network"`
	Process   MetricGroup `json:"process"`
	Collected time.Time   `json:"collectedAt"`
}

type MetricGroup map[string]float64

type Collector struct {
	rng *rand.Rand
}

func New() *Collector {
	return &Collector{rng: rand.New(rand.NewSource(time.Now().UnixNano()))}
}

func (c *Collector) Collect() Snapshot {
	var mem runtime.MemStats
	runtime.ReadMemStats(&mem)

	cpuUsage := clamp(18+c.rng.Float64()*72, 0, 99)
	memoryUsage := clamp(float64(mem.Alloc%90000000)/1000000+22, 0, 95)
	diskUsage := clamp(40+c.rng.Float64()*45, 0, 98)
	loadValue := clamp(cpuUsage/35+c.rng.Float64()*1.6, 0, 10)
	processCount := clamp(80+c.rng.Float64()*120, 0, 999)
	networkThroughput := clamp(c.rng.Float64()*120, 0, 999)

	return Snapshot{
		CPU:       MetricGroup{"usage": cpuUsage},
		Memory:    MetricGroup{"usage": memoryUsage},
		Disk:      MetricGroup{"usage": diskUsage},
		Load:      MetricGroup{"average": loadValue},
		Network:   MetricGroup{"throughput": networkThroughput},
		Process:   MetricGroup{"count": processCount},
		Collected: time.Now(),
	}
}

func clamp(v, min, max float64) float64 {
	if v < min {
		return min
	}
	if v > max {
		return max
	}
	return v
}
