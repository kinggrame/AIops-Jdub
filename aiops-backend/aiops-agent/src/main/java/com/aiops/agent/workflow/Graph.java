package com.aiops.agent.workflow;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Graph implements Serializable {
    private final String name;
    private final Map<String, GraphNode> nodes = new ConcurrentHashMap<>();
    private final Map<String, String> edges = new ConcurrentHashMap<>();
    private final Map<String, ConditionalEdge> conditionalEdges = new ConcurrentHashMap<>();
    private String startNode;
    private String endNode;

    public Graph(String name) {
        this.name = name;
    }

    public Graph addNode(GraphNode node) {
        nodes.put(node.getName(), node);
        return this;
    }

    public Graph addEdge(String from, String to) {
        edges.put(from, to);
        return this;
    }

    public Graph addConditionalEdge(String from, Function<GraphState, String> condition) {
        conditionalEdges.put(from, new ConditionalEdge(from, condition));
        return this;
    }

    public Graph setStart(String nodeName) {
        this.startNode = nodeName;
        return this;
    }

    public Graph setEnd(String nodeName) {
        this.endNode = nodeName;
        return this;
    }

    public String traverse(GraphState state) {
        String current = startNode;
        while (current != null && !current.equals(endNode)) {
            GraphNode node = nodes.get(current);
            if (node == null) break;

            state.setCurrentNode(current);
            state = node.process(state);

            ConditionalEdge conditional = conditionalEdges.get(current);
            if (conditional != null) {
                current = conditional.apply(state);
            } else {
                current = edges.get(current);
            }

            state.incrementIteration();
            if (state.getIteration() > 20) {
                state.setCompleted(true);
                state.setFinalResult("最大迭代次数已达");
                break;
            }
        }

        if (endNode != null) {
            state.setCurrentNode(endNode);
            state.setCompleted(true);
        }

        return state.getFinalResult();
    }

    public Map<String, GraphNode> getNodes() { return nodes; }
    public String getStartNode() { return startNode; }
    public String getEndNode() { return endNode; }
}

class ConditionalEdge implements Function<GraphState, String>, Serializable {
    private final String fromNode;
    private final Function<GraphState, String> condition;

    public ConditionalEdge(String fromNode, Function<GraphState, String> condition) {
        this.fromNode = fromNode;
        this.condition = condition;
    }

    @Override
    public String apply(GraphState state) {
        return condition.apply(state);
    }

    public String getFromNode() {
        return fromNode;
    }
}
