package com.example.consulta.domain.port.out;

import org.springframework.web.multipart.MultipartFile;

public interface StoragePort {

    String upload(MultipartFile file, String folder);

    void delete(String key);
}
