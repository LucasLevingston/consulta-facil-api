package com.consultafacil.core.seeder;

import com.github.javafaker.Faker;

public final class CpfGeneratorUtils {

    private CpfGeneratorUtils() {
    }

    public static String generateFakeCPF(Faker faker) {
        StringBuilder cpf = new StringBuilder();
        for (int i = 0; i < 11; i++) cpf.append(faker.random().nextInt(0, 9));
        return cpf.toString();
    }
}
