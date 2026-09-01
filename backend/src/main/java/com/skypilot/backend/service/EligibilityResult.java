package com.skypilot.backend.service;

import java.util.ArrayList;
import java.util.List;

public class EligibilityResult {
    private final boolean eligible;
    private final List<String> reasons;

    public EligibilityResult(boolean eligible, List<String> reasons) {
        this.eligible = eligible;
        this.reasons = new ArrayList<>(reasons == null ? List.of() : reasons);
    }

    public boolean isEligible() {
        return eligible;
    }

    public List<String> getReasons() {
        return reasons;
    }
}
