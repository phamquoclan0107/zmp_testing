package com.stu.attendance.controller;

import com.stu.attendance.dto.*;
import com.stu.attendance.service.NguoiThamGiaService;
import com.stu.attendance.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    private final TeacherService teacherService;
    private final NguoiThamGiaService nguoiThamGiaService;

//    // Session management
//    @GetMapping("/sessions")
//    public ResponseEntity<List<SessionDto>> getTeacherSessions(
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//        return ResponseEntity.ok(teacherService.getTeacherSessions(startDate, endDate));
//    }

//    @GetMapping("/sessions/today")
//    public ResponseEntity<List<SessionDto>> getTodaySessions() {
//        return ResponseEntity.ok(teacherService.getTodaySessions());
//    }

    @GetMapping("/sessions/{sessionId}")//lay thong tin buoi hoc
    public ResponseEntity<SessionDto> getSessionDetails(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(teacherService.getSessionDetails(sessionId));
    }

    // Attendance management
    @PostMapping("/attendance/take")//ghi danh sinh vien
    public ResponseEntity<List<AttendanceResponse>> takeAttendance(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(teacherService.takeAttendance(request));
    }

    @PostMapping("/attendance/update")//cap nhat trang thai
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @RequestParam Integer attendanceId,
            @RequestParam String status) {
        return ResponseEntity.ok(teacherService.updateAttendanceStatus(attendanceId, status));
    }

    @GetMapping(value = "/qr-code/{sessionId}", produces = MediaType.IMAGE_PNG_VALUE)//tao qr code
    public ResponseEntity<byte[]> generateQrCode(
            @PathVariable Integer sessionId,
            @RequestParam(defaultValue = "5") Integer validityMinutes,
            @RequestParam(defaultValue = "250") Integer size) {
        byte[] qrCodeImage = teacherService.generateQrCode(sessionId, validityMinutes, size);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeImage);
    }

    // Class management
//    @GetMapping("/classes")
//    public ResponseEntity<List<Map<String, Object>>> getClasses() {
//        return ResponseEntity.ok(teacherService.getClasses());
//    }

    @GetMapping("/classes/{classId}/students")//lay danh sach sinh vien
    public ResponseEntity<List<Map<String, Object>>> getStudentsByClass(@PathVariable String classId) {
        return ResponseEntity.ok(teacherService.getStudentsByClass(classId));
    }

}