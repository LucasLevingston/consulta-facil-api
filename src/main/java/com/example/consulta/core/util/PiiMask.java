package com.example.consulta.core.util;

public final class PiiMask {

    private PiiMask() {}

    public static String maskEmail(String email) {
        if (email == null) return "[null]";
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        String local = email.substring(0, at);
        String domain = email.substring(at + 1);
        int dot = domain.lastIndexOf('.');
        String maskedLocal = local.charAt(0) + "***";
        String maskedDomain = dot > 0
                ? domain.charAt(0) + "***" + domain.substring(dot)
                : "***";
        return maskedLocal + "@" + maskedDomain;
    }

    public static String maskPhone(String phone) {
        if (phone == null) return "[null]";
        if (phone.length() <= 8) return "***";
        return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 4);
    }
}
