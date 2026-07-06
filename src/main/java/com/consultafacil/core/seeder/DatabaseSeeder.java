package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("seed | (!prod & !test & !railway)")
public class DatabaseSeeder implements CommandLineRunner {

    private final Flyway flyway;
    private final PlanSeeder planSeeder;
    private final TestUserSeeder testUserSeeder;
    private final AdminUserSeeder adminUserSeeder;
    private final ServiceAndProcedureRequestSeeder serviceAndProcedureRequestSeeder;
    private final ScheduleSeeder scheduleSeeder;
    private final BulkPatientSeeder bulkPatientSeeder;
    private final BulkProfessionalSeeder bulkProfessionalSeeder;
    private final ClinicSeeder clinicSeeder;
    private final ReceptionistSeeder receptionistSeeder;
    private final AppointmentBulkSeeder appointmentBulkSeeder;
    private final TestAppointmentSeeder testAppointmentSeeder;
    private final OnlineAppointmentSeeder onlineAppointmentSeeder;
    private final BulkProfessionalAppointmentSeeder bulkProfessionalAppointmentSeeder;
    private final MedicalRecordSeeder medicalRecordSeeder;
    private final SubscriptionSeeder subscriptionSeeder;
    private final ClinicalNoteAndExamRequestSeeder clinicalNoteAndExamRequestSeeder;
    private final NotificationSeeder notificationSeeder;
    private final ClinicWorkingHoursSeeder clinicWorkingHoursSeeder;
    private final AddressSeeder addressSeeder;
    private final EmergencyContactSeeder emergencyContactSeeder;
    private final AnamnesisSeeder anamnesisSeeder;
    private final SellerSeeder sellerSeeder;
    private final SellerSaleSeeder sellerSaleSeeder;
    private final CouponSeeder couponSeeder;
    private final CouponUseSeeder couponUseSeeder;
    private final SubscriptionPaymentSeeder subscriptionPaymentSeeder;
    private final ExamLabSeeder examLabSeeder;
    private final BillingPaymentSeeder billingPaymentSeeder;

    @Override
    public void run(String... args) {
        flyway.clean();
        flyway.migrate();
        try {
            planSeeder.seed();
            String patientUserId = testUserSeeder.createPatient("patient@example.com", "12345678", "Paciente Teste", "00000000001", "https://i.pravatar.cc/150?img=1");
            String professionalProfileId = testUserSeeder.createProfessional("professional@example.com", "12345678", "Profissional Teste", "00000000002", ProfessionalType.MEDICO, Specialty.CARDIOLOGIA, "CRM-TESTE-001");
            String professionalUserId = testUserSeeder.getUserIdForProfile(professionalProfileId);
            String adminUserId = adminUserSeeder.createAdmin("admin@example.com", "admin1234", "Admin Teste", "00000000003");
            serviceAndProcedureRequestSeeder.seed(professionalUserId, patientUserId);
            scheduleSeeder.seedSchedule(professionalUserId, ScheduleTemplate.FULL_WEEK);

            List<String> patientUserIds = bulkPatientSeeder.seed(20);
            List<String> professionalProfileIds = bulkProfessionalSeeder.seed(52);
            bulkProfessionalSeeder.approveAll(professionalProfileIds);
            scheduleSeeder.seedForProfessionals(professionalProfileIds);
            String firstClinicId = clinicSeeder.seed(professionalProfileId, professionalUserId, professionalProfileIds);
            String professionalOwnerUserId = testUserSeeder.getUserIdForProfile(professionalProfileId);
            if (firstClinicId != null && professionalOwnerUserId != null) {
                receptionistSeeder.seed("receptionist@example.com", "12345678", "Recepcionista Teste", "00000000004", firstClinicId, professionalOwnerUserId);
            }

            appointmentBulkSeeder.seed(patientUserIds, professionalProfileIds);
            testAppointmentSeeder.seed(patientUserId, professionalProfileId);
            onlineAppointmentSeeder.seed(patientUserId, professionalProfileId);
            bulkProfessionalAppointmentSeeder.seed(professionalProfileId, patientUserIds);
            medicalRecordSeeder.seed(patientUserIds);
            subscriptionSeeder.seed(professionalUserId, professionalProfileIds);
            clinicalNoteAndExamRequestSeeder.seed();
            notificationSeeder.seed(patientUserIds, professionalProfileIds);
            clinicWorkingHoursSeeder.seed();
            addressSeeder.seed(patientUserId, professionalUserId, adminUserId, patientUserIds);
            emergencyContactSeeder.seed(patientUserId, patientUserIds);
            anamnesisSeeder.seed();
            sellerSeeder.seed(adminUserId, professionalUserId, professionalProfileIds);
            sellerSaleSeeder.seed();
            List<Coupon> coupons = couponSeeder.seed(adminUserId);
            couponUseSeeder.seed(coupons);
            subscriptionPaymentSeeder.seed();
            examLabSeeder.seed();
            billingPaymentSeeder.seed(patientUserId, professionalUserId, patientUserIds);
        } catch (Exception e) {
            log.error("Erro durante o seed:", e);
        }
    }
}
