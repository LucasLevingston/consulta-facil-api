package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.exam.ExamRequestResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UploadExamUseCase {

    ExamRequestResponseDTO execute(String examId, String patientUserId, MultipartFile file);
}
