package com.stu.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {

    private Integer sessionId;//ma buoi hoc

    @NotBlank(message = "Mã môn học không được để trống")
    private String subjectId;

    private String subjectName;

    @NotBlank(message = "Mã phòng không được để trống")
    private String roomId;

    private String roomName;

    @NotNull(message = "Ngày học không được để trống")
    private LocalDate date;

    @NotNull(message = "Tiết bắt đầu không được để trống")
    private Integer startPeriod;

    @NotNull(message = "Tiết kết thúc không được để trống")
    private Integer endPeriod;

    // Statistics for attendance
    private Integer totalStudents;//tong so sinh vien
    private Integer presentCount;//so sinh vien diem danh
    private Integer absentCount;//so sinh vien diem danh khong den
    private Integer lateCount;//so sinh vien diem danh muon tre


    // For detailed view
    private List<AttendanceResponse> attendances;//danh sach diem danh

    // For creation and update
    private List<String> classIds;//danh sach lop

    private String maThamGia;//ma tham gia
    private String gvId;//ma giao vien
}
