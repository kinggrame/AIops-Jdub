package com.aiops.agent.workflow;

public interface AgentWorkflow {
    String plan(String request, String context);
    String analyze(String task, String data);
    String execute(String action, String params);
    String report(String results);
}
