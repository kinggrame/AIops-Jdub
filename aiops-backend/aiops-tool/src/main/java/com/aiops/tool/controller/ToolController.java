package com.aiops.tool.controller;

import com.aiops.tool.model.Tool;
import com.aiops.tool.service.ToolService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tools")
public class ToolController {
    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @GetMapping
    public List<Tool> list() { return toolService.findAll(); }

    @GetMapping("/enabled")
    public List<Tool> enabled() { return toolService.findEnabled(); }

    @GetMapping("/{id}")
    public Tool get(@PathVariable String id) { return toolService.findById(id); }

    @PostMapping
    public Tool create(@RequestBody Tool tool) { return toolService.save(tool); }

    @PutMapping("/{id}")
    public Tool update(@PathVariable String id, @RequestBody Tool tool) {
        tool.setId(id);
        return toolService.save(tool);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { toolService.delete(id); }

    @PostMapping("/execute/{id}")
    public Object execute(@PathVariable String id, @RequestBody Map<String, Object> params) {
        return toolService.execute(id, params);
    }

    @GetMapping("/definitions")
    public String definitions() { return toolService.getDefinitions(); }
}
