package logwatch

import (
	"bufio"
	"os"
	"strings"
	"time"

	"aiops-agent/internal/config"
)

type Watcher struct {
	cfg     config.LogwatchConfig
	offsets map[string]int64
}

func New(cfg config.LogwatchConfig) *Watcher {
	return &Watcher{cfg: cfg, offsets: map[string]int64{}}
}

func (w *Watcher) Collect() []map[string]any {
	var entries []map[string]any
	for _, file := range w.cfg.Files {
		entries = append(entries, w.readFile(file)...)
	}
	return entries
}

func (w *Watcher) readFile(path string) []map[string]any {
	file, err := os.Open(path)
	if err != nil {
		return nil
	}
	defer file.Close()

	if _, err := file.Seek(w.offsets[path], 0); err != nil {
		return nil
	}

	var results []map[string]any
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
		if !w.match(line) {
			continue
		}
		results = append(results, map[string]any{
			"source":    path,
			"content":   truncate(line, 500),
			"timestamp": time.Now().Format(time.RFC3339),
		})
	}
	if offset, err := file.Seek(0, 1); err == nil {
		w.offsets[path] = offset
	}
	return results
}

func (w *Watcher) match(line string) bool {
	if len(w.cfg.Keywords) == 0 {
		return true
	}
	text := strings.ToLower(line)
	for _, keyword := range w.cfg.Keywords {
		if strings.Contains(text, strings.ToLower(keyword)) {
			return true
		}
	}
	return false
}

func truncate(value string, size int) string {
	if len(value) <= size {
		return value
	}
	return value[:size]
}
