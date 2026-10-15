package com.capgemini.freebirds.model;

public enum CancellationPolicy {
    FREE(0, true, "Cancel for no additional fee"),
    ADMIN_COSTS(25, true, "Administrative fees"),
    LATE_CXL(50, true, "Cancellation fees"),
    NO_REFUND(100, true, "No refund!");

    private final int feePercent;
    private final boolean cancellable;
    private final String description;

    CancellationPolicy(int feePercent, boolean cancellable, String description) {
        this.feePercent = feePercent;
        this.cancellable = cancellable;
        this.description = description;
    }

    public int getFeePercent() { return feePercent; }
    public boolean isCancellable() { return cancellable; }
    public String getDescription() { return description; }

    public static CancellationPolicy hoursUntilStart(long hours) {
        if (hours >= 24) return FREE;
        if (hours >= 12) return ADMIN_COSTS;
        if (hours >= 4) return LATE_CXL;
        return NO_REFUND;
    }
}
