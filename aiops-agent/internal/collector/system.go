package collector

import (
	"bufio"
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"strconv"
	"strings"
	"time"
)

type SystemCollector struct {
	lastCPUIdle  uint64
	lastCPUTotal uint64
	fallback     *Collector
}

func NewSystemCollector() *SystemCollector {
	return &SystemCollector{fallback: New()}
}

func (c *SystemCollector) Collect() Snapshot {
	if runtime.GOOS != "linux" {
		return c.fallback.Collect()
	}

	cpuUsage := c.readCPUUsage()
	memoryUsage := readMemoryUsage()
	diskUsage := readDiskUsage()
	loadAverage := readLoadAverage()
	processCount := readProcessCount()
	networkThroughput := readNetworkBytes()

	if cpuUsage < 0 || memoryUsage <= 0 {
		return c.fallback.Collect()
	}

	return Snapshot{
		CPU:       MetricGroup{"usage": cpuUsage},
		Memory:    MetricGroup{"usage": memoryUsage},
		Disk:      MetricGroup{"usage": diskUsage},
		Load:      MetricGroup{"average": loadAverage},
		Network:   MetricGroup{"throughput": networkThroughput},
		Process:   MetricGroup{"count": processCount},
		Collected: time.Now(),
	}
}

func (c *SystemCollector) readCPUUsage() float64 {
	file, err := os.Open("/proc/stat")
	if err != nil {
		return -1
	}
	defer file.Close()

	line, err := bufio.NewReader(file).ReadString('\n')
	if err != nil {
		return -1
	}
	fields := strings.Fields(line)
	if len(fields) < 5 {
		return -1
	}

	var values []uint64
	for _, field := range fields[1:] {
		value, err := strconv.ParseUint(field, 10, 64)
		if err != nil {
			return -1
		}
		values = append(values, value)
	}

	idle := values[3]
	total := uint64(0)
	for _, value := range values {
		total += value
	}

	if c.lastCPUTotal == 0 {
		c.lastCPUIdle = idle
		c.lastCPUTotal = total
		return 0
	}

	idleDelta := idle - c.lastCPUIdle
	totalDelta := total - c.lastCPUTotal
	c.lastCPUIdle = idle
	c.lastCPUTotal = total
	if totalDelta == 0 {
		return 0
	}
	return 100 * (1 - float64(idleDelta)/float64(totalDelta))
}

func readMemoryUsage() float64 {
	values := map[string]float64{}
	file, err := os.Open("/proc/meminfo")
	if err != nil {
		return 0
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		parts := strings.Fields(scanner.Text())
		if len(parts) < 2 {
			continue
		}
		value, err := strconv.ParseFloat(parts[1], 64)
		if err != nil {
			continue
		}
		values[strings.TrimSuffix(parts[0], ":")] = value
	}
	total := values["MemTotal"]
	available := values["MemAvailable"]
	if total == 0 {
		return 0
	}
	return (1 - available/total) * 100
}

func readDiskUsage() float64 {
	var statfs syscallStatfs
	if err := statFS("/", &statfs); err != nil || statfs.Blocks == 0 {
		return 0
	}
	used := float64(statfs.Blocks-statfs.Bfree) / float64(statfs.Blocks)
	return used * 100
}

func readLoadAverage() float64 {
	content, err := os.ReadFile("/proc/loadavg")
	if err != nil {
		return 0
	}
	parts := strings.Fields(string(content))
	if len(parts) == 0 {
		return 0
	}
	value, _ := strconv.ParseFloat(parts[0], 64)
	return value
}

func readProcessCount() float64 {
	entries, err := os.ReadDir("/proc")
	if err != nil {
		return 0
	}
	count := 0.0
	for _, entry := range entries {
		if _, err := strconv.Atoi(entry.Name()); err == nil {
			count++
		}
	}
	return count
}

func readNetworkBytes() float64 {
	file, err := os.Open("/proc/net/dev")
	if err != nil {
		return 0
	}
	defer file.Close()

	var total float64
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if !strings.Contains(line, ":") {
			continue
		}
		parts := strings.Fields(strings.ReplaceAll(line, ":", " "))
		if len(parts) < 10 || parts[0] == "lo" {
			continue
		}
		rx, _ := strconv.ParseFloat(parts[1], 64)
		tx, _ := strconv.ParseFloat(parts[9], 64)
		total += (rx + tx) / 1024 / 1024
	}
	return total
}

type syscallStatfs struct {
	Blocks uint64
	Bfree  uint64
}

func statFS(path string, stat *syscallStatfs) error {
	content, err := os.ReadFile(filepath.Clean("/proc/self/mounts"))
	if err != nil || len(content) == 0 {
		return fmt.Errorf("mounts unavailable")
	}
	var fs runtimeStatfs
	if err := runtimeStatFS(path, &fs); err != nil {
		return err
	}
	stat.Blocks = fs.Blocks
	stat.Bfree = fs.Bfree
	return nil
}
