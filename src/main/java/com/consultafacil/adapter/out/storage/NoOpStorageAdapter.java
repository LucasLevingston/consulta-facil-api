package com.consultafacil.adapter.out.storage;

import com.consultafacil.domain.port.out.StoragePort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpStorageAdapter implements StoragePort {

    @Override
    public String upload(byte[] content, String filename, String contentType, String folder) {
        log.warn("[Storage] AWS desabilitado — upload ignorado para {}/{}", folder, filename);
        return "";
    }

    @Override
    public void delete(String key) {
        log.warn("[Storage] AWS desabilitado — delete ignorado para {}", key);
    }
}
