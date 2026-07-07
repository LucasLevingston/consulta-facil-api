package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalRatingDTO;
import com.consultafacil.application.port.in.GetProfessionalRatingsUseCase;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProfessionalRatingsService implements GetProfessionalRatingsUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final RatingDistributionBuilder ratingDistributionBuilder;

    @Override
    @Transactional(readOnly = true)
    public ProfessionalRatingDTO execute(String professionalId) {
        Double average = appointmentRepository.findAverageRatingByProfessionalId(professionalId);
        List<Object[]> rows = appointmentRepository.findRatingDistributionByProfessionalId(professionalId);
        return ratingDistributionBuilder.build(average, rows);
    }
}
