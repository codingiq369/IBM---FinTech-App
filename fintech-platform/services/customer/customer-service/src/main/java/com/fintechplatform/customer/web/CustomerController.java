package com.fintechplatform.customer.web;

import com.fintechplatform.customer.domain.Customer;
import com.fintechplatform.customer.dto.CustomerOnboardingRequest;
import com.fintechplatform.customer.dto.CustomerResponse;
import com.fintechplatform.customer.service.CustomerService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> onboard(@Valid @RequestBody CustomerOnboardingRequest request) {
        Customer customer = customerService.onboard(request);
        CustomerResponse body = CustomerResponse.from(customer);
        return ResponseEntity.created(URI.create("/api/customers/" + customer.getId())).body(body);
    }

    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable UUID id) {
        return CustomerResponse.from(customerService.getById(id));
    }

    /**
     * Used by accounts-service to check "is this a real, KYC-approved
     * customer" before it lets someone open an account. Deliberately a tiny,
     * boolean-shaped endpoint rather than making the caller fetch and
     * interpret the full customer record.
     */
    @GetMapping("/{id}/approved")
    public ApprovalStatus isApproved(@PathVariable UUID id) {
        return new ApprovalStatus(customerService.isApprovedCustomer(id));
    }

    public record ApprovalStatus(boolean approved) {}
}
