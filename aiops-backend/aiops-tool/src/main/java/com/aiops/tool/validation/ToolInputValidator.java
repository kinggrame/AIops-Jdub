package com.aiops.tool.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ToolInputValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ValidationResult validate(String inputSchema, Map<String, Object> input) {
        if (inputSchema == null || inputSchema.isEmpty()) {
            return ValidationResult.success();
        }

        try {
            JsonNode schema = objectMapper.readTree(inputSchema);
            List<String> errors = new ArrayList<>();

            if (schema.has("required") && schema.get("required").isArray()) {
                for (JsonNode requiredField : schema.get("required")) {
                    String field = requiredField.asText();
                    if (!input.containsKey(field) || input.get(field) == null) {
                        errors.add("Required field missing: " + field);
                    }
                }
            }

            if (schema.has("properties") && schema.get("properties").isObject()) {
                JsonNode properties = schema.get("properties");
                Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String fieldName = field.getKey();
                    JsonNode fieldSchema = field.getValue();
                    Object fieldValue = input.get(fieldName);

                    if (fieldValue != null) {
                        String type = fieldSchema.has("type") ? fieldSchema.get("type").asText() : null;
                        if (type != null) {
                            String actualType = getActualType(fieldValue);
                            if (!isCompatibleType(actualType, type)) {
                                errors.add(String.format("Field '%s' expected type %s but got %s", 
                                    fieldName, type, actualType));
                            }
                        }

                        if (fieldSchema.has("enum")) {
                            List<String> enumValues = new ArrayList<>();
                            fieldSchema.get("enum").forEach(e -> enumValues.add(e.asText()));
                            if (!enumValues.contains(String.valueOf(fieldValue))) {
                                errors.add(String.format("Field '%s' value must be one of: %s", 
                                    fieldName, enumValues));
                            }
                        }
                    }
                }
            }

            if (errors.isEmpty()) {
                return ValidationResult.success();
            } else {
                return ValidationResult.error(errors);
            }
        } catch (Exception e) {
            return ValidationResult.error(List.of("Schema validation error: " + e.getMessage()));
        }
    }

    private String getActualType(Object value) {
        if (value instanceof Number) {
            if (value instanceof Double || value instanceof Float) {
                return "number";
            }
            return "integer";
        }
        if (value instanceof Boolean) return "boolean";
        if (value instanceof List || value instanceof Map) return "object";
        return "string";
    }

    private boolean isCompatibleType(String actual, String expected) {
        if (actual.equals(expected)) return true;
        if (expected.equals("number") && (actual.equals("integer") || actual.equals("number"))) return true;
        if (expected.equals("object") && (actual.equals("object") || actual.equals("array"))) return true;
        if (expected.equals("string") && actual.equals("integer")) return true;
        return false;
    }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, Collections.emptyList());
        }

        public static ValidationResult error(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
    }
}
