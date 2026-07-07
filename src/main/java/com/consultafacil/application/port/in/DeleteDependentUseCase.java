package com.consultafacil.application.port.in;

public interface DeleteDependentUseCase {

    void execute(String dependentId, String guardianUserId);
}
