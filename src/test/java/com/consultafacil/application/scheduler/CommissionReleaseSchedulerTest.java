package com.consultafacil.application.scheduler;

import com.consultafacil.application.port.in.commission.ProcessAvailableCommissionsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommissionReleaseSchedulerTest {

    @Mock ProcessAvailableCommissionsUseCase processAvailableCommissionsUseCase;

    CommissionReleaseScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new CommissionReleaseScheduler(processAvailableCommissionsUseCase);
    }

    @Test
    void releaseCommissions_delegatesToUseCase() {
        scheduler.releaseCommissions();

        verify(processAvailableCommissionsUseCase).execute();
    }

    @Test
    void releaseCommissions_exceptionIsSwallowed_doesNotPropagate() {
        doThrow(new RuntimeException("DB timeout")).when(processAvailableCommissionsUseCase).execute();

        assertThatCode(() -> scheduler.releaseCommissions()).doesNotThrowAnyException();
    }
}
