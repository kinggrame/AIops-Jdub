package com.aiops.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AgentFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reportFlowShouldGenerateAlertAnalysisAndCommand() throws Exception {
        String registerBody = """
                {
                  "hostname": "server-001",
                  "ip": "10.0.0.1",
                  "token": "aiops-mvp-seed-demo-token",
                  "capabilities": ["cpu", "memory", "disk"]
                }
                """;

        String agentId = mockMvc.perform(post("/api/v1/agent/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hostname", is("server-001")))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("(?s).*\"agentId\":\"([^\"]+)\".*", "$1");

        String reportBody = """
                {
                  "agentId": "%s",
                  "hostname": "server-001",
                  "metrics": {
                    "cpu": {"usage": 95},
                    "memory": {"usage": 88}
                  },
                  "events": [
                    {"type": "threshold", "metric": "cpu.usage", "value": 95, "target": "ai"}
                  ]
                }
                """.formatted(agentId);

        mockMvc.perform(post("/api/v1/agent/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reportBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stored", is(true)))
                .andExpect(jsonPath("$.data.status", is("command_dispatched")))
                .andExpect(jsonPath("$.data.alerts[0].severity", containsString("critical")))
                .andExpect(jsonPath("$.data.analysis.analysis", containsString("Detected")))
                .andExpect(jsonPath("$.data.command.action", is("get_logs")));

        mockMvc.perform(get("/api/v1/agent/command/results"))
                .andExpect(status().isOk());
    }

    @Test
    void chatAndKnowledgeEndpointsShouldRespond() throws Exception {
        String chatBody = """
                {
                  "agentType": "analysis",
                  "message": "cpu usage is high",
                  "metrics": {
                    "cpu": {"usage": 92},
                    "memory": {"usage": 70}
                  },
                  "events": []
                }
                """;

        mockMvc.perform(post("/api/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chatBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agentType", is("analysis")))
                .andExpect(jsonPath("$.data.provider", is("Ollama")));

        String knowledgeBody = """
                {
                  "query": "cpu nginx restart",
                  "topK": 2
                }
                """;

        mockMvc.perform(post("/api/v1/knowledge/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(knowledgeBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").exists());
    }
}
