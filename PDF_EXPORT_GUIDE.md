# Hướng dẫn xuất PDF cho Vaccination History

## Tổng quan
Dự án đã được tích hợp tính năng xuất PDF cho các báo cáo lịch sử tiêm chủng sử dụng thư viện iText 7.

## Các endpoint PDF mới

### 1. Xuất PDF lịch sử tiêm chủng theo sự kiện
**Endpoint:** `GET /api/vaccination-history/event/{eventId}/pdf`

**Mô tả:** Xuất báo cáo PDF chứa thông tin tất cả học sinh đã tiêm chủng trong một sự kiện cụ thể.

**Tham số:**
- `eventId` (Long): ID của sự kiện tiêm chủng

**Response:**
- Content-Type: `application/pdf`
- File được download với tên: `vaccination-history-event-{eventId}.pdf`

**Nội dung PDF bao gồm:**

#### **Bảng tổng quan:**
- History ID (ID lịch sử)
- Student ID (Mã học sinh)
- Student Name (Tên học sinh)
- Vaccine (Tên vaccine)
- Dose (Liều số)
- Date (Ngày tiêm)
- Location (Địa điểm)
- Abnormal (Bất thường)
- Created (Ngày tạo)

#### **Thông tin chi tiết cho từng record:**
- **History ID:** ID của record lịch sử
- **Student ID:** Mã học sinh
- **Student Name:** Tên học sinh
- **Event ID:** ID sự kiện tiêm chủng
- **Vaccine:** Tên vaccine
- **Dose Number:** Số liều
- **Vaccination Date:** Ngày tiêm chủng
- **Location:** Địa điểm tiêm
- **Note:** Ghi chú
- **Abnormal:** Có bất thường không (Yes/No)
- **Follow-up Note:** Ghi chú theo dõi
- **Created By:** Người tạo (UUID)
- **Created At:** Thời gian tạo

### 2. Xuất PDF lịch sử tiêm chủng cá nhân
**Endpoint:** `GET /api/vaccination-history/student/{studentId}/pdf`

**Mô tả:** Xuất báo cáo PDF chứa toàn bộ lịch sử tiêm chủng của một học sinh, được nhóm theo danh mục vaccine.

**Tham số:**
- `studentId` (Integer): ID của học sinh

**Response:**
- Content-Type: `application/pdf`
- File được download với tên: `student-vaccination-history-{studentId}.pdf`

**Nội dung PDF bao gồm:**

#### **Bảng tổng quan theo danh mục:**
- History ID (ID lịch sử)
- Vaccine (Tên vaccine)
- Dose (Liều số)
- Date (Ngày tiêm)
- Location (Địa điểm)
- Abnormal (Bất thường)
- Event ID (ID sự kiện)
- Created (Ngày tạo)

#### **Thông tin chi tiết cho từng record:**
- **History ID:** ID của record lịch sử
- **Student ID:** Mã học sinh
- **Event ID:** ID sự kiện tiêm chủng
- **Vaccine:** Tên vaccine
- **Dose Number:** Số liều
- **Vaccination Date:** Ngày tiêm chủng
- **Location:** Địa điểm tiêm
- **Note:** Ghi chú
- **Abnormal:** Có bất thường không (Yes/No)
- **Follow-up Note:** Ghi chú theo dõi
- **Created By:** Người tạo (UUID)
- **Created At:** Thời gian tạo

## Cách sử dụng

### Sử dụng với cURL
```bash
# Xuất PDF lịch sử tiêm chủng theo sự kiện
curl -X GET "http://localhost:8080/api/vaccination-history/event/1/pdf" \
  -H "Accept: application/pdf" \
  --output vaccination-history-event-1.pdf

# Xuất PDF lịch sử tiêm chủng cá nhân
curl -X GET "http://localhost:8080/api/vaccination-history/student/123/pdf" \
  -H "Accept: application/pdf" \
  --output student-vaccination-history-123.pdf
```

### Sử dụng với JavaScript/Fetch
```javascript
// Xuất PDF lịch sử tiêm chủng theo sự kiện
async function downloadEventPdf(eventId) {
    const response = await fetch(`/api/vaccination-history/event/${eventId}/pdf`);
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `vaccination-history-event-${eventId}.pdf`;
    a.click();
    window.URL.revokeObjectURL(url);
}

// Xuất PDF lịch sử tiêm chủng cá nhân
async function downloadStudentPdf(studentId) {
    const response = await fetch(`/api/vaccination-history/student/${studentId}/pdf`);
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `student-vaccination-history-${studentId}.pdf`;
    a.click();
    window.URL.revokeObjectURL(url);
}
```

## Tính năng đặc biệt

### Hiển thị đầy đủ thông tin
- **Tất cả các field** của VaccinationHistory được hiển thị
- **Bảng tổng quan** cho cái nhìn nhanh
- **Thông tin chi tiết** cho từng record
- **Định dạng thời gian** rõ ràng (dd/MM/yyyy cho ngày, dd/MM/yyyy HH:mm cho datetime)

### Xử lý ký tự đặc biệt
- PDF sử dụng font mặc định để tránh lỗi với ký tự tiếng Việt
- Các ký tự tiếng Việt được chuyển đổi thành ASCII tương đương
- Đảm bảo tính ổn định trên mọi hệ thống

### Định dạng bảng
- Bảng được tạo với độ rộng tự động điều chỉnh
- Header được in đậm
- Dữ liệu được căn chỉnh phù hợp
- Phân chia rõ ràng giữa bảng tổng quan và chi tiết

### Xử lý lỗi
- Nếu có lỗi trong quá trình tạo PDF, hệ thống sẽ trả về lỗi 500
- Log lỗi được ghi lại để debug

## Dependencies đã thêm
```xml
<!-- PDF Generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
    <type>pom</type>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>kernel</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>io</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>layout</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>font-asian</artifactId>
    <version>7.2.5</version>
</dependency>
```

## Lưu ý
- Đảm bảo server có đủ bộ nhớ để xử lý các file PDF lớn
- Với dữ liệu lớn, có thể cần tối ưu hóa thêm để tránh timeout
- PDF sử dụng tiếng Anh để đảm bảo tính tương thích
- Các ký tự tiếng Việt trong dữ liệu sẽ được chuyển đổi thành ASCII
- Tất cả các field của VaccinationHistory đều được hiển thị đầy đủ 