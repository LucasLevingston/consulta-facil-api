package com.consultafacil.api.dto.examlab;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class ExamLabResponseDTO {

    private String id;
    private String name;
    private String description;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private List<String> acceptedExams;
    private String status;
    private List<HoursEntry> hours;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class HoursEntry {
        private String id;
        private String dayOfWeek;
        private LocalTime openTime;
        private LocalTime closeTime;
        private Integer slotDurationMinutes;
        private Boolean isOpen;
    }
}
