package com.consultafacil.api.dto.professional;

import com.consultafacil.domain.enums.PaymentMethod;
import com.consultafacil.domain.enums.PaymentTiming;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalResponseDTO {
    private String id;
    private String userId;
    private String name;
    private String email;
    private String profession;
    private String specialty;
    private String licenseNumber;
    private String phone;
    private String imageUrl;
    private Double rating;
    private Integer consultationCount;
    private ProfessionalProfileStatus status;
    private String city;
    private String state;
    private String address;
    private Double latitude;
    private Double longitude;
    private String clinicId;
    private String clinicName;
    private BigDecimal consultationPrice;
    private Set<PaymentMethod> acceptedPaymentMethods;
    private PaymentTiming paymentTiming;
    private String instagramUrl;
    private String linkedinUrl;
    private String websiteUrl;
}
