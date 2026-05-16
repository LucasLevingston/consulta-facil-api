package com.example.consulta.core.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String id) {
        super(String.format("%s not found with ID: %s", resource, id));
    }
}
