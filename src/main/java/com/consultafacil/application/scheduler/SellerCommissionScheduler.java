package com.consultafacil.application.scheduler;

import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerCommissionScheduler {

    private final SellerSaleRepositoryPort sellerSaleRepository;

    // Runs at 8:00 AM on the 1st of every month
    @Scheduled(cron = "0 0 8 1 * *")
    @Transactional(readOnly = true)
    public void summarizePreviousMonthCommissions() {
        LocalDate previousMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        List<SellerSale> sales = sellerSaleRepository.findByMonthReference(previousMonth);

        if (sales.isEmpty()) {
            log.info("[SellerCommission] No sales for month {}", previousMonth);
            return;
        }

        Map<String, List<SellerSale>> bySeller = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getSeller().getId()));

        BigDecimal totalPending = BigDecimal.ZERO;
        for (var entry : bySeller.entrySet()) {
            String sellerId = entry.getKey();
            List<SellerSale> sellerSales = entry.getValue();
            BigDecimal pending = sellerSales.stream()
                    .filter(s -> s.getStatus() == SellerSaleStatus.PENDING)
                    .map(SellerSale::getCommissionAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalPending = totalPending.add(pending);

            log.info("[SellerCommission] Month={} sellerId={} sales={} pendingCommission=R${}",
                    previousMonth, sellerId, sellerSales.size(), pending);
        }

        log.info("[SellerCommission] Summary month={} totalSellers={} totalPendingCommission=R${}",
                previousMonth, bySeller.size(), totalPending);
    }
}
