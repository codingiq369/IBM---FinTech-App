package com.fintechplatform.cardmanagement.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Produces a synthetic, display-safe card number: the standard 16-digit
 * layout, masked down to the last four digits (e.g. "•••• •••• •••• 4242")
 * plus the last four on their own. There is no real PAN, Luhn check digit,
 * or BIN behind this — it exists to make the demo UI look and feel real,
 * exactly like AccountNumberGenerator does for bank account numbers. A real
 * issuer would never generate or hold a PAN like this outside a PCI-DSS
 * scoped vault; see docs/domains/cards.md.
 */
@Component
public class CardNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public Generated generate() {
        String lastFour = String.format("%04d", RANDOM.nextInt(10_000));
        String masked = "•••• •••• •••• " + lastFour;
        return new Generated(masked, lastFour);
    }

    public record Generated(String masked, String lastFour) {}
}
