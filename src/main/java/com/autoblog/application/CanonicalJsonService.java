package com.autoblog.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CanonicalJsonService {

    private final ObjectMapper objectMapper;

    public CanonicalJsonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String canonicalize(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(sort(payload));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Payload JSON is invalid", exception);
        }
    }

    public JsonNode parse(String canonicalJson) {
        if (canonicalJson == null) {
            return null;
        }

        try {
            return objectMapper.readTree(canonicalJson);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored payload JSON is invalid", exception);
        }
    }

    private JsonNode sort(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sorted = objectMapper.createObjectNode();
            List<String> fieldNames = new ArrayList<>();
            node.fieldNames().forEachRemaining(fieldNames::add);
            fieldNames.stream()
                    .sorted()
                    .forEach(fieldName -> sorted.set(fieldName, sort(node.get(fieldName))));
            return sorted;
        }

        if (node.isArray()) {
            ArrayNode sorted = objectMapper.createArrayNode();
            node.forEach(item -> sorted.add(sort(item)));
            return sorted;
        }

        return node.deepCopy();
    }
}
