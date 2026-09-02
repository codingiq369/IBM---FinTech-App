package com.fintechplatform.cardauthorization.repository;

import com.fintechplatform.cardauthorization.domain.ClearingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClearingAccountRepository extends JpaRepository<ClearingAccount, String> {}
