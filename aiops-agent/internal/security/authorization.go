package security

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"time"
)

type Authorization struct {
	AgentID   string `json:"agentId"`
	Token     string `json:"token"`
	ServerURL string `json:"serverUrl"`
	Hostname  string `json:"hostname,omitempty"`
	IP        string `json:"ip,omitempty"`
	IssuedAt  string `json:"issuedAt"`
	Version   int    `json:"version"`
}

func AuthorizationPath() (string, error) {
	home, err := os.UserHomeDir()
	if err != nil {
		return "", fmt.Errorf("resolve user home: %w", err)
	}
	return filepath.Join(home, ".aiops", "authorization.json"), nil
}

func LoadAuthorization() (*Authorization, error) {
	path, err := AuthorizationPath()
	if err != nil {
		return nil, err
	}
	content, err := os.ReadFile(path)
	if err != nil {
		if os.IsNotExist(err) {
			return nil, nil
		}
		return nil, fmt.Errorf("read authorization file: %w", err)
	}
	var auth Authorization
	if err := json.Unmarshal(content, &auth); err != nil {
		return nil, fmt.Errorf("parse authorization file: %w", err)
	}
	if auth.Token == "" || auth.AgentID == "" {
		return nil, nil
	}
	return &auth, nil
}

func SaveAuthorization(auth Authorization) error {
	path, err := AuthorizationPath()
	if err != nil {
		return err
	}
	if auth.Version == 0 {
		auth.Version = 1
	}
	if auth.IssuedAt == "" {
		auth.IssuedAt = time.Now().UTC().Format(time.RFC3339)
	}
	if err := os.MkdirAll(filepath.Dir(path), 0o700); err != nil {
		return fmt.Errorf("create authorization directory: %w", err)
	}
	content, err := json.MarshalIndent(auth, "", "  ")
	if err != nil {
		return fmt.Errorf("marshal authorization file: %w", err)
	}
	if err := os.WriteFile(path, content, 0o600); err != nil {
		return fmt.Errorf("write authorization file: %w", err)
	}
	if runtime.GOOS == "windows" {
		return nil
	}
	return os.Chmod(path, 0o600)
}
