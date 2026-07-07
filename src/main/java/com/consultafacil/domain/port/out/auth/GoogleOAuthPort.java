package com.consultafacil.domain.port.out.auth;

public interface GoogleOAuthPort {

    record GoogleUserInfo(String sub, String email, String name, String picture) {}

    GoogleUserInfo verifyIdToken(String idToken);

    GoogleUserInfo exchangeCode(String code, String redirectUri);
}
