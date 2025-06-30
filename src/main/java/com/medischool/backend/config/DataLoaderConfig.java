package com.medischool.backend.config;

import com.medischool.backend.repository.CheckupConsentRepository;
import com.medischool.backend.repository.ParentProfileRepository;
import com.medischool.backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


// đây là file insert data sẵn vào database
@Component
public class DataLoaderConfig implements CommandLineRunner {


    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ParentProfileRepository parentProfileRepository;
    @Autowired
    private CheckupConsentRepository checkupConsentRepository;

    @Override
    public void run(String... args) throws Exception {
//        insertStudent();
//        insertParent();
//        insertStudent_Parent();
//        insertConsent();
    }
    public void insertConsent(){
        if(checkupConsentRepository.findAll().size()==0){
            jdbcTemplate.update("INSERT INTO checkup_consent_item (is_active, text) VALUES\n" +
                    "(1, 'Khám tổng quát'),\n" +
                    "(1, 'Khám nội khoa'),\n" +
                    "(1, 'Khám tai mũi họng'),\n" +
                    "(1, 'Khám mắt'),\n" +
                    "(1, 'Khám răng hàm mặt'),\n" +
                    "(1, 'Khám da liễu'),\n" +
                    "(1, 'Khám cơ xương khớp'),\n" +
                    "(1, 'Khám tim mạch'),\n" +
                    "(1, 'Khám tiết niệu'),\n" +
                    "(1, 'Khám hô hấp');\n");
        }
    }
    public void insertStudent(){
        if(this.studentRepository.count()==0){
            jdbcTemplate.update("INSERT INTO student_profile (\n" +
                    "    student_id, address, age, class_code, created_at, date_of_birth,\n" +
                    "    emergency_contact, emergency_phone, enrollment_date, gender,\n" +
                    "    grade, name, student_code\n" +
                    ") VALUES\n" +
                    "(1, '123 Lê Lợi, Q1, HCM', 15, '10A1', NOW(), '2010-05-12', 'Nguyễn Văn A', '0912345678', '2024-08-15', 1, 10, 'Trần Minh Khoa', 'STU001'),\n" +
                    "(2, '45 Nguyễn Trãi, Q5, HCM', 14, '9A2', NOW(), '2011-07-23', 'Trần Thị B', '0909876543', '2024-08-15', 0, 9, 'Lê Ngọc Hân', 'STU002'),\n" +
                    "(3, '78 Hai Bà Trưng, Q3, HCM', 16, '11B3', NOW(), '2009-03-18', 'Phạm Văn C', '0932123456', '2024-08-15', 1, 11, 'Nguyễn Quốc Dũng', 'STU003'),\n" +
                    "(4, '12 Trường Chinh, Tân Bình, HCM', 15, '10A2', NOW(), '2010-10-01', 'Ngô Thị D', '0967890123', '2024-08-15', 0, 10, 'Võ Minh Tuấn', 'STU004'),\n" +
                    "(5, '100 CMT8, Q10, HCM', 17, '12C1', NOW(), '2008-01-09', 'Đặng Văn E', '0945678910', '2024-08-15', 1, 12, 'Phan Thị Lan', 'STU005'),\n" +
                    "(6, '22 Lạc Long Quân, Q11, HCM', 14, '9B1', NOW(), '2011-11-20', 'Trần Thị F', '0976543210', '2024-08-15', 0, 9, 'Đỗ Thành Nam', 'STU006'),\n" +
                    "(7, '55 Nguyễn Oanh, Gò Vấp, HCM', 16, '11A1', NOW(), '2009-06-30', 'Nguyễn Văn G', '0909090909', '2024-08-15', 1, 11, 'Huỳnh Phúc An', 'STU007'),\n" +
                    "(8, '3 Phạm Văn Đồng, Thủ Đức, HCM', 15, '10C3', NOW(), '2010-12-25', 'Lê Thị H', '0988888888', '2024-08-15', 0, 10, 'Trịnh Hoài My', 'STU008'),\n" +
                    "(9, '18 Điện Biên Phủ, Bình Thạnh, HCM', 17, '12A4', NOW(), '2008-04-14', 'Phạm Văn I', '0933445566', '2024-08-15', 1, 12, 'Cao Thanh Bình', 'STU009'),\n" +
                    "(10, '36 Hoàng Văn Thụ, Phú Nhuận, HCM', 16, '11C2', NOW(), '2009-08-08', 'Đỗ Thị J', '0955667788', '2024-08-15', 0, 11, 'Lý Kim Chi', 'STU010');\n");
        }
    }
    public void insertParent(){
        if(this.parentProfileRepository.count()==0){
            jdbcTemplate.update("INSERT INTO parent_profile (parent_id, occupation)\n" +
                    "VALUES\n" +
                    "(1, 'Giáo viên'),\n" +
                    "(2, 'Kỹ sư'),\n" +
                    "(3, 'Bác sĩ'),\n" +
                    "(4, 'Nhân viên văn phòng'),\n" +
                    "(5, 'Kinh doanh'),\n" +
                    "(6, 'Công nhân'),\n" +
                    "(7, 'Luật sư'),\n" +
                    "(8, 'Nông dân'),\n" +
                    "(9, 'Lập trình viên'),\n" +
                    "(10, 'Tài xế');\n");
        }
    }
        // hàm này chạy 1 lần xong comment lại


    public void insertStudent_Parent() {
        Integer studentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM student_profile", Integer.class);
        Integer parentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM parent_profile", Integer.class);

        if (studentCount != null && studentCount > 0 && parentCount != null && parentCount > 0) {
            jdbcTemplate.update("INSERT INTO parent_student (student_id, parent_id) VALUES " +
                    "(1, 1), (2, 2), (3, 3), (4, 4), (5, 5), " +
                    "(6, 6), (7, 7), (8, 8), (9, 9), (10, 10);");
        } else {
            System.out.println("Insert skipped: 'student' or 'parent' table is empty.");
        }
    }

}
