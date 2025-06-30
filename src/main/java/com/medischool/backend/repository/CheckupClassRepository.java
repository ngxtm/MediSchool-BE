package com.medischool.backend.repository;

import com.medischool.backend.model.CheckUpClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckupClassRepository extends JpaRepository<CheckUpClass,Long>, JpaSpecificationExecutor<CheckUpClass> {
}
