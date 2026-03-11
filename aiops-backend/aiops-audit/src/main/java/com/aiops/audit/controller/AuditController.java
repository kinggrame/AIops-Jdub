package com.aiops.audit.controller;

import com.aiops.audit.model.AuditLog;
import com.aiops.audit.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public Page<AuditLog> list(@RequestParam(defaultValue = "0") int page, 
                               @RequestParam(defaultValue = "20") int size) {
        return auditService.findAll(PageRequest.of(page, size));
    }

    @GetMapping("/user/{username}")
    public Page<AuditLog> byUser(@PathVariable String username,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size) {
        return auditService.findByUsername(username, PageRequest.of(page, size));
    }

    @GetMapping("/search")
    public List<AuditLog> search(@RequestParam String keyword) {
        return auditService.search(keyword);
    }

    @GetMapping("/range")
    public List<AuditLog> byDateRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return auditService.findByDateRange(start, end);
    }

    @PostMapping("/log")
    public AuditLog manualLog(@RequestBody Map<String, String> request) {
        return auditService.log(
            request.get("action"),
            request.get("resource"),
            request.get("resourceId"),
            request.get("details")
        );
    }
}
