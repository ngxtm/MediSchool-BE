package com.medischool.backend.repository;

import com.medischool.backend.model.ParentStudentLink;
import com.medischool.backend.model.ParentStudentLinkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, ParentStudentLinkId>{
    List<ParentStudentLink>  findByStudentId(Integer id);
}
