package com.consultafacil.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WhatsAppHistoryManager {

    private static final int MAX_HISTORY = 20;

    private final ObjectMapper objectMapper;

    public List<Map<String, Object>> deserialize(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> trim(List<Map<String, Object>> history) {
        if (history.size() <= MAX_HISTORY) {
            return history;
        }
        return new ArrayList<>(history.subList(history.size() - MAX_HISTORY, history.size()));
    }

    public String serialize(List<Map<String, Object>> history) throws Exception {
        return objectMapper.writeValueAsString(history);
    }
}
