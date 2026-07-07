package com.consultafacil.application.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WhatsAppHistoryTrimmer {

    private static final int MAX_HISTORY = 20;

    public List<Map<String, Object>> trim(List<Map<String, Object>> history) {
        if (history.size() <= MAX_HISTORY) {
            return history;
        }
        return new ArrayList<>(history.subList(history.size() - MAX_HISTORY, history.size()));
    }
}
