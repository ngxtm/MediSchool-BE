package com.medischool.backend.util;

import com.medischool.backend.model.enums.ConsentStatus;
import com.medischool.backend.model.vaccine.VaccinationConsent;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
public final class ConsentStatisticsUtil {
    public static Map<String, Object> calculate(List<VaccinationConsent> consents) {
        long totalConsents = consents.size();
        long respondedConsents = consents.stream()
                .filter(c -> c.getConsentStatus() != null)
                .count();
        long approvedConsents = consents.stream()
                .filter(c -> ConsentStatus.APPROVE.equals(c.getConsentStatus()))
                .count();
        long rejectedConsents = consents.stream()
                .filter(c -> ConsentStatus.REJECT.equals(c.getConsentStatus()))
                .count();
        long pendingConsents = totalConsents - respondedConsents;
        return Map.of(
                "totalConsents", totalConsents,
                "respondedConsents", respondedConsents,
                "approvedConsents", approvedConsents,
                "rejectedConsents", rejectedConsents,
                "pendingConsents", pendingConsents,
                "responseRate", String.format("%.2f%%", (respondedConsents * 100.0) / totalConsents)
        );
    }
}
