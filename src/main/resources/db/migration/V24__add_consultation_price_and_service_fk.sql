ALTER TABLE professional_profiles ADD COLUMN consultation_price DECIMAL(10, 2);

ALTER TABLE appointments ADD COLUMN service_id VARCHAR(36);
ALTER TABLE appointments ADD CONSTRAINT fk_appt_service FOREIGN KEY (service_id) REFERENCES professional_services(id);
