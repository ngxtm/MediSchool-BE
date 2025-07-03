package com.medischool.backend.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface AsyncEmailService {
    /**
     * Gửi email bất đồng bộ với hiệu suất cao
     * @param parentNotifications Danh sách thông báo cần gửi
     */
    void sendBulkEmailsAsync(List<Map<String, Object>> parentNotifications);
    
    /**
     * Gửi email bất đồng bộ và trả về CompletableFuture để theo dõi kết quả
     * @param parentNotifications Danh sách thông báo cần gửi
     * @return CompletableFuture với số lượng email thành công
     */
    CompletableFuture<Integer> sendBulkEmailsAsyncWithResult(List<Map<String, Object>> parentNotifications);
    
    /**
     * Gửi email đơn lẻ bất đồng bộ
     * @param notification Thông tin email cần gửi
     */
    void sendSingleEmailAsync(Map<String, Object> notification);
    
    /**
     * Gửi email thông báo sự kiện y tế bất đồng bộ
     * @param healthEventNotifications Danh sách thông báo sự kiện y tế cần gửi
     */
    void sendBulkHealthEventEmailsAsync(List<Map<String, Object>> healthEventNotifications);
    
    /**
     * Gửi email thông báo sự kiện y tế bất đồng bộ và trả về kết quả
     * @param healthEventNotifications Danh sách thông báo sự kiện y tế cần gửi
     * @return CompletableFuture với số lượng email thành công
     */
    CompletableFuture<Integer> sendBulkHealthEventEmailsAsyncWithResult(List<Map<String, Object>> healthEventNotifications);
} 