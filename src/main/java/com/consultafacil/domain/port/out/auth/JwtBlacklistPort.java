package com.consultafacil.domain.port.out.auth;

public interface JwtBlacklistPort {

    void blacklist(String token, long ttlSeconds);

    boolean isBlacklisted(String token);
}
