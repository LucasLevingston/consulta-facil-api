package com.consultafacil.application.port.in.examrequest;

public interface CancelExamSchedulingUseCase {
    void execute(String schedulingId, String userId);
}
