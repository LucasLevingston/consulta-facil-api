package com.example.consulta.domain.port.out;

public interface GoogleOAuthPort {

    record GoogleUserInfo(String sub, String email, String name, String picture) {}

    GoogleUserInfo verifyIdToken(String idToken);
}
