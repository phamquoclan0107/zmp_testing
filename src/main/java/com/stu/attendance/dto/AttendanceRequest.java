package com.stu.attendance.dto;

import com.stu.attendance.entity.DiemDanh;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {

    // For QR code attendance
    private String qrToken;

    // For manual attendance
    @NotNull(message = "Mã buổi học không được để trống")
    private Integer sessionId;//ma buoi hoc

    // For single student attendance
    private String studentId;//ma sinh vien

    // For manual batch attendance
    private List<StudentAttendance> studentAttendances;//danh sach sinh vien điểm danh

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAttendance {
        @NotBlank(message = "Mã sinh viên không được để trống")
        private String studentId;

        @NotNull(message = "Trạng thái điểm danh không được để trống")
        private DiemDanh.TrangThai status;
    }
}