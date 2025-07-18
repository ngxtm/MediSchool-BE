package com.medischool.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for email notification operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationResponseDTO {

  /**
   * Whether the email operation was successful
   */
  private boolean success;

  /**
   * Response message
   */
  private String message;

  /**
   * Total number of emails that were attempted to be sent
   */
  private int totalEmailsSent;

  /**
   * Actual number of emails successfully sent
   */
  private int actualCount;

  /**
   * Whether the email included a PDF attachment
   */
  private boolean hasPdfAttachment;
} 