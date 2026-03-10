package com.aiops.tool.service;

import com.aiops.tool.model.Tool;
import com.aiops.tool.repository.ToolRepository;
import com.aiops.tool.registry.ToolRegistry;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class ToolService {
    private final ToolRepository toolRepository;
    private final ToolRegistry toolRegistry;

    public ToolService(ToolRepository toolRepository, ToolRegistry toolRegistry) {
        this.toolRepository = toolRepository;
        this.toolRegistry = toolRegistry;
    }

    public List<Tool> findAll() { return toolRepository.findAll(); }
    public List<Tool> findEnabled() { return toolRepository.findByEnabledTrue(); }
    public Tool findById(String id) { return toolRepository.findById(id).orElse(null); }
    public Tool save(Tool tool) { 
        tool.setUpdatedAt(java.time.LocalDateTime.now());
        return toolRepository.save(tool); 
    }
    public void delete(String id) { toolRepository.deleteById(id); }

    public Object execute(String toolId, Map<String, Object> params) {
        return toolRegistry.execute(toolId, params);
    }

    public String getDefinitions() {
        return toolRegistry.getDefinitions();
    }

    public List<Tool> getToolsForAI() {
        return toolRepository.findByEnabledTrue();
    }
}
