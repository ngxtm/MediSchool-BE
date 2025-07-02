package com.medischool.backend.service.impl;

import org.springframework.stereotype.Service;

import com.medischool.backend.dto.healthevent.response.TotalHealthEventStatusResDTO;
import com.medischool.backend.repository.healthevent.HealthEventRepository;
import com.medischool.backend.service.healthevent.HealthEventService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthEventServiceImpl implements HealthEventService {

    private final HealthEventRepository healthEventRepository;

    public TotalHealthEventStatusResDTO getTotalHealthEventStatusResDTO() {
        int totalEvent = healthEventRepository.findAll().size();
        int normalEvent = healthEventRepository.findByExtent("NORMAL").size();
        int dangerousEvent = healthEventRepository.findByExtent("DANGEROUS").size();
        
        return TotalHealthEventStatusResDTO.builder()
                .totalHealthEvent(totalEvent)
                .totalNormalCase(normalEvent)
                .totalDangerousCase(dangerousEvent)
                .build();
    }
}
