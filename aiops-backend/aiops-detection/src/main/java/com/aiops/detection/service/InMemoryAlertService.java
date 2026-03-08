package com.aiops.detection.service;

import com.aiops.detection.entity.Alert;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class InMemoryAlertService implements AlertService {

    private final List<Alert> alerts = new CopyOnWriteArrayList<>();

    @Override
    public List<Alert> evaluate(String hostname, Map<String, Object> metrics, List<Map<String, Object>> events) {
        List<Alert> generated = new ArrayList<>();
        generated.addAll(generateMetricAlert(hostname, "cpu.usage", metrics, 90, "critical"));
        generated.addAll(generateMetricAlert(hostname, "memory.usage", metrics, 85, "warning"));

        for (Map<String, Object> event : events) {
            String target = String.valueOf(event.getOrDefault("target", "server"));
            if ("ai".equalsIgnoreCase(target)) {
                double value = number(event.get("value"));
                String metric = String.valueOf(event.getOrDefault("metric", "unknown"));
                generated.add(create(new Alert(
                        UUID.randomUUID().toString(),
                        hostname,
                        "event",
                        metric,
                        value >= 90 ? "critical" : "warning",
                        value,
                        value,
                        "Trigger event routed to AI for further analysis",
                        "open",
                        Instant.now()
                )));
            }
        }
        return generated;
    }

    @Override
    public Alert create(Alert alert) {
        alerts.add(alert);
        return alert;
    }

    @Override
    public List<Alert> list() {
        return List.copyOf(alerts);
    }

    private List<Alert> generateMetricAlert(String hostname, String path, Map<String, Object> metrics, double threshold, String severity) {
        double value = nestedMetric(metrics, path);
        if (value < threshold) {
            return List.of();
        }
        return List.of(create(new Alert(
                UUID.randomUUID().toString(),
                hostname,
                "metric",
                path,
                severity,
                value,
                threshold,
                "Metric threshold exceeded",
                "open",
                Instant.now()
        )));
    }

    private double nestedMetric(Map<String, Object> metrics, String path) {
        String[] parts = path.split("\\.");
        Object current = metrics;
        for (String part : parts) {
            if (!(current instanceof Map<?, ?> map)) {
                return 0;
            }
            current = map.get(part);
        }
        return number(current);
    }

    private double number(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null ? 0 : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
