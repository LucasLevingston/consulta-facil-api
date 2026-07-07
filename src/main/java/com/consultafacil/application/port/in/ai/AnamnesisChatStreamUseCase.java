package com.consultafacil.application.port.in.ai;

import com.consultafacil.api.dto.ai.ChatMessage;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

public interface AnamnesisChatStreamUseCase {
    StreamingResponseBody execute(List<ChatMessage> messages);
}
