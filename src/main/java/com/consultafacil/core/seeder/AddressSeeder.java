package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.Address;
import com.consultafacil.domain.repository.professional.enrichment.AddressRepository;
import com.consultafacil.domain.repository.user.UserRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressSeeder {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    private record AddressDef(String userId, String zip, String street, String number,
            String district, String city, String state) {
    }

    public void seed(String patientUserId, String professionalUserId, String adminUserId,
            List<String> randomPatientIds) {
        List<AddressDef> fixed = new ArrayList<>();
        if (patientUserId != null)
            fixed.add(new AddressDef(patientUserId, "58000-000", "Rua das Flores", "123", "Centro", "João Pessoa", "PB"));
        if (professionalUserId != null)
            fixed.add(new AddressDef(professionalUserId, "01310-100", "Av. Paulista", "1578", "Bela Vista", "São Paulo", "SP"));
        if (adminUserId != null)
            fixed.add(new AddressDef(adminUserId, "70040-010", "SCS Quadra 2", "Bloco C", "Asa Sul", "Brasília", "DF"));

        for (AddressDef def : fixed) {
            saveAddressIfMissing(def.userId(), def.zip(), def.street(), def.number(), def.district(), def.city(), def.state());
        }

        List<String> cities = List.of("São Paulo", "Rio de Janeiro", "Belo Horizonte",
                "Curitiba", "Porto Alegre", "João Pessoa", "Campina Grande");
        List<String> states = List.of("SP", "RJ", "MG", "PR", "RS", "PB", "PB");

        int created = 0;
        for (int i = 0; i < Math.min(randomPatientIds.size(), 15); i++) {
            int idx = i % cities.size();
            created += saveAddressIfMissing(randomPatientIds.get(i),
                    "58" + String.format("%06d", faker.random().nextInt(999999)),
                    faker.address().streetName(),
                    String.valueOf(faker.random().nextInt(1, 999)),
                    faker.address().streetAddressNumber(),
                    cities.get(idx), states.get(idx)) ? 1 : 0;
        }
        log.info("[Seed] Endereços criados: {}", fixed.size() + created);
    }

    private boolean saveAddressIfMissing(String userId, String zip, String street,
            String number, String district, String city, String state) {
        try {
            if (addressRepository.findByUserId(userId).isPresent()) return false;
            userRepository.findById(userId).ifPresent(user ->
                    addressRepository.save(Address.builder()
                            .user(user).zipCode(zip).street(street).number(number)
                            .district(district).city(city).state(state).build()));
            return true;
        } catch (Exception e) {
            log.debug("Erro ao criar endereço para userId={}: {}", userId, e.getMessage());
            return false;
        }
    }
}
