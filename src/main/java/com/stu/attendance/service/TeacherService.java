package com.stu.attendance.service;

import com.stu.attendance.dto.AttendanceReportDto;
import com.stu.attendance.dto.AttendanceRequest;
import com.stu.attendance.dto.AttendanceResponse;
import com.stu.attendance.dto.SessionDto;
import com.stu.attendance.entity.*;
import com.stu.attendance.repository.AttendanceRepository;
import com.stu.attendance.repository.TeacherRepository;
import com.stu.attendance.security.JwtTokenProvider;
//import com.stu.attendance.util.JwtUtils;
import com.stu.attendance.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final JwtTokenProvider jwtUtils;
    private final AttendanceRepository attendanceRepository;

    /**
     * Get sessions taught by the current teacher within date range
     */
//    public List<SessionDto> getTeacherSessions(LocalDate startDate, LocalDate endDate) {
//        String teacherId = getCurrentTeacherId();
//        List<BuoiHoc> sessions = teacherRepository.findTeacherSessions(teacherId, startDate, endDate);
//        return sessions.stream()
//                .map(this::mapToSessionDto)
//                .collect(Collectors.toList());
//    }

    /**
     * Get today's sessions for the current teacher
     */
//    public List<SessionDto> getTodaySessions() {
//        String teacherId = getCurrentTeacherId();
//        List<BuoiHoc> sessions = teacherRepository.findTodaySessions(teacherId);
//        return sessions.stream()
//                .map(this::mapToSessionDto)
//                .collect(Collectors.toList());
//    }

    /**
     * Get detailed information about a specific session
     */
    public SessionDto getSessionDetails(Integer sessionId) {
        BuoiHoc session = teacherRepository.findByMaBuoiHoc(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found with ID: " + sessionId);
        }

        SessionDto sessionDto = mapToSessionDto(session);

        // Populate attendance details
        if (session.getDiemDanhs() != null && !session.getDiemDanhs().isEmpty()) {
            List<AttendanceResponse> attendances = session.getDiemDanhs().stream()
                    .map(this::mapToAttendanceResponse)
                    .collect(Collectors.toList());
            sessionDto.setAttendances(attendances);

            // Calculate statistics
            sessionDto.setTotalStudents(attendances.size());
            sessionDto.setPresentCount((int) attendances.stream()
                    .filter(a -> a.getStatus() == DiemDanh.TrangThai.present).count());
            sessionDto.setAbsentCount((int) attendances.stream()
                    .filter(a -> a.getStatus() == DiemDanh.TrangThai.absent).count());
            sessionDto.setLateCount((int) attendances.stream()
                    .filter(a -> a.getStatus() == DiemDanh.TrangThai.late).count());
        }

        return sessionDto;
    }

    /**
     * Take attendance for students in a session
     */
    @Transactional
    public List<AttendanceResponse> takeAttendance(AttendanceRequest request) {
        BuoiHoc session = teacherRepository.findByMaBuoiHoc(request.getSessionId());
        if (session == null) {
            throw new RuntimeException("Session not found with ID: " + request.getSessionId());
        }

        List<AttendanceResponse> responses = new ArrayList<>();

        // Handle QR code attendance
        if (request.getQrToken() != null && !request.getQrToken().isEmpty()) {
            // Verify QR token and extract studentId
            Map<String, Object> claims = jwtUtils.validateQrToken(request.getQrToken());
            if (claims != null) {
                String studentId = (String) claims.get("studentId");
                Integer sessionIdFromToken = (Integer) claims.get("sessionId");

                if (!session.getMaBuoiHoc().equals(sessionIdFromToken)) {
                    AttendanceResponse response = new AttendanceResponse();
                    response.setSuccess(false);
                    response.setMessage("QR code is not valid for this session");
                    responses.add(response);
                    return responses;
                }

                // Process single student QR attendance
                responses.add(processStudentAttendance(studentId, session, DiemDanh.TrangThai.present, DiemDanh.PhuongThuc.qr_code));
            } else {
                AttendanceResponse response = new AttendanceResponse();
                response.setSuccess(false);
                response.setMessage("Invalid QR code");
                responses.add(response);
            }
            return responses;
        }

        // Handle single student attendance
        if (request.getStudentId() != null && !request.getStudentId().isEmpty()) {
            responses.add(processStudentAttendance(
                    request.getStudentId(),
                    session,
                    DiemDanh.TrangThai.present,
                    DiemDanh.PhuongThuc.manual));
            return responses;
        }

        // Handle batch attendance
        if (request.getStudentAttendances() != null && !request.getStudentAttendances().isEmpty()) {
            for (AttendanceRequest.StudentAttendance studentAttendance : request.getStudentAttendances()) {
                responses.add(processStudentAttendance(
                        studentAttendance.getStudentId(),
                        session,
                        studentAttendance.getStatus(),
                        DiemDanh.PhuongThuc.manual));
            }
            return responses;
        }

        // If no valid attendance data was provided
        AttendanceResponse response = new AttendanceResponse();
        response.setSuccess(false);
        response.setMessage("No valid attendance data provided");
        responses.add(response);
        return responses;
    }

    /**
     * Update attendance status for a specific attendance record
     */
    @Transactional
    public AttendanceResponse updateAttendanceStatus(Integer attendanceId, String status) {
        DiemDanh attendance = teacherRepository.findAttendanceById(attendanceId);
        if (attendance == null) {
            throw new RuntimeException("Attendance record not found with ID: " + attendanceId);
        }

        try {
            DiemDanh.TrangThai trangThai = DiemDanh.TrangThai.valueOf(status);
            attendance.setTrangThai(trangThai);
            attendance.setThoiGianDiemDanh(LocalDateTime.now());
            attendance = attendanceRepository.save(attendance);

            AttendanceResponse response = mapToAttendanceResponse(attendance);
            response.setSuccess(true);
            response.setMessage("Attendance status updated successfully");
            return response;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid attendance status: " + status);
        }
    }

    /**
     * Generate QR code for attendance in a session
     */
    public byte[] generateQrCode(Integer sessionId, Integer validityMinutes, Integer size) {
        BuoiHoc session = teacherRepository.findByMaBuoiHoc(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found with ID: " + sessionId);
        }

        // Create a token with session information
        String qrToken = jwtUtils.generateQrToken(sessionId, validityMinutes);

        // Generate QR code image
        return qrCodeGenerator.generateQrCode(qrToken, size);
    }

    /**
     * Get classes taught by the current teacher
     */
//    public List<Map<String, Object>> getClasses() {
//        String teacherId = getCurrentTeacherId();
//        List<LopSinhVien> classes = teacherRepository.findTeacherClasses(teacherId);
//
//        return classes.stream().map(lop -> {
//            Map<String, Object> classInfo = new HashMap<>();
//            classInfo.put("classId", lop.getMaLop());
//            classInfo.put("className", lop.getTenLop());
//            return classInfo;
//        }).collect(Collectors.toList());
//    }

    /**
     * Get students in a specific class
     */
    public List<Map<String, Object>> getStudentsByClass(String classId) {
        List<NguoiDung> students = teacherRepository.findStudentsByClass(classId);

        return students.stream().map(student -> {
            Map<String, Object> studentInfo = new HashMap<>();
            studentInfo.put("studentId", student.getMaNguoiDung());
            studentInfo.put("fullName", student.getTenNguoiDung());
            studentInfo.put("email", student.getEmail());
            studentInfo.put("phone", student.getSdt());
            return studentInfo;
        }).collect(Collectors.toList());
    }

    /**
     * Process attendance for a single student
     */
    private AttendanceResponse processStudentAttendance(
            String studentId,
            BuoiHoc session,
            DiemDanh.TrangThai status,
            DiemDanh.PhuongThuc method) {

        // Find existing attendance or create a new one
        DiemDanh attendance = session.getDiemDanhs().stream()
                .filter(d -> d.getNguoiDung().getMaNguoiDung().equals(studentId))
                .findFirst()
                .orElse(null);

        AttendanceResponse response = new AttendanceResponse();
        response.setSessionId(session.getMaBuoiHoc());
        response.setSubjectName(session.getMonHoc().getTenMonHoc());
        response.setRoomName(session.getPhong().getTenPhong());
        response.setStudentId(studentId);

        try {
            if (attendance == null) {
                // Student not found in this session's attendance list
                response.setSuccess(false);
                response.setMessage("Student not registered for this session");
                return response;
            }

            // Update attendance record
            attendance.setTrangThai(status);
            attendance.setPhuongThuc(method);
            attendance.setThoiGianDiemDanh(LocalDateTime.now());
            attendance = attendanceRepository.save(attendance);

            // Map to response
            response = mapToAttendanceResponse(attendance);
            response.setSuccess(true);
            response.setMessage("Attendance recorded successfully");

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error recording attendance: " + e.getMessage());
        }

        return response;
    }

    /**
     * Helper method to map BuoiHoc entity to SessionDto
     */
    private SessionDto mapToSessionDto(BuoiHoc buoiHoc) {
        SessionDto dto = new SessionDto();
        dto.setSessionId(buoiHoc.getMaBuoiHoc());
        dto.setSubjectId(buoiHoc.getMonHoc().getMaMonHoc());
        dto.setSubjectName(buoiHoc.getMonHoc().getTenMonHoc());
        dto.setRoomId(buoiHoc.getPhong().getMaPhong());
        dto.setRoomName(buoiHoc.getPhong().getTenPhong());
        dto.setDate(buoiHoc.getNgayHoc());
        dto.setStartPeriod(buoiHoc.getTietBatDau());
        dto.setEndPeriod(buoiHoc.getTietKetThuc());

        // Basic attendance statistics
        if (buoiHoc.getDiemDanhs() != null) {
            dto.setTotalStudents(buoiHoc.getDiemDanhs().size());
            dto.setPresentCount((int) buoiHoc.getDiemDanhs().stream()
                    .filter(d -> d.getTrangThai() == DiemDanh.TrangThai.present).count());
            dto.setAbsentCount((int) buoiHoc.getDiemDanhs().stream()
                    .filter(d -> d.getTrangThai() == DiemDanh.TrangThai.absent).count());
            dto.setLateCount((int) buoiHoc.getDiemDanhs().stream()
                    .filter(d -> d.getTrangThai() == DiemDanh.TrangThai.late).count());
        }

        return dto;
    }

    /**
     * Helper method to map DiemDanh entity to AttendanceResponse
     */
    private AttendanceResponse mapToAttendanceResponse(DiemDanh diemDanh) {
        AttendanceResponse response = new AttendanceResponse();
        response.setAttendanceId(diemDanh.getMaDiemDanh());
        response.setStudentId(diemDanh.getNguoiDung().getMaNguoiDung());
        response.setStudentName(diemDanh.getNguoiDung().getTenNguoiDung());

        if (diemDanh.getNguoiDung().getLopSinhVien() != null) {
            response.setClassName(diemDanh.getNguoiDung().getLopSinhVien().getTenLop());
        }

        response.setSessionId(diemDanh.getBuoiHoc().getMaBuoiHoc());
        response.setSubjectName(diemDanh.getBuoiHoc().getMonHoc().getTenMonHoc());
        response.setRoomName(diemDanh.getBuoiHoc().getPhong().getTenPhong());
        response.setStatus(diemDanh.getTrangThai());
        response.setMethod(diemDanh.getPhuongThuc());
        response.setRecordTime(diemDanh.getThoiGianDiemDanh());
        response.setSuccess(true);

        return response;
    }

    /**
     * Get current logged-in teacher ID from security context
     */
    private String getCurrentTeacherId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // In a real implementation, this would query a repository to get the teacher ID from username
        // For simplicity, we're assuming the username is the teacher ID
        return username;
    }
}