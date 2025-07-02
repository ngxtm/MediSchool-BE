# Hướng dẫn sử dụng Email đã tối ưu

## Tổng quan
Hệ thống email đã được tối ưu với:
- **Gửi email bất đồng bộ** - không block main thread
- **Connection pooling** - tái sử dụng kết nối SMTP
- **Retry mechanism** - tự động thử lại khi lỗi
- **Batch processing** - xử lý theo batch để tránh quá tải

## Cách sử dụng

### 1. Gửi email đơn lẻ bất đồng bộ
```java
@Autowired
private AsyncEmailService asyncEmailService;

// Gửi email đơn lẻ
Map<String, Object> notification = Map.of(
    "email", "parent@example.com",
    "parentName", "Tên phụ huynh",
    "studentName", "Tên học sinh",
    "vaccineName", "Tên vaccine",
    "eventDate", "01/01/2025",
    "eventLocation", "Địa điểm",
    "consentUrl", "http://localhost:5173/consent"
);

asyncEmailService.sendSingleEmailAsync(notification);
```

### 2. Gửi nhiều email cùng lúc (fire-and-forget)
```java
List<Map<String, Object>> notifications = // danh sách thông báo
asyncEmailService.sendBulkEmailsAsync(notifications);
```

### 3. Gửi nhiều email và đợi kết quả
```java
CompletableFuture<Integer> future = asyncEmailService.sendBulkEmailsAsyncWithResult(notifications);
int successCount = future.get(); // đợi kết quả
```

## Cấu hình hiện tại

### Thread Pool
- **Core pool size**: 3 threads
- **Max pool size**: 3 threads  
- **Queue capacity**: 100 tasks
- **Timeout**: 60 giây

### SMTP Configuration
- **Host**: smtp.gmail.com
- **Port**: 465 (SSL)
- **Connection pooling**: 5 connections
- **Timeout**: 30 giây

### Batch Processing
- **Batch size**: 5 emails/batch
- **Delay between batches**: 1 giây
- **Retry attempts**: 3 lần

## Performance Expectations

### Với 20 emails:
- **Thời gian gửi**: ~10-15 giây
- **Success rate**: >95%
- **Memory usage**: Thấp (connection pooling)

### Với 100 emails:
- **Thời gian gửi**: ~30-45 giây
- **Success rate**: >90%
- **Batch processing**: 20 batches

## Monitoring

### Logs
- `INFO`: Bắt đầu gửi email
- `DEBUG`: Email gửi thành công
- `WARN`: Retry attempts
- `ERROR`: Lỗi cuối cùng

### Metrics
- Số email thành công/thất bại
- Thời gian gửi
- Số lần retry

## Troubleshooting

### Lỗi SSL
- Kiểm tra App Password
- Thử port 587 với STARTTLS
- Kiểm tra firewall

### Lỗi timeout
- Tăng timeout trong config
- Giảm batch size
- Kiểm tra network

### Lỗi connection
- Kiểm tra SMTP credentials
- Thử email khác
- Kiểm tra Gmail settings 