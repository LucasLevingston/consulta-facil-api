package com.consultafacil.application.port.in;

public interface CancelExamSchedulingUseCase {
    void execute(String schedulingId, String userId);
}
