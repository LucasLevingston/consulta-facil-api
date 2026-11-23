package com.consultafacil.core.config;

import com.consultafacil.adapter.out.email.NoOpEmailAdapter;
import com.consultafacil.adapter.out.storage.NoOpStorageAdapter;
import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.port.out.StoragePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NoOpAwsConfig {

    @Bean
    @ConditionalOnMissingBean(EmailPort.class)
    public EmailPort noOpEmailPort() {
        return new NoOpEmailAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(StoragePort.class)
    public StoragePort noOpStoragePort() {
        return new NoOpStorageAdapter();
    }
}
