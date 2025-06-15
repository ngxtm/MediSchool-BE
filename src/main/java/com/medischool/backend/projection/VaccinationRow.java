package com.medischool.backend.projection;

import java.time.LocalDate;

public interface VaccinationRow {
    String getCategoryName();        // vc.name
    Integer getDoseNumber();         // vh.dose_number
    LocalDate getVaccinationDate();  // vh.vaccination_date
    String   getLocation();          // vh.location
    String   getVaccineName();
}
