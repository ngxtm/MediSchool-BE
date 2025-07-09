package com.medischool.backend.repository.checkup;

import com.medischool.backend.model.checkup.CheckupEventClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckupEventClassRepository extends JpaRepository<CheckupEventClass, CheckupEventClass.PK> {

}