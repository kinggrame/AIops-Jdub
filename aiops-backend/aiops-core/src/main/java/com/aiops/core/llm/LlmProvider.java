package com.aiops.core.llm;

import java.util.Map;

public interface LlmProvider {

    String name();

    boolean available();

    String generate(String prompt, Map<String, Object> context);
}
