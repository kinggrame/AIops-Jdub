package com.aiops.core.agent;

import com.aiops.common.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class AgentFactory {

    private final DataAgent dataAgent;
    private final AnalysisAgent analysisAgent;
    private final ReportAgent reportAgent;
    private final SummarizeAgent summarizeAgent;
    private final PlanAgent planAgent;
    private final ExecuteAgent executeAgent;

    public AgentFactory(DataAgent dataAgent,
                        AnalysisAgent analysisAgent,
                        ReportAgent reportAgent,
                        SummarizeAgent summarizeAgent,
                        PlanAgent planAgent,
                        ExecuteAgent executeAgent) {
        this.dataAgent = dataAgent;
        this.analysisAgent = analysisAgent;
        this.reportAgent = reportAgent;
        this.summarizeAgent = summarizeAgent;
        this.planAgent = planAgent;
        this.executeAgent = executeAgent;
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

    public SummarizeAgent summarizeAgent() {
        return summarizeAgent;
    }

    public PlanAgent planAgent() {
        return planAgent;
    }

    public ExecuteAgent executeAgent() {
        return executeAgent;
    }

    public void validateType(String type) {
        if (!("data".equalsIgnoreCase(type)
                || "analysis".equalsIgnoreCase(type)
                || "report".equalsIgnoreCase(type)
                || "summarize".equalsIgnoreCase(type)
                || "plan".equalsIgnoreCase(type)
                || "execute".equalsIgnoreCase(type))) {
            throw new BusinessException("Unsupported agentType: " + type);
        }
    }
}
