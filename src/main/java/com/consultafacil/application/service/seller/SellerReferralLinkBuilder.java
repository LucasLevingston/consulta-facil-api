package com.consultafacil.application.service.seller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SellerReferralLinkBuilder {

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    public String build(String slug) {
        return appUrl + "/ref/" + slug;
    }
}
