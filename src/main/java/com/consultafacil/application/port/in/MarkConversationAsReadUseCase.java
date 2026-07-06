package com.consultafacil.application.port.in;

public interface MarkConversationAsReadUseCase {

    void execute(String conversationId, String userId);
}
