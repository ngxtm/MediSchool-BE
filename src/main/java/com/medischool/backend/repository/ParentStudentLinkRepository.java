package com.medischool.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medischool.backend.model.parentstudent.ParentStudentLink;
import com.medischool.backend.model.parentstudent.ParentStudentLinkId;

@Repository
public interface
ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, ParentStudentLinkId>{
    List<ParentStudentLink> findByStudentId(Integer studentId);
    List<ParentStudentLink> findByParentId(UUID parentId);
    boolean existsByParentIdAndStudentId(UUID parentId, Integer studentId);
}
