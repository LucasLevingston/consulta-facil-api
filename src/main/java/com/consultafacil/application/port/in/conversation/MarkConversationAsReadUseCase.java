package com.consultafacil.application.port.in.conversation;

public interface MarkConversationAsReadUseCase {

    void execute(String conversationId, String userId);
}
