package com.aiops.core.agent;

import java.util.Map;

public interface ReportAgent {

    String buildSummary(Map<String, Object> diagnosis);
}
