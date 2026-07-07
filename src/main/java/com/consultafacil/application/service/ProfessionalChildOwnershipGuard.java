package com.consultafacil.application.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProfessionalChildOwnershipGuard {

    public void assertOwnedBy(String ownerProfileId, String requestorProfileId) {
        if (!ownerProfileId.equals(requestorProfileId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }
    }
}
