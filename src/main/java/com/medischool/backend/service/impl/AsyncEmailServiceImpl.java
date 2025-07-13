package com.medischool.backend.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.medischool.backend.service.AsyncEmailService;
import com.medischool.backend.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEmailServiceImpl implements AsyncEmailService {

    private final EmailService emailService;
    
    @Value("${app.email.thread-pool.size:5}")
    private int threadPoolSize;
    
    @Value("${app.email.timeout.seconds:60}")
    private int timeoutSeconds;

    // Thread pool riêng cho việc gửi email - giảm số lượng để tránh quá tải
    private final ExecutorService emailExecutor = Executors.newFixedThreadPool(3);

    @Override
    @Async("emailTaskExecutor")
    public void sendBulkEmailsAsync(List<Map<String, Object>> parentNotifications) {
        if (parentNotifications == null || parentNotifications.isEmpty()) {
            log.warn("No notifications to send");
            return;
        }

        log.info("Starting async bulk email sending for {} notifications", parentNotifications.size());
        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Giảm batch size để tránh quá tải SMTP server
        int batchSize = 5;
        List<List<Map<String, Object>>> batches = splitIntoBatches(parentNotifications, batchSize);

        for (List<Map<String, Object>> batch : batches) {
            log.info("Processing batch of {} emails", batch.size());
            
            List<CompletableFuture<Boolean>> futures = batch.stream()
                    .map(notification -> CompletableFuture.supplyAsync(() -> {
                        try {
                            String toEmail = (String) notification.get("email");
                            String parentName = (String) notification.get("parentName");
                            String studentName = (String) notification.get("studentName");
                            String subject = (String) notification.get("subject");
                            String content = (String) notification.get("content");
                            String template = (String) notification.get("template");
                            
                            if (subject != null && content != null) {
                                emailService.sendCustomEmail(toEmail, subject, content);
                            } else {
                                String vaccineName = (String) notification.get("vaccineName");
                                String eventDate = (String) notification.get("eventDate");
                                String eventLocation = (String) notification.get("eventLocation");
                                String consentUrl = (String) notification.get("consentUrl");
                                
                                emailService.sendVaccineConsentNotification(
                                    toEmail, parentName, studentName, vaccineName, 
                                    eventDate, eventLocation, consentUrl
                                );
                            }
                            
                            successCount.incrementAndGet();
                            log.debug("Email sent successfully to: {}", toEmail);
                            return true;
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            log.error("Failed to send email to: {}", notification.get("email"), e);
                            return false;
                        }
                    }, emailExecutor))
                    .toList();

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeoutSeconds * 2, TimeUnit.SECONDS);

                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("Timeout or error during batch email sending", e);
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("Async bulk email sending completed in {} ms. Success: {}, Failed: {}", 
                (endTime - startTime), successCount.get(), failureCount.get());
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendSingleEmailAsync(Map<String, Object> notification) {
        try {
            String toEmail = (String) notification.get("email");
            String parentName = (String) notification.get("parentName");
            String studentName = (String) notification.get("studentName");
            String subject = (String) notification.get("subject");
            String content = (String) notification.get("content");
            String template = (String) notification.get("template");
            
            if (subject != null && content != null) {
                emailService.sendCustomEmail(toEmail, subject, content);
            } else {
                String vaccineName = (String) notification.get("vaccineName");
                String eventDate = (String) notification.get("eventDate");
                String eventLocation = (String) notification.get("eventLocation");
                String consentUrl = (String) notification.get("consentUrl");
                
                emailService.sendVaccineConsentNotification(
                    toEmail, parentName, studentName, vaccineName, 
                    eventDate, eventLocation, consentUrl
                );
            }
            
            log.info("Async email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send async email to: {}", notification.get("email"), e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Integer> sendBulkEmailsAsyncWithResult(List<Map<String, Object>> parentNotifications) {
        if (parentNotifications == null || parentNotifications.isEmpty()) {
            log.warn("No notifications to send");
            return CompletableFuture.completedFuture(0);
        }

        log.info("Starting async bulk email sending with result for {} notifications", parentNotifications.size());
        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        int batchSize = 5;
        List<List<Map<String, Object>>> batches = splitIntoBatches(parentNotifications, batchSize);

        for (List<Map<String, Object>> batch : batches) {
            log.info("Processing batch of {} emails", batch.size());
            
            List<CompletableFuture<Boolean>> futures = batch.stream()
                    .map(notification -> CompletableFuture.supplyAsync(() -> {
                        try {
                            String toEmail = (String) notification.get("email");
                            String parentName = (String) notification.get("parentName");
                            String studentName = (String) notification.get("studentName");
                            String subject = (String) notification.get("subject");
                            String content = (String) notification.get("content");
                            String template = (String) notification.get("template");
                            
                            if (subject != null && content != null) {
                                emailService.sendCustomEmail(toEmail, subject, content);
                            } else {
                                String vaccineName = (String) notification.get("vaccineName");
                                String eventDate = (String) notification.get("eventDate");
                                String eventLocation = (String) notification.get("eventLocation");
                                String consentUrl = (String) notification.get("consentUrl");
                                
                                emailService.sendVaccineConsentNotification(
                                    toEmail, parentName, studentName, vaccineName, 
                                    eventDate, eventLocation, consentUrl
                                );
                            }
                            
                            successCount.incrementAndGet();
                            log.debug("Email sent successfully to: {}", toEmail);
                            return true;
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            log.error("Failed to send email to: {}", notification.get("email"), e);
                            return false;
                        }
                    }, emailExecutor))
                    .toList();

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeoutSeconds * 2, TimeUnit.SECONDS);

                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("Timeout or error during batch email sending", e);
            }
        }

        long endTime = System.currentTimeMillis();
        int finalSuccessCount = successCount.get();
        log.info("Async bulk email sending with result completed in {} ms. Success: {}, Failed: {}", 
                (endTime - startTime), finalSuccessCount, failureCount.get());
        
        return CompletableFuture.completedFuture(finalSuccessCount);
    }

    private List<List<Map<String, Object>>> splitIntoBatches(List<Map<String, Object>> list, int batchSize) {
        return list.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    item -> list.indexOf(item) / batchSize,
                    java.util.stream.Collectors.toList()
                ))
                .values()
                .stream()
                .toList();
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendBulkHealthEventEmailsAsync(List<Map<String, Object>> healthEventNotifications) {
        if (healthEventNotifications == null || healthEventNotifications.isEmpty()) {
            log.warn("No health event notifications to send");
            return;
        }

        log.info("Starting async bulk health event email sending for {} notifications", healthEventNotifications.size());
        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Giảm batch size để tránh quá tải SMTP server
        int batchSize = 5;
        List<List<Map<String, Object>>> batches = splitIntoBatches(healthEventNotifications, batchSize);

        for (List<Map<String, Object>> batch : batches) {
            log.info("Processing batch of {} health event emails", batch.size());
            
            List<CompletableFuture<Boolean>> futures = batch.stream()
                    .map(notification -> CompletableFuture.supplyAsync(() -> {
                        try {
                            String toEmail = (String) notification.get("email");
                            String parentName = (String) notification.get("parentName");
                            String studentName = (String) notification.get("studentName");
                            String problem = (String) notification.get("problem");
                            String description = (String) notification.get("description");
                            String solution = (String) notification.get("solution");
                            String extent = (String) notification.get("extent");
                            String eventDate = (String) notification.get("eventDate");
                            String eventLocation = (String) notification.get("eventLocation");
                            
                            emailService.sendHealthEventNotification(
                                toEmail, parentName, studentName, problem, description, 
                                solution, extent, eventDate, eventLocation
                            );
                            
                            successCount.incrementAndGet();
                            log.debug("Health event email sent successfully to: {}", toEmail);
                            return true;
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            log.error("Failed to send health event email to: {}", notification.get("email"), e);
                            return false;
                        }
                    }, emailExecutor))
                    .toList();

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeoutSeconds * 2, TimeUnit.SECONDS);

                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("Timeout or error during batch health event email sending", e);
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("Async bulk health event email sending completed in {} ms. Success: {}, Failed: {}", 
                (endTime - startTime), successCount.get(), failureCount.get());
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Integer> sendBulkHealthEventEmailsAsyncWithResult(List<Map<String, Object>> healthEventNotifications) {
        if (healthEventNotifications == null || healthEventNotifications.isEmpty()) {
            log.warn("No health event notifications to send");
            return CompletableFuture.completedFuture(0);
        }

        log.info("Starting async bulk health event email sending with result for {} notifications", healthEventNotifications.size());
        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        int batchSize = 5;
        List<List<Map<String, Object>>> batches = splitIntoBatches(healthEventNotifications, batchSize);

        for (List<Map<String, Object>> batch : batches) {
            log.info("Processing batch of {} health event emails", batch.size());
            
            List<CompletableFuture<Boolean>> futures = batch.stream()
                    .map(notification -> CompletableFuture.supplyAsync(() -> {
                        try {
                            String toEmail = (String) notification.get("email");
                            String parentName = (String) notification.get("parentName");
                            String studentName = (String) notification.get("studentName");
                            String problem = (String) notification.get("problem");
                            String description = (String) notification.get("description");
                            String solution = (String) notification.get("solution");
                            String extent = (String) notification.get("extent");
                            String eventDate = (String) notification.get("eventDate");
                            String eventLocation = (String) notification.get("eventLocation");
                            
                            emailService.sendHealthEventNotification(
                                toEmail, parentName, studentName, problem, description, 
                                solution, extent, eventDate, eventLocation
                            );
                            
                            successCount.incrementAndGet();
                            log.debug("Health event email sent successfully to: {}", toEmail);
                            return true;
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            log.error("Failed to send health event email to: {}", notification.get("email"), e);
                            return false;
                        }
                    }, emailExecutor))
                    .toList();

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeoutSeconds * 2, TimeUnit.SECONDS);

                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("Timeout or error during batch health event email sending", e);
            }
        }

        long endTime = System.currentTimeMillis();
        int finalSuccessCount = successCount.get();
        log.info("Async bulk health event email sending with result completed in {} ms. Success: {}, Failed: {}", 
                (endTime - startTime), finalSuccessCount, failureCount.get());
        
        return CompletableFuture.completedFuture(finalSuccessCount);
    }

    public void shutdown() {
        emailExecutor.shutdown();
        try {
            if (!emailExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                emailExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            emailExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 