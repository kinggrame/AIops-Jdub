package com.aiops.agent.workflow;

public interface GraphNode {
    String getName();
    GraphState process(GraphState state);
    default String decideNext(GraphState state) {
        return null;
    }
}
