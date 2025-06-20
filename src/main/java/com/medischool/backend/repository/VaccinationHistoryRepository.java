package com.medischool.backend.repository;

import com.medischool.backend.model.Vaccine.VaccinationHistory;
import com.medischool.backend.projection.VaccinationRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface VaccinationHistoryRepository extends JpaRepository<VaccinationHistory, Integer> {

    @Query(
            value = """
            SELECT  vc.name            AS categoryName,
                    vh.dose_number     AS doseNumber,
                    vh.vaccination_date AS vaccinationDate,
                    vh.location        AS location,
                    v.name             AS vaccineName
            FROM    vaccination_history vh
            JOIN    vaccine_event  ve ON ve.event_id     = vh.event_id
            JOIN    vaccine        v  ON v.vaccine_id    = ve.vaccine_id
            JOIN    vaccine_category vc ON vc.category_id = v.category_id
            WHERE   vh.student_id = :studentId
            ORDER BY vc.name, vh.dose_number
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM    vaccination_history vh
            JOIN    vaccine_event ve ON ve.event_id = vh.event_id
            JOIN    vaccine v ON v.vaccine_id = ve.vaccine_id
            WHERE   vh.student_id = :studentId
            """,
            nativeQuery = true)
    Page<VaccinationRow> findRowsByStudentId(@Param("studentId") Integer studentId, Pageable pageable);
}
