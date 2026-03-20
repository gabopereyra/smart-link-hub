package org.gabo.infrastructure.generator;

import jakarta.enterprise.context.ApplicationScoped;
import org.gabo.domain.generator.AliasGenerator;

import java.security.SecureRandom;

@ApplicationScoped
public class Base62AliasGenerator implements AliasGenerator {
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ALIAS_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder alias = new StringBuilder(ALIAS_LENGTH);
        for (int i = 0; i < ALIAS_LENGTH; i++) {
            alias.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }
        return alias.toString();
    }
}
