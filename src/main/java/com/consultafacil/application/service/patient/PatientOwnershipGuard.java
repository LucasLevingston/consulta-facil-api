package com.consultafacil.application.service.patient;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PatientOwnershipGuard {

    public void assertOwnership(String ownerProfileId, String requestorProfileId) {
        if (!ownerProfileId.equals(requestorProfileId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }
    }
}
