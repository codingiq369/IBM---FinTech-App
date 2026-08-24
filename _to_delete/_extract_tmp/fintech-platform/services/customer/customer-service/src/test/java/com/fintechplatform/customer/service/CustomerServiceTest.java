package com.fintechplatform.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintechplatform.customer.domain.Customer;
import com.fintechplatform.customer.domain.KycStatus;
import com.fintechplatform.customer.dto.CustomerOnboardingRequest;
import com.fintechplatform.customer.repository.CustomerRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Test
    void onboardingAnAdultProducesAnApprovedCustomer() {
        CustomerService service = new CustomerService(customerRepository);
        when(customerRepository.findByEmail("ada@example.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate adultBirthDate = LocalDate.now().minusYears(30);
        Customer saved = service.onboard(new CustomerOnboardingRequest("Ada Lovelace", "ada@example.com", adultBirthDate.toString()));

        assertThat(saved.getKycStatus()).isEqualTo(KycStatus.APPROVED);
    }

    @Test
    void onboardingSomeoneUnder18IsRejectedNotBlocked() {
        // Rejection is a valid outcome of onboarding, not an error: the
        // customer record still gets created so support staff can see why.
        CustomerService service = new CustomerService(customerRepository);
        when(customerRepository.findByEmail("kid@example.com")).thenReturn(Optional.empty());
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        when(customerRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate minorBirthDate = LocalDate.now().minusYears(10);
        service.onboard(new CustomerOnboardingRequest("Kid Example", "kid@example.com", minorBirthDate.toString()));

        assertThat(captor.getValue().getKycStatus()).isEqualTo(KycStatus.REJECTED);
    }

    @Test
    void onboardingWithAnExistingEmailIsRejected() {
        CustomerService service = new CustomerService(customerRepository);
        when(customerRepository.findByEmail("dup@example.com"))
                .thenReturn(Optional.of(new Customer("Existing Person", "dup@example.com", LocalDate.now().minusYears(40), KycStatus.APPROVED)));

        assertThatThrownBy(() -> service.onboard(
                        new CustomerOnboardingRequest("New Person", "dup@example.com", LocalDate.now().minusYears(25).toString())))
                .isInstanceOf(DuplicateCustomerException.class);

        verify(customerRepository, org.mockito.Mockito.never()).save(any());
    }
}
