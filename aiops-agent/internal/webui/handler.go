package webui

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strings"
	"sync/atomic"
	"time"

	"aiops-agent/internal/cache"
	"aiops-agent/internal/collector"
	"aiops-agent/internal/config"
	"aiops-agent/internal/trigger"
)

type State struct {
	Config         *config.Config
	AgentID        string
	StartedAt      time.Time
	Snapshot       atomic.Pointer[collector.Snapshot]
	Connected      atomic.Bool
	PendingReports *cache.Buffer[map[string]any]
	EventsBuffer   *cache.Buffer[trigger.Event]
}

func NewServer(state *State) http.Handler {
	mux := http.NewServeMux()
	mux.HandleFunc("/api/state", func(w http.ResponseWriter, r *http.Request) {
		_ = json.NewEncoder(w).Encode(buildState(state))
	})
	mux.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		_, _ = w.Write([]byte(renderHTML()))
	})
	return mux
}

func buildState(state *State) map[string]any {
	var snapshot any = nil
	if value := state.Snapshot.Load(); value != nil {
		snapshot = value
	}
	return map[string]any{
		"agentId":        state.AgentID,
		"serverUrl":      state.Config.Server.URL,
		"host":           state.Config.Server.Host,
		"connected":      state.Connected.Load(),
		"uptime":         time.Since(state.StartedAt).Round(time.Second).String(),
		"metrics":        snapshot,
		"triggers":       state.Config.Triggers,
		"commands":       state.Config.Commands.Allowed,
		"pendingReports": len(state.PendingReports.Snapshot()),
		"recentEvents":   state.EventsBuffer.Snapshot(),
	}
}

func renderHTML() string {
	return `<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>AIOps Agent</title>
  <style>
    body{margin:0;font-family:Segoe UI,Arial,sans-serif;background:linear-gradient(135deg,#06121f,#0f172a);color:#e2e8f0;padding:24px}
    .wrap{max-width:1100px;margin:0 auto;display:grid;gap:16px}
    .hero,.card{background:rgba(15,23,42,.8);border:1px solid rgba(148,163,184,.16);border-radius:20px;padding:20px;box-shadow:0 20px 60px rgba(2,6,23,.25)}
    .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:16px}
    .metric{font-size:28px;font-weight:700;color:#38bdf8}
    .muted{color:#94a3b8}
    ul{padding-left:18px}
    code{background:rgba(148,163,184,.12);padding:2px 6px;border-radius:6px}
  </style>
</head>
<body>
  <div class="wrap">
    <div class="hero">
      <h1>AIOps Agent 控制面板</h1>
      <p class="muted">轻量客户端运行状态、实时指标、触发规则与命令白名单。</p>
      <div id="summary"></div>
    </div>
    <div class="grid">
      <div class="card"><h3>实时指标</h3><div id="metrics"></div></div>
      <div class="card"><h3>触发规则</h3><ul id="triggers"></ul></div>
      <div class="card"><h3>允许命令</h3><ul id="commands"></ul></div>
      <div class="card"><h3>最近事件</h3><ul id="events"></ul></div>
    </div>
  </div>
  <script>
    async function render(){
      const data = await fetch('/api/state').then(r=>r.json())
      document.getElementById('summary').innerHTML = '<p>状态: <strong>'+(data.connected?'已连接':'未连接')+'</strong> · AgentID: <code>'+(data.agentId||'pending')+'</code> · 服务端: <code>'+data.serverUrl+'</code> · 运行时间: '+data.uptime+'</p>'
      const metrics = data.metrics || {}
      document.getElementById('metrics').innerHTML =
        '<div><span class="metric">'+((metrics.cpu && typeof metrics.cpu.usage === "number") ? metrics.cpu.usage.toFixed(1) : '--')+'%</span><div class="muted">CPU</div></div>'+
        '<div><span class="metric">'+((metrics.memory && typeof metrics.memory.usage === "number") ? metrics.memory.usage.toFixed(1) : '--')+'%</span><div class="muted">内存</div></div>'+
        '<div><span class="metric">'+((metrics.disk && typeof metrics.disk.usage === "number") ? metrics.disk.usage.toFixed(1) : '--')+'%</span><div class="muted">磁盘</div></div>'
      document.getElementById('triggers').innerHTML = (data.triggers||[]).map(function(t){ return '<li>'+t.name+': '+t.metric+' '+t.operator+' '+t.value+' -> '+t.target+'</li>' }).join('')
      document.getElementById('commands').innerHTML = (data.commands||[]).map(function(c){ return '<li>'+c+'</li>' }).join('')
      document.getElementById('events').innerHTML = (data.recentEvents||[]).slice(-6).reverse().map(function(e){ return '<li>'+e.metric+': '+e.value+' -> '+e.target+'</li>' }).join('') || '<li>暂无</li>'
    }
    render(); setInterval(render, 3000)
  </script>
</body>
</html>`
}

func Start(state *State) error {
	if !state.Config.WebUI.Enable {
		return nil
	}
	addr := fmt.Sprintf(":%d", state.Config.WebUI.Port)
	server := &http.Server{Addr: addr, Handler: NewServer(state)}
	go func() {
		_ = server.ListenAndServe()
	}()
	return nil
}

func MetricText(snapshot *collector.Snapshot) string {
	if snapshot == nil {
		return "no metrics"
	}
	parts := []string{
		fmt.Sprintf("cpu=%.1f", snapshot.CPU["usage"]),
		fmt.Sprintf("memory=%.1f", snapshot.Memory["usage"]),
		fmt.Sprintf("disk=%.1f", snapshot.Disk["usage"]),
	}
	return strings.Join(parts, ", ")
}
