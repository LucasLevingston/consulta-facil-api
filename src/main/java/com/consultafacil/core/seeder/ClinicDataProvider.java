package com.consultafacil.core.seeder;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClinicDataProvider {

    public record CityLocation(String city, String state, double lat, double lng) {
    }

    public record ClinicDef(String name, String description, String phone, String address,
            CityLocation location, String imageUrl, String ownerProfileId) {
    }

    public List<ClinicDef> buildDefs(String testProfessionalProfileId, String professionalUserId,
            List<String> extraIds) {
        List<CityLocation> cities = List.of(
                new CityLocation("São Paulo", "SP", -23.5505, -46.6333),
                new CityLocation("Rio de Janeiro", "RJ", -22.9068, -43.1729),
                new CityLocation("Belo Horizonte", "MG", -19.9191, -43.9386),
                new CityLocation("Curitiba", "PR", -25.4290, -49.2671),
                new CityLocation("Porto Alegre", "RS", -30.0346, -51.2177),
                new CityLocation("Brasília", "DF", -15.7942, -47.8822),
                new CityLocation("João Pessoa", "PB", -7.1195, -34.8450),
                new CityLocation("Campina Grande", "PB", -7.2306, -35.8811));

        return List.of(
                new ClinicDef("Clínica Cardio Saúde", "Especializada em cardiologia e prevenção cardiovascular",
                        "(11) 3344-5566", "Av. Paulista, 1578", cities.get(0),
                        "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=600", testProfessionalProfileId),
                new ClinicDef("Instituto Carioca de Saúde", "Atendimento multidisciplinar com foco em qualidade de vida",
                        "(21) 2233-4455", "Rua Visconde de Pirajá, 330", cities.get(1),
                        "https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=600", professionalUserId),
                new ClinicDef("Centro Médico BH", "Clínica geral e especialidades para toda a família",
                        "(31) 3344-7788", "Av. Afonso Pena, 1000", cities.get(2),
                        "https://images.unsplash.com/photo-1504813184591-01572f98c85f?w=600", pickOrFallback(extraIds, 0, testProfessionalProfileId)),
                new ClinicDef("Clínica Curitibana", "Medicina preventiva e diagnóstico avançado",
                        "(41) 3344-9900", "Rua XV de Novembro, 700", cities.get(3),
                        "https://images.unsplash.com/photo-1530497610245-94d3c16cda28?w=600", pickOrFallback(extraIds, 2, testProfessionalProfileId)),
                new ClinicDef("Saúde Sul Clínica", "Atendimento humanizado em Porto Alegre",
                        "(51) 3344-1122", "Av. Independência, 500", cities.get(4),
                        "https://images.unsplash.com/photo-1538108149393-fbbd81895907?w=600", pickOrFallback(extraIds, 4, testProfessionalProfileId)),
                new ClinicDef("Clínica Capital Federal", "Excelência em saúde no coração do Brasil",
                        "(61) 3344-3344", "SCS Quadra 2, Bloco C", cities.get(5),
                        "https://images.unsplash.com/photo-1516549655169-df83a0774514?w=600", pickOrFallback(extraIds, 6, testProfessionalProfileId)),
                new ClinicDef("Clínica Saúde João Pessoa", "Atendimento completo em cardiologia, pediatria e clínica geral",
                        "(83) 3224-5566", "Av. Epitácio Pessoa, 1234", cities.get(6),
                        "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=600", pickOrFallback(extraIds, 8, testProfessionalProfileId)),
                new ClinicDef("Centro de Saúde Paraibano", "Medicina preventiva e especialidades para toda a família em João Pessoa",
                        "(83) 3311-7788", "Rua Cardoso Vieira, 200 — Miramar", cities.get(6),
                        "https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=600", pickOrFallback(extraIds, 10, testProfessionalProfileId)),
                new ClinicDef("Clínica Campina Grande Saúde", "Referência em saúde no Agreste paraibano",
                        "(83) 3322-4411", "Av. Assis Chateaubriand, 500", cities.get(7),
                        "https://images.unsplash.com/photo-1504813184591-01572f98c85f?w=600", pickOrFallback(extraIds, 12, testProfessionalProfileId)),
                new ClinicDef("NutriVida João Pessoa", "Nutrição e fisioterapia integradas para melhor qualidade de vida",
                        "(83) 3244-9900", "Rua Padre Meira, 89 — Tambauzinho", cities.get(6),
                        "https://images.unsplash.com/photo-1530497610245-94d3c16cda28?w=600", pickOrFallback(extraIds, 14, testProfessionalProfileId)));
    }

    private String pickOrFallback(List<String> list, int index, String fallback) {
        return list.size() > index ? list.get(index) : fallback;
    }
}
