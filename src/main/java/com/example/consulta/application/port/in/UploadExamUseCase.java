package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.exam.ExamRequestResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UploadExamUseCase {

    ExamRequestResponseDTO execute(String examId, String patientUserId, MultipartFile file);
}
