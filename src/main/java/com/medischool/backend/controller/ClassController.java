package com.medischool.backend.controller;

import com.medischool.backend.model.parentstudent.Classes;
import com.medischool.backend.service.impl.ClassesServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Tag(name = "Class", description = "Class endpoints")
public class ClassController {

    private final ClassesServiceImpl classesService;

    @GetMapping
    public ResponseEntity<List<Classes>> getAllClasses() {
        return ResponseEntity.ok(classesService.findAll());
    }
}
