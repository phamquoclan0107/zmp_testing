package com.stu.attendance.dto;

import com.stu.attendance.entity.DiemDanh;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Integer attendanceId;// ma diem danh
    private String studentId;//ma sinh vien
    private String studentName;//ten sinh vien
    private String className;//lop hoc
    private Integer sessionId;//ma buoi hoc
    private String subjectName;//ten mon hoc
    private String roomName;//ten phong hoc
    private DiemDanh.TrangThai status;//trang thai
    private DiemDanh.PhuongThuc method;//phuong thuc diem danh
    private LocalDateTime recordTime;//thoi gian diem danh
    private boolean success;//thanh cong
    private String message;//thong bao
}