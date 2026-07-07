package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.ExamType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_labs", indexes = {
    @Index(name = "idx_exam_labs_city", columnList = "city"),
    @Index(name = "idx_exam_labs_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExamLab {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;

    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @ElementCollection(targetClass = ExamType.class)
    @CollectionTable(name = "exam_lab_accepted_exams", joinColumns = @JoinColumn(name = "exam_lab_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_name")
    @Builder.Default
    @ToString.Exclude
    private List<ExamType> acceptedExams = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "examLab", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ExamLabHours> hours = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
