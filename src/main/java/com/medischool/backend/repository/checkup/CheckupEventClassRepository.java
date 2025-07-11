package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupEventClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface CheckupEventClassRepository extends JpaRepository<CheckupEventClass, CheckupEventClass.PK> {

    Collection<Object> findByEventId(Long eventId);
}