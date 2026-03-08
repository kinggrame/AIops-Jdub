APP=aiops-agent

.PHONY: tidy build run

tidy:
	go mod tidy

build:
	go build -o bin/$(APP) ./cmd

run:
	go run ./cmd -c config.yaml
