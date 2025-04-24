package com.stu.attendance.controller;

import com.stu.attendance.dto.AttendanceRequest;
import com.stu.attendance.dto.AttendanceResponse;
import com.stu.attendance.dto.QrCodeRequest;
import com.stu.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")// diem danh
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/record")//diem danh sinh vien
    public ResponseEntity<AttendanceResponse> recordAttendance(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.recordAttendance(request));
    }

    @PostMapping("/record-batch")// diem danh nhieu sinh vien
    public ResponseEntity<List<AttendanceResponse>> recordBatchAttendance(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.recordBatchAttendance(request));
    }

    @GetMapping("/session/{sessionId}")//lay danh sach diem danh theo buoi hoc
    public ResponseEntity<List<AttendanceResponse>> getSessionAttendance(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(attendanceService.getAttendancesBySession(sessionId));
    }

    @GetMapping("/student/{studentId}")//lay danh sach diem danh theo sinh vien
    public ResponseEntity<List<AttendanceResponse>> getStudentAttendance(@PathVariable String studentId) {
        return ResponseEntity.ok(attendanceService.getAttendancesByStudent(studentId));
    }

    @PostMapping(value = "/generate-qr", produces = MediaType.IMAGE_PNG_VALUE)//tao ma qr
    public ResponseEntity<byte[]> generateQrCode(@Valid @RequestBody QrCodeRequest request) {
        byte[] qrCodeImage = attendanceService.generateQrCode(request);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeImage);
    }

    @PostMapping("/verify-qr")//kiem tra ma qr
    public ResponseEntity<AttendanceResponse> verifyQrCode(@RequestParam String token, @RequestParam String studentId) {
        return ResponseEntity.ok(attendanceService.verifyQrCode(token, studentId));
    }
}