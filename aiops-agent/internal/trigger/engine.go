package trigger

import (
	"strings"

	"aiops-agent/internal/collector"
	"aiops-agent/internal/config"
)

type Engine struct {
	rules []config.TriggerRule
}

func New(rules []config.TriggerRule) *Engine {
	return &Engine{rules: rules}
}

func (e *Engine) Evaluate(snapshot collector.Snapshot) []Event {
	metrics := flatten(snapshot)
	var events []Event
	for _, rule := range e.rules {
		value, ok := metrics[rule.Metric]
		if !ok {
			continue
		}
		if compare(value, rule.Operator, rule.Value) {
			events = append(events, Event{
				Type:   "threshold",
				Metric: rule.Metric,
				Value:  value,
				Target: rule.Target,
				Rule:   rule.Name,
			})
		}
	}
	return events
}

func flatten(snapshot collector.Snapshot) map[string]float64 {
	return map[string]float64{
		"cpu.usage":          snapshot.CPU["usage"],
		"memory.usage":       snapshot.Memory["usage"],
		"disk.usage":         snapshot.Disk["usage"],
		"load.average":       snapshot.Load["average"],
		"network.throughput": snapshot.Network["throughput"],
		"process.count":      snapshot.Process["count"],
	}
}

func compare(current float64, operator string, target float64) bool {
	switch strings.TrimSpace(operator) {
	case ">":
		return current > target
	case ">=":
		return current >= target
	case "<":
		return current < target
	case "<=":
		return current <= target
	case "==":
		return current == target
	case "!=":
		return current != target
	default:
		return false
	}
}
