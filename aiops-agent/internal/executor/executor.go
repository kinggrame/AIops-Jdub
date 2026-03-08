package executor

import (
	"fmt"
	"strings"
	"time"

	"aiops-agent/internal/config"
)

type Result struct {
	CommandID string `json:"cmd_id"`
	Status    string `json:"status"`
	Output    string `json:"output"`
}

type Executor struct {
	cfg *config.Config
}

func New(cfg *config.Config) *Executor {
	return &Executor{cfg: cfg}
}

func (e *Executor) Execute(commandID, action string, params map[string]any) Result {
	if !e.cfg.AllowedCommand(action) {
		return Result{CommandID: commandID, Status: "rejected", Output: "command not allowed"}
	}
	if strings.Contains(strings.ToLower(action), "rm -rf") {
		return Result{CommandID: commandID, Status: "rejected", Output: "dangerous command blocked"}
	}

	output := fmt.Sprintf("executed %s with params=%v at %s", action, params, time.Now().Format(time.RFC3339))
	return Result{CommandID: commandID, Status: "success", Output: output}
}
