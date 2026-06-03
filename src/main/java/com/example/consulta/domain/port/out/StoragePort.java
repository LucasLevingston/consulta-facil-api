package com.example.consulta.domain.port.out;

public interface StoragePort {

    String upload(byte[] content, String filename, String contentType, String folder);

    void delete(String key);
}
