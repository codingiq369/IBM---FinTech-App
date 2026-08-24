package com.fintechplatform.customer.dto;

import com.fintechplatform.customer.domain.Customer;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** What we hand back to callers. Never expose the JPA entity directly over HTTP. */
public record CustomerResponse(
        UUID id,
        String fullName,
        String email,
        LocalDate dateOfBirth,
        String kycStatus,
        Instant createdAt) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getDateOfBirth(),
                customer.getKycStatus().name(),
                customer.getCreatedAt());
    }
}
