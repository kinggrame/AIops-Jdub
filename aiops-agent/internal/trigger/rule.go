package trigger

import "aiops-agent/internal/config"

type Event struct {
	Type   string  `json:"type"`
	Metric string  `json:"metric"`
	Value  float64 `json:"value"`
	Target string  `json:"target"`
	Rule   string  `json:"rule"`
}

type Rule = config.TriggerRule
