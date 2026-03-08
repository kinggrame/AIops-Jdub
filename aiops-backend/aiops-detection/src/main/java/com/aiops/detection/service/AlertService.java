package com.aiops.detection.service;

import com.aiops.detection.entity.Alert;

import java.util.List;
import java.util.Map;

/**
 * Alert service.
 *
 * <p>Evaluates incoming metrics and events, creates alerts and exposes alert state.
 * The MVP keeps alert rules simple and deterministic so the end-to-end flow is stable.</p>
 *
 * <p><b>Asynchronous notes:</b></p>
 * <ul>
 *   <li>Rule evaluation is synchronous in the MVP.</li>
 *   <li>TODO: add asynchronous evaluation and MQ-based notification fan-out.</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface AlertService {

    /**
     * Evaluates metrics and trigger events.
     *
     * @param hostname host name
     * @param metrics metrics payload
     * @param events event payload
     * @return generated alerts
     */
    List<Alert> evaluate(String hostname, Map<String, Object> metrics, List<Map<String, Object>> events);

    /**
     * Persists an alert.
     *
     * @param alert alert instance
     * @return stored alert
     */
    Alert create(Alert alert);

    List<Alert> list();
}
