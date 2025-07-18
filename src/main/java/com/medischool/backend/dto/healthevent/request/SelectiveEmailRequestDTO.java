package com.medischool.backend.dto.healthevent.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending selective email notifications to specific health
 * event consents
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectiveEmailRequestDTO {

  /**
   * List of consent IDs to send emails to
   */
  private List<Long> consentIds;

  /**
   * Optional custom message to include in the email
   */
  private String customMessage;

  /**
   * Email template type (optional, defaults to standard health event
   * notification)
   */
  private String templateType;
} 