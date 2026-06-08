package com.consultafacil.domain.enums;

/**
 * All named permissions in the system.
 * Evaluated by PolicyService — never check roles directly in controllers.
 */
public enum Permission {

    // ── Appointments ─────────────────────────────────────────────────────
    APPOINTMENT_SCHEDULE,
    APPOINTMENT_VIEW_AS_PATIENT,
    APPOINTMENT_VIEW_AS_PROFESSIONAL,
    APPOINTMENT_CONFIRM,
    APPOINTMENT_RESCHEDULE,
    APPOINTMENT_CANCEL,
    APPOINTMENT_COMPLETE,
    APPOINTMENT_RATE,
    APPOINTMENT_DELETE,
    APPOINTMENT_GENERATE_CHECKIN_TOKEN,
    APPOINTMENT_CHECKIN,
    APPOINTMENT_VIEW_QUEUE,
    APPOINTMENT_CALL_PATIENT,
    APPOINTMENT_SET_MODALITY,
    APPOINTMENT_GENERATE_MEET_LINK,
    APPOINTMENT_VIEW_ANAMNESIS,
    APPOINTMENT_SAVE_ANAMNESIS,
    APPOINTMENT_VIEW_CLINICAL_NOTE,
    APPOINTMENT_SAVE_CLINICAL_NOTE,

    // ── Clinics ───────────────────────────────────────────────────────────
    CLINIC_MANAGE,

    // ── Professionals ─────────────────────────────────────────────────────
    PROFESSIONAL_CREATE_PROFILE,
    PROFESSIONAL_LIST,
    PROFESSIONAL_VIEW_OWN_PROFILE,
    PROFESSIONAL_ADMIN_MANAGE,     // approve / reject / update status

    // ── Patients ──────────────────────────────────────────────────────────
    PATIENT_PROFILE_VIEW,          // professional/admin viewing a patient
    PATIENT_PROFILE_MANAGE,        // patient managing own profile

    // ── Exam & Procedure Requests ─────────────────────────────────────────
    EXAM_REQUEST_MANAGE,
    EXAM_REQUEST_VIEW,
    EXAM_REQUEST_REVIEW_AS_PATIENT,

    PROCEDURE_REQUEST_MANAGE,
    PROCEDURE_REQUEST_VIEW,
    PROCEDURE_REQUEST_REVIEW_AS_PATIENT,

    // ── Professional Services ─────────────────────────────────────────────
    PROFESSIONAL_SERVICE_MANAGE,

    // ── Users ─────────────────────────────────────────────────────────────
    USER_VIEW_PROFILE,
    USER_UPDATE_PROFILE,
    USER_ADMIN_LIST,
    USER_ADMIN_UPDATE,

    // ── Payments & Billing ────────────────────────────────────────────────
    PAYMENT_CHECKOUT,
    BILLING_VIEW,

    // ── Notifications ─────────────────────────────────────────────────────
    NOTIFICATION_ACCESS,

    // ── Admin panel ───────────────────────────────────────────────────────
    ADMIN_ACCESS,
}
