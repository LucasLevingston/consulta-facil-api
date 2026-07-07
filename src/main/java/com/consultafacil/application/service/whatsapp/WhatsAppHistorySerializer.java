package com.consultafacil.application.service.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WhatsAppHistorySerializer {

    private final ObjectMapper objectMapper;

    public String serialize(List<Map<String, Object>> history) throws Exception {
        return objectMapper.writeValueAsString(history);
    }
}
