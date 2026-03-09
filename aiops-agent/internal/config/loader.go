package config

import (
	"fmt"
	"os"
	"strings"

	"gopkg.in/yaml.v3"
)

type Config struct {
	Server     ServerConfig     `yaml:"server"`
	WebUI      WebUIConfig      `yaml:"webui"`
	Collection CollectionConfig `yaml:"collection"`
	Triggers   []TriggerRule    `yaml:"triggers"`
	Commands   CommandsConfig   `yaml:"commands"`
	Logwatch   LogwatchConfig   `yaml:"logwatch"`
	Security   SecurityConfig   `yaml:"security"`
}

type ServerConfig struct {
	URL            string   `yaml:"url"`
	Token          string   `yaml:"token"`
	BootstrapToken string   `yaml:"bootstrap_token"`
	Host           string   `yaml:"host"`
	IP             string   `yaml:"ip"`
	TrustedServers []string `yaml:"trusted_servers"`
}

type WebUIConfig struct {
	Enable bool `yaml:"enable"`
	Port   int  `yaml:"port"`
}

type CollectionConfig struct {
	Interval int `yaml:"interval"`
}

type TriggerRule struct {
	Name     string  `yaml:"name"`
	Metric   string  `yaml:"metric"`
	Operator string  `yaml:"operator"`
	Value    float64 `yaml:"value"`
	Target   string  `yaml:"target"`
}

type CommandsConfig struct {
	Allowed []string `yaml:"allowed"`
}

type LogwatchConfig struct {
	Files    []string `yaml:"files"`
	Keywords []string `yaml:"keywords"`
}

type SecurityConfig struct {
	ApprovalRequired bool     `yaml:"approval_required"`
	TrustedServers   []string `yaml:"trusted_servers"`
}

func Load(path string) (*Config, error) {
	content, err := os.ReadFile(path)
	if err != nil {
		return nil, err
	}

	expanded := os.ExpandEnv(string(content))
	var cfg Config
	if err := yaml.Unmarshal([]byte(expanded), &cfg); err != nil {
		return nil, err
	}

	if cfg.Collection.Interval <= 0 {
		cfg.Collection.Interval = 30
	}
	if cfg.WebUI.Port == 0 {
		cfg.WebUI.Port = 8089
	}
	if cfg.Server.BootstrapToken == "" {
		cfg.Server.BootstrapToken = cfg.Server.Token
	}
	if cfg.Server.BootstrapToken == "" {
		return nil, fmt.Errorf("server.bootstrap_token is required")
	}
	if cfg.Server.Host == "" {
		cfg.Server.Host = cfg.Server.IP
	}
	return &cfg, nil
}

func (c *Config) AllowedCommand(action string) bool {
	action = strings.TrimSpace(strings.ToLower(action))
	for _, allowed := range c.Commands.Allowed {
		if strings.ToLower(allowed) == action {
			return true
		}
	}
	return false
}

func (c *Config) TrustedServer(server string) bool {
	server = strings.TrimSpace(strings.ToLower(server))
	for _, item := range c.Server.TrustedServers {
		if strings.ToLower(strings.TrimSpace(item)) == server {
			return true
		}
	}
	for _, item := range c.Security.TrustedServers {
		if strings.ToLower(strings.TrimSpace(item)) == server {
			return true
		}
	}
	return false
}
