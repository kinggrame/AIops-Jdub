package executor

import (
	"fmt"
	"os/exec"
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

	parts := strings.Fields(action)
	if len(parts) == 0 {
		return Result{CommandID: commandID, Status: "rejected", Output: "empty command"}
	}
	cmd := exec.Command(parts[0], parts[1:]...)
	output, err := cmd.CombinedOutput()
	if err != nil {
		return Result{CommandID: commandID, Status: "failed", Output: fmt.Sprintf("%s | error=%v", strings.TrimSpace(string(output)), err)}
	}
	return Result{CommandID: commandID, Status: "success", Output: strings.TrimSpace(string(output)) + " @ " + time.Now().Format(time.RFC3339)}
}
