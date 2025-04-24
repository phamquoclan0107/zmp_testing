package com.stu.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReportDto {

    // For student report
    private String studentId;//ma sinh vien
    private String studentName;//ten sinh vien
    private String className;//ten lop
    private LocalDate startDate;//ngay bat dau
    private LocalDate endDate;//ngay ket thuc

    // For subject report
    private String subjectId;//ma mon hoc
    private String subjectName;//ten mon hoc

    // For class report
    private String classId;//ma lop
//    private String className;//ten lop

    // Common statistics
    private int totalSessions;//tong so buoi hoc
    private int totalPresent;//tong so diem danh thanh cong
    private int totalAbsent;//tong so diem danh khong thanh cong
    private int totalLate;//tong so diem danh muon
    private double presentPercentage;//phan tram diem danh thanh cong

    // Detailed statistics
    private List<SessionAttendance> sessionAttendances;//danh sach buoi hoc

    // Subject-wise statistics
    private Map<String, SubjectStatistics> subjectStatistics;//thong ke theo mon hoc

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionAttendance {
        private Integer sessionId;//ma buoi
        private LocalDate date;//ngay hoc
        private String subjectName;//ten mon hoc
        private String roomName;//ten phong
        private int startPeriod;//tiet bat dau
        private int endPeriod;//tiet ket thuc
        private String status;//trang thai
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectStatistics {
        private String subjectName;//ten mon hoc
        private int totalSessions;//tong so buoi
        private int present;//tong so diem danh
        private int absent;//tong so khong diem
        private int late;//tong so muon
        private double presentPercentage;//phan tram diem
    }
}