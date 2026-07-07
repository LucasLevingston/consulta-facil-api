package com.consultafacil.application.port.in.auth;

public interface RequestMagicLinkUseCase {
    void execute(String email);
}
