package com.consultafacil.application.scheduler;

import com.consultafacil.application.port.in.ProcessAvailableCommissionsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionReleaseScheduler {

    private final ProcessAvailableCommissionsUseCase processAvailableCommissionsUseCase;

    @Scheduled(cron = "0 0 3 * * *")
    public void releaseCommissions() {
        log.info("[Scheduler] Processando comissões disponíveis...");
        try {
            processAvailableCommissionsUseCase.execute();
        } catch (Exception e) {
            log.error("[Scheduler] Erro ao processar comissões: {}", e.getMessage());
        }
    }
}
