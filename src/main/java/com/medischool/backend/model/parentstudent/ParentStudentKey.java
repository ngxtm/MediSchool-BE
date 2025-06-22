package com.medischool.backend.model.parentstudent;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ParentStudentKey implements Serializable {

    private UUID parentId;
    private Integer studentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParentStudentKey)) return false;
        ParentStudentKey that = (ParentStudentKey) o;
        return Objects.equals(parentId, that.parentId) &&
                Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, studentId);
    }
}
