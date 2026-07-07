package com.consultafacil.core.seeder;

import com.consultafacil.domain.enums.ExamType;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
public class ExamLabDataProvider {

    public record LabDef(String name, String description, String phone, String address,
            String city, String state, double lat, double lng, String imageUrl,
            List<ExamType> acceptedExams) {
    }

    public record DaySlot(String day, LocalTime open, LocalTime close, int duration, boolean isOpen) {
    }

    public List<LabDef> getLabs() {
        return List.of(
                new LabDef("Laboratório Saúde Total", "Exames laboratoriais completos com resultados online em 24h", "(83) 3224-1100", "Av. Epitácio Pessoa, 2490 — Bessa", "João Pessoa", "PB", -7.1105, -34.8239, "https://images.unsplash.com/photo-1579165466741-7f35e4755182?w=600", List.of(ExamType.HEMOGRAMA_COMPLETO, ExamType.GLICEMIA_JEJUM, ExamType.TSH, ExamType.COLESTEROL_TOTAL, ExamType.CREATININA, ExamType.TGO, ExamType.TGP, ExamType.PSA, ExamType.VITAMINA_D)),
                new LabDef("Clínica Diagnóstica Paraibana", "Imagem e análises clínicas de alta precisão", "(83) 3311-4422", "Rua Cardoso Vieira, 180 — Miramar", "João Pessoa", "PB", -7.1158, -34.8611, "https://images.unsplash.com/photo-1581595220892-b0739db3ba8c?w=600", List.of(ExamType.RAIO_X, ExamType.ULTRASSOM_ABDOMINAL, ExamType.ULTRASSOM_PELVICO, ExamType.ECOCARDIOGRAMA, ExamType.HOLTER_24H, ExamType.MAPA, ExamType.TOMOGRAFIA)),
                new LabDef("LabClin Campina Grande", "Referência em análises clínicas no Agreste paraibano", "(83) 3322-5500", "Av. Assis Chateaubriand, 1200", "Campina Grande", "PB", -7.2258, -35.8811, "https://images.unsplash.com/photo-1530026405186-ed1f139313f8?w=600", List.of(ExamType.HEMOGRAMA_COMPLETO, ExamType.GLICEMIA_JEJUM, ExamType.HEMOGLOBINA_GLICADA, ExamType.COLESTEROL_TOTAL, ExamType.UROCULTURA, ExamType.URINA_TIPO_I, ExamType.SOROLOGIAS_HIV)),
                new LabDef("Laboratório Einstein SP", "Exames de alta complexidade com tecnologia de ponta", "(11) 3747-1000", "Av. Albert Einstein, 627 — Morumbi", "São Paulo", "SP", -23.5978, -46.7195, "https://images.unsplash.com/photo-1576765608866-5b51046452be?w=600", List.of(ExamType.HEMOGRAMA_COMPLETO, ExamType.GLICEMIA_JEJUM, ExamType.TSH, ExamType.COLESTEROL_TOTAL, ExamType.RAIO_X, ExamType.RESSONANCIA_MAGNETICA, ExamType.TOMOGRAFIA, ExamType.PET_CT)),
                new LabDef("Centro de Imagem Rio", "Diagnóstico por imagem no Rio de Janeiro", "(21) 2253-7700", "Rua Visconde de Pirajá, 550 — Ipanema", "Rio de Janeiro", "RJ", -22.9840, -43.2053, "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=600", List.of(ExamType.ULTRASSONOGRAFIA, ExamType.RAIO_X, ExamType.ECOCARDIOGRAMA, ExamType.HOLTER_24H, ExamType.MAPA, ExamType.MAMOGRAFIA, ExamType.DENSITOMETRIA_OSSEA)));
    }

    public List<DaySlot> getWeekdays() {
        return List.of(
                new DaySlot("MONDAY", LocalTime.of(7, 0), LocalTime.of(18, 0), 30, true),
                new DaySlot("TUESDAY", LocalTime.of(7, 0), LocalTime.of(18, 0), 30, true),
                new DaySlot("WEDNESDAY", LocalTime.of(7, 0), LocalTime.of(18, 0), 30, true),
                new DaySlot("THURSDAY", LocalTime.of(7, 0), LocalTime.of(18, 0), 30, true),
                new DaySlot("FRIDAY", LocalTime.of(7, 0), LocalTime.of(17, 0), 30, true),
                new DaySlot("SATURDAY", LocalTime.of(7, 0), LocalTime.of(12, 0), 30, true),
                new DaySlot("SUNDAY", LocalTime.of(7, 0), LocalTime.of(12, 0), 30, false));
    }
}
