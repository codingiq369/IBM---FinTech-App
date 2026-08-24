package com.fintechplatform.customer.domain;

/**
 * Know-Your-Customer status. Real banks run this through a compliance
 * pipeline (document checks, sanctions screening, etc). For this learning
 * slice we simulate it with a trivial rule in {@code CustomerService} so the
 * shape of the workflow is visible without building a real KYC engine.
 */
public enum KycStatus {
    PENDING,
    APPROVED,
    REJECTED
}
