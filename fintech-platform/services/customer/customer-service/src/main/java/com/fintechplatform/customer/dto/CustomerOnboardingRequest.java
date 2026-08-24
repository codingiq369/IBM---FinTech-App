package com.fintechplatform.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/** What a caller sends us to onboard a brand-new customer. */
public record CustomerOnboardingRequest(
        @NotBlank(message = "fullName is required") String fullName,
        @NotBlank(message = "email is required") @Email(message = "email must be a valid address") String email,
        @NotBlank(message = "dateOfBirth is required (yyyy-MM-dd)") String dateOfBirth) {

    public LocalDate parsedDateOfBirth() {
        return LocalDate.parse(dateOfBirth);
    }
}
