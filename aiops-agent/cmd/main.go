package main

import (
	"context"
	"flag"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"

	"aiops-agent/internal/cache"
	"aiops-agent/internal/collector"
	"aiops-agent/internal/config"
	"aiops-agent/internal/executor"
	"aiops-agent/internal/logwatch"
	"aiops-agent/internal/security"
	"aiops-agent/internal/sender"
	"aiops-agent/internal/trigger"
	"aiops-agent/internal/webui"
)

func main() {
	configPath := flag.String("c", "config.yaml", "config file path")
	flag.Parse()

	cfg, err := config.Load(*configPath)
	if err != nil {
		log.Fatalf("load config: %v", err)
	}

	ctx, cancel := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer cancel()

	auth, err := security.LoadAuthorization()
	if err != nil {
		log.Printf("load authorization file failed, fallback to config token: %v", err)
	}
	runtimeToken := cfg.GetEffectiveToken()
	agentID := ""
	if auth != nil && auth.ServerURL == cfg.Server.URL && auth.Token != "" {
		runtimeToken = auth.Token
		agentID = auth.AgentID
	}

	httpClient := sender.NewHTTPClient(cfg.Server.URL, runtimeToken)
	collectorSvc := collector.NewSystemCollector()
	triggerEngine := trigger.New(cfg.Triggers)
	exec := executor.New(cfg)
	logWatcher := logwatch.New(cfg.Logwatch)
	reportBuffer := cache.New[map[string]any]()
	eventBuffer := cache.New[trigger.Event]()

	registeredAgentID, issuedToken, err := httpClient.Register(ctx, sender.RegisterRequest{
		Hostname:     cfg.Server.Host,
		IP:           cfg.Server.IP,
		Token:        runtimeToken,
		Capabilities: []string{"cpu", "memory", "disk", "load", "network", "process"},
	})
	if err != nil {
		log.Printf("register failed, agent will continue in offline mode: %v", err)
	}
	if registeredAgentID != "" {
		agentID = registeredAgentID
	}
	if issuedToken != "" {
		runtimeToken = issuedToken
		cfg.Server.Token = issuedToken
		httpClient = sender.NewHTTPClient(cfg.Server.URL, runtimeToken)
		if err := security.SaveAuthorization(security.Authorization{
			AgentID:   agentID,
			Token:     issuedToken,
			ServerURL: cfg.Server.URL,
			Hostname:  cfg.Server.Host,
			IP:        cfg.Server.IP,
			Version:   1,
		}); err != nil {
			log.Printf("save authorization file failed: %v", err)
		}
	}

	state := &webui.State{
		Config:         cfg,
		AgentID:        agentID,
		StartedAt:      time.Now(),
		PendingReports: reportBuffer,
		EventsBuffer:   eventBuffer,
	}
	state.Connected.Store(agentID != "")
	if err := webui.Start(state); err == nil && cfg.WebUI.Enable {
		log.Printf("web ui started on http://localhost:%d", cfg.WebUI.Port)
	}

	trustedServer := ""
	if len(cfg.Server.TrustedServers) > 0 {
		trustedServer = cfg.Server.TrustedServers[0]
	}
	wsClient := sender.NewWSClient(cfg.Server.URL, agentID, runtimeToken, trustedServer, exec, func(result executor.Result) {
		log.Printf("command result: %+v", result)
		if err := httpClient.ReportCommandResult(ctx, sender.CommandResultRequest{
			CommandID: result.CommandID,
			AgentID:   agentID,
			Status:    result.Status,
			Output:    result.Output,
		}); err != nil {
			log.Printf("report command result failed: %v", err)
		}
	})
	if agentID != "" {
		wsClient.Start(ctx)
	}

	ticker := time.NewTicker(time.Duration(cfg.Collection.Interval) * time.Second)
	defer ticker.Stop()

	run := func() {
		snapshot := collectorSvc.Collect()
		state.Snapshot.Store(&snapshot)
		state.Connected.Store(agentID != "" || wsClient.Connected())

		events := triggerEngine.Evaluate(snapshot)
		for _, event := range events {
			eventBuffer.Push(event)
		}

		logs := logWatcher.Collect()
		if len(logs) > 0 && agentID != "" {
			if err := httpClient.ReportLogs(ctx, sender.LogIngestionRequest{
				AgentID:  agentID,
				Hostname: cfg.Server.Host,
				Logs:     logs,
			}); err != nil {
				log.Printf("log report failed: %v", err)
			}
		}

		payload := sender.ReportRequest{
			AgentID:  agentID,
			Hostname: cfg.Server.Host,
			Metrics: map[string]any{
				"cpu":     snapshot.CPU,
				"memory":  snapshot.Memory,
				"disk":    snapshot.Disk,
				"load":    snapshot.Load,
				"network": snapshot.Network,
				"process": snapshot.Process,
			},
			Events: events,
		}

		if agentID == "" {
			reportBuffer.Push(map[string]any{"reason": "agent not registered", "payload": payload})
			return
		}

		if err := httpClient.Report(ctx, payload); err != nil {
			log.Printf("report failed: %v", err)
			reportBuffer.Push(map[string]any{"reason": err.Error(), "payload": payload})
			return
		}
		log.Printf("report sent: cpu=%.1f memory=%.1f events=%d logs=%d", snapshot.CPU["usage"], snapshot.Memory["usage"], len(events), len(logs))
	}

	run()
	for {
		select {
		case <-ctx.Done():
			log.Println("agent shutting down")
			return
		case <-ticker.C:
			run()
		}
	}
}
