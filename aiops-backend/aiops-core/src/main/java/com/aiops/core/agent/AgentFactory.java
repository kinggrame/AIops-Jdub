package com.aiops.core.agent;

import com.aiops.common.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class AgentFactory {

    private final DataAgent dataAgent;
    private final AnalysisAgent analysisAgent;
    private final ReportAgent reportAgent;

    public AgentFactory(DataAgent dataAgent, AnalysisAgent analysisAgent, ReportAgent reportAgent) {
        this.dataAgent = dataAgent;
        this.analysisAgent = analysisAgent;
        this.reportAgent = reportAgent;
    }

    public DataAgent dataAgent() {
        return dataAgent;
    }

    public AnalysisAgent analysisAgent() {
        return analysisAgent;
    }

    public ReportAgent reportAgent() {
        return reportAgent;
    }

    public void validateType(String type) {
        if (!("data".equalsIgnoreCase(type) || "analysis".equalsIgnoreCase(type) || "report".equalsIgnoreCase(type))) {
            throw new BusinessException("Unsupported agentType: " + type);
        }
    }
}
