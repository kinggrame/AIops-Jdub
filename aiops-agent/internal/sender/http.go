package sender

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"strings"
	"time"

	"aiops-agent/internal/security"
	"aiops-agent/internal/trigger"
)

type RegisterRequest struct {
	Hostname     string   `json:"hostname"`
	IP           string   `json:"ip"`
	Token        string   `json:"token"`
	Capabilities []string `json:"capabilities"`
}

type RegisterResponse struct {
	Success bool `json:"success"`
	Data    struct {
		AgentID string `json:"agentId"`
		Token   string `json:"token"`
	} `json:"data"`
}

type ReportRequest struct {
	AgentID  string          `json:"agentId"`
	Hostname string          `json:"hostname"`
	Metrics  map[string]any  `json:"metrics"`
	Events   []trigger.Event `json:"events"`
}

type LogIngestionRequest struct {
	AgentID  string           `json:"agentId"`
	Hostname string           `json:"hostname"`
	Logs     []map[string]any `json:"logs"`
}

type HTTPClient struct {
	baseURL string
	token   string
	client  *http.Client
}

type CommandResultRequest struct {
	CommandID string `json:"commandId"`
	AgentID   string `json:"agentId"`
	Status    string `json:"status"`
	Output    string `json:"output"`
}

func NewHTTPClient(baseURL, token string) *HTTPClient {
	return &HTTPClient{
		baseURL: strings.TrimRight(baseURL, "/"),
		token:   token,
		client:  &http.Client{Timeout: 10 * time.Second},
	}
}

func (c *HTTPClient) Register(ctx context.Context, payload RegisterRequest) (string, string, error) {
	var response RegisterResponse
	if err := c.post(ctx, "/api/v1/agent/register", payload, &response); err != nil {
		return "", "", err
	}
	if response.Data.AgentID == "" {
		return "", "", fmt.Errorf("empty agentId in register response")
	}
	if response.Data.Token == "" {
		return "", "", fmt.Errorf("empty token in register response")
	}
	return response.Data.AgentID, response.Data.Token, nil
}

func (c *HTTPClient) Report(ctx context.Context, payload ReportRequest) error {
	return c.post(ctx, "/api/v1/agent/report", payload, nil)
}

func (c *HTTPClient) ReportLogs(ctx context.Context, payload LogIngestionRequest) error {
	return c.post(ctx, "/api/v1/agent/logs", payload, nil)
}

func (c *HTTPClient) ReportCommandResult(ctx context.Context, payload CommandResultRequest) error {
	return c.post(ctx, "/api/v1/agent/command/result", payload, nil)
}

func (c *HTTPClient) post(ctx context.Context, path string, payload any, out any) error {
	body, err := json.Marshal(payload)
	if err != nil {
		return err
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, c.baseURL+path, bytes.NewBuffer(body))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	if token := security.BearerToken(c.token); token != "" {
		req.Header.Set("Authorization", token)
	}

	resp, err := c.client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 300 {
		return fmt.Errorf("request failed with status %s", resp.Status)
	}
	if out == nil {
		return nil
	}
	return json.NewDecoder(resp.Body).Decode(out)
}
