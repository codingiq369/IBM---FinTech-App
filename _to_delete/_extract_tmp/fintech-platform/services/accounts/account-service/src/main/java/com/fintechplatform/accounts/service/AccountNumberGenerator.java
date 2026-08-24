package com.fintechplatform.accounts.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Produces human-readable account numbers. A real bank has a much more
 * involved scheme (routing prefixes, check digits); this is enough to make
 * accounts look and feel real in the demo UI.
 */
@Component
public class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        long candidate = 1_000_000_000L + (long) (RANDOM.nextDouble() * 8_999_999_999L);
        return "ACC-" + candidate;
    }
}
