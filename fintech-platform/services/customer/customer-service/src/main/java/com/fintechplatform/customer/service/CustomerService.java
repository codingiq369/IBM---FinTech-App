package com.fintechplatform.customer.service;

import com.fintechplatform.customer.domain.Customer;
import com.fintechplatform.customer.domain.KycStatus;
import com.fintechplatform.customer.dto.CustomerOnboardingRequest;
import com.fintechplatform.customer.repository.CustomerRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    /** Simplified KYC rule: real onboarding would call out to a compliance
     * provider (document verification, sanctions lists, etc). We only check
     * age, which is enough to show the workflow shape: onboarding can result
     * in either an approved or a rejected customer, and callers need to
     * handle both. */
    private static final int MINIMUM_AGE_YEARS = 18;

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer onboard(CustomerOnboardingRequest request) {
        customerRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new DuplicateCustomerException(request.email());
        });

        LocalDate dateOfBirth = request.parsedDateOfBirth();
        KycStatus kycStatus = meetsMinimumAge(dateOfBirth) ? KycStatus.APPROVED : KycStatus.REJECTED;

        Customer customer = new Customer(request.fullName(), request.email(), dateOfBirth, kycStatus);
        return customerRepository.save(customer);
    }

    public Customer getById(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public boolean isApprovedCustomer(UUID id) {
        return customerRepository.findById(id)
                .map(customer -> customer.getKycStatus() == KycStatus.APPROVED)
                .orElse(false);
    }

    private boolean meetsMinimumAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears() >= MINIMUM_AGE_YEARS;
    }
}
