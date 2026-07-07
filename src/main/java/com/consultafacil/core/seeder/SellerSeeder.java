package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.repository.professional.profile.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.seller.SellerRepository;
import com.consultafacil.domain.repository.user.UserRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerSeeder {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed(String adminUserId, String professionalUserId, List<String> professionalProfileIds) {
        if (professionalUserId != null && !sellerRepository.existsByUserId(professionalUserId)) {
            try {
                userRepository.findById(professionalUserId).ifPresent(user ->
                        sellerRepository.save(Seller.builder()
                                .user(user)
                                .slug("prof-teste")
                                .commissionRate(new BigDecimal("15.00"))
                                .status(SellerStatus.ACTIVE)
                                .pixKey("professional@example.com")
                                .notes("Vendedor de teste criado pelo seed")
                                .build()));
                log.info("[Seed] Seller de teste criado para professional@example.com");
            } catch (Exception e) {
                log.warn("Erro ao criar seller de teste: {}", e.getMessage());
            }
        }

        int created = 0;
        for (int i = 0; i < Math.min(professionalProfileIds.size(), 8); i++) {
            if (i % 3 != 0) continue;
            final int idx = i;
            try {
                String userId = professionalProfileRepository.findById(professionalProfileIds.get(idx))
                        .map(p -> p.getUser().getId())
                        .orElse(null);
                if (userId == null || sellerRepository.existsByUserId(userId)) continue;

                String slug = "afiliado-" + (i + 1);
                if (sellerRepository.existsBySlug(slug)) continue;

                userRepository.findById(userId).ifPresent(user -> {
                    SellerStatus status = faker.random().nextInt(100) < 80
                            ? SellerStatus.ACTIVE : SellerStatus.INACTIVE;
                    sellerRepository.save(Seller.builder()
                            .user(user)
                            .slug(slug)
                            .commissionRate(BigDecimal.valueOf(10 + faker.random().nextInt(11)))
                            .status(status)
                            .pixKey(user.getEmail())
                            .build());
                });
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar seller random: {}", e.getMessage());
            }
        }
        log.info("[Seed] Sellers adicionais criados: {}", created);
    }
}
