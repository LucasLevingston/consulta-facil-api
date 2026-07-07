package com.consultafacil.application.port.in;

public interface DeleteDocumentUseCase {

    void execute(String userId, String documentId);
}
