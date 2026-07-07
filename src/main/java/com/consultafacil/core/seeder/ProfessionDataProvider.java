package com.consultafacil.core.seeder;

import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProfessionDataProvider {

    public record ProfessionData(ProfessionalType profession, Specialty specialty, String licensePrefix) {
    }

    private final List<ProfessionData> data = List.of(
            new ProfessionData(ProfessionalType.MEDICO, Specialty.CARDIOLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.CLINICA_GERAL, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.DERMATOLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.ENDOCRINOLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.GASTROENTEROLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.NEUROLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.OFTALMOLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.ORTOPEDIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.PEDIATRIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.PNEUMOLOGIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.PSIQUIATRIA, "CRM/SP"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.CARDIOLOGIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.CLINICA_GERAL, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.PEDIATRIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.GINECOLOGIA_OBSTETRICIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.ORTOPEDIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.NEUROLOGIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.MEDICO, Specialty.PSIQUIATRIA, "CRM/PB"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_CLINICA, "CRN/SP"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_ESPORTIVA, "CRN/SP"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_ONCOLOGICA, "CRN/SP"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_MATERNO_INFANTIL, "CRN/SP"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_FUNCIONAL, "CRN/SP"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_CLINICA, "CRN/PB"),
            new ProfessionData(ProfessionalType.NUTRICIONISTA, Specialty.NUTRICAO_ESPORTIVA, "CRN/PB"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_ORTOPEDICA, "CREFITO/SP"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_NEUROLOGICA, "CREFITO/SP"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_RESPIRATORIA, "CREFITO/SP"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_DESPORTIVA, "CREFITO/SP"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.PILATES_CLINICO, "CREFITO/SP"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_ORTOPEDICA, "CREFITO/PB"),
            new ProfessionData(ProfessionalType.FISIOTERAPEUTA, Specialty.FISIOTERAPIA_NEUROLOGICA, "CREFITO/PB"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.TCC, "CRP/SP"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.PSICANALISE, "CRP/SP"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.PSICOLOGIA_INFANTIL, "CRP/SP"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.PSICOLOGIA_ORGANIZACIONAL, "CRP/SP"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.PSICOLOGIA_DO_ESPORTE, "CRP/SP"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.TCC, "CRP/PB"),
            new ProfessionData(ProfessionalType.PSICOLOGO, Specialty.PSICANALISE, "CRP/PB"),
            new ProfessionData(ProfessionalType.DENTISTA, Specialty.ODONTOLOGIA_GERAL, "CRO/PB"),
            new ProfessionData(ProfessionalType.DENTISTA, Specialty.ORTODONTIA, "CRO/PB"),
            new ProfessionData(ProfessionalType.DENTISTA, Specialty.IMPLANTODONTIA, "CRO/PB"));

    public List<ProfessionData> getAll() {
        return data;
    }
}
