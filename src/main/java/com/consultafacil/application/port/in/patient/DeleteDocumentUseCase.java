package com.consultafacil.application.port.in.patient;

public interface DeleteDocumentUseCase {

    void execute(String userId, String documentId);
}
