package com.atm.models;

import java.time.LocalDateTime;

/**
 * Fixed deposit product with simple annual interest prorated by tenure.
 */
public final class FixedDeposit {

    private final double principal;
    private final double annualRatePercent;
    private final int tenureMonths;
    private final LocalDateTime openedOn;

    /**
     * Opens a fixed deposit at the bank's standard 6.5% annual rate.
     *
     * @param principal    amount locked in the FD
     * @param tenureMonths tenure in months
     */
    public FixedDeposit(double principal, int tenureMonths) {
        this.principal = principal;
        this.annualRatePercent = 6.5;
        this.tenureMonths = tenureMonths;
        this.openedOn = LocalDateTime.now();
    }

    /**
     * Returns the principal amount.
     *
     * @return principal
     */
    public double getPrincipal() {
        return principal;
    }

    /**
     * Returns the annual interest rate percent.
     *
     * @return annual rate percent
     */
    public double getAnnualRatePercent() {
        return annualRatePercent;
    }

    /**
     * Returns the tenure in months.
     *
     * @return tenure months
     */
    public int getTenureMonths() {
        return tenureMonths;
    }

    /**
     * Returns when the FD was opened.
     *
     * @return open timestamp
     */
    public LocalDateTime getOpenedOn() {
        return openedOn;
    }

    /**
     * Computes maturity amount using simple interest prorated by tenure.
     *
     * @return maturity amount
     */
    public double maturityAmount() {
        return principal * (1 + (annualRatePercent / 100.0) * (tenureMonths / 12.0));
    }

    /**
     * Returns the maturity date based on tenure.
     *
     * @return maturity timestamp
     */
    public LocalDateTime maturityDate() {
        return openedOn.plusMonths(tenureMonths);
    }
}
