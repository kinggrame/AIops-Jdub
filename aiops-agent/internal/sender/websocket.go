package sender

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"net/url"
	"strings"
	"time"

	"aiops-agent/internal/executor"

	"nhooyr.io/websocket"
)

type WSClient struct {
	baseURL   string
	agentID   string
	executor  *executor.Executor
	onResult  func(executor.Result)
	connected bool
}

func NewWSClient(baseURL, agentID string, exec *executor.Executor, onResult func(executor.Result)) *WSClient {
	return &WSClient{baseURL: baseURL, agentID: agentID, executor: exec, onResult: onResult}
}

func (c *WSClient) Start(ctx context.Context) {
	go func() {
		for {
			if err := c.connectOnce(ctx); err != nil {
				log.Printf("websocket disabled or unavailable: %v", err)
			}
			select {
			case <-ctx.Done():
				return
			case <-time.After(10 * time.Second):
			}
		}
	}()
}

func (c *WSClient) Connected() bool {
	return c.connected
}

func (c *WSClient) connectOnce(ctx context.Context) error {
	if strings.HasPrefix(strings.ToLower(c.baseURL), "https://") {
		wsURL := strings.Replace(strings.TrimRight(c.baseURL, "/"), "https://", "wss://", 1) + "/ws/agent/" + url.PathEscape(c.agentID)
		return c.runDial(ctx, wsURL)
	}
	wsURL := strings.Replace(strings.TrimRight(c.baseURL, "/"), "http://", "ws://", 1) + "/ws/agent/" + url.PathEscape(c.agentID)
	return c.runDial(ctx, wsURL)
}

func (c *WSClient) runDial(ctx context.Context, wsURL string) error {
	conn, _, err := websocket.Dial(ctx, wsURL, &websocket.DialOptions{HTTPHeader: http.Header{}})
	if err != nil {
		c.connected = false
		return err
	}
	defer conn.Close(websocket.StatusNormalClosure, "bye")
	c.connected = true

	for {
		_, data, err := conn.Read(ctx)
		if err != nil {
			c.connected = false
			return err
		}
		var command struct {
			ID     string         `json:"id"`
			Action string         `json:"action"`
			Params map[string]any `json:"params"`
		}
		if err := json.Unmarshal(data, &command); err != nil {
			continue
		}
		result := c.executor.Execute(command.ID, command.Action, command.Params)
		if c.onResult != nil {
			c.onResult(result)
		}
	}
}
