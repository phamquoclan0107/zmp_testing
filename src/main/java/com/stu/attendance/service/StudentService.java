package com.stu.attendance.service;

import com.stu.attendance.dto.AttendanceResponse;
import com.stu.attendance.dto.SessionDto;
import com.stu.attendance.entity.*;
import com.stu.attendance.repository.StudentRepository;
import com.stu.attendance.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Get the current authenticated student ID
     */
    private String getCurrentStudentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }


//    public List<SessionDto> getTodaySessions() {
//        String studentId = getCurrentStudentId();
//        List<BuoiHoc> todaySessions = studentRepository.findTodaySessions(studentId);
//        return mapToSessionDtos(todaySessions);
//    }


//    public List<SessionDto> getUpcomingSessions(Integer days) {
//        String studentId = getCurrentStudentId();
//        LocalDate endDate = LocalDate.now().plusDays(days);
//        List<BuoiHoc> upcomingSessions = studentRepository.findUpcomingSessions(studentId, endDate);
//        return mapToSessionDtos(upcomingSessions);
//    }


    public List<AttendanceResponse> getMyAttendance(LocalDate startDate, LocalDate endDate, String subjectId) {
        String studentId = getCurrentStudentId();
        List<DiemDanh> attendances = studentRepository.findStudentAttendance(studentId, startDate, endDate, subjectId);
        return mapToAttendanceResponses(attendances);
    }


    public List<AttendanceResponse> getTodayAttendance() {
        String studentId = getCurrentStudentId();
        List<DiemDanh> todayAttendances = studentRepository.findTodayAttendance(studentId);
        return mapToAttendanceResponses(todayAttendances);
    }


    @Transactional
    public AttendanceResponse attendViaQrCode(String qrToken) {
        String studentId = getCurrentStudentId();

        // Validate QR token
        Map<String, Object> claims = jwtTokenProvider.validateQrToken(qrToken);
        if (claims == null) {
            return createErrorResponse("Mã QR không hợp lệ hoặc đã hết hạn");
        }

        // Extract session ID from token
        Integer sessionId = (Integer) claims.get("sessionId");
        if (sessionId == null) {
            return createErrorResponse("Thông tin buổi học không hợp lệ");
        }

        // Find the student
        Optional<NguoiDung> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return createErrorResponse("Không tìm thấy thông tin sinh viên");
        }
        NguoiDung student = studentOpt.get();

        // Find the session
        Optional<BuoiHoc> sessionOpt = findSessionById(sessionId);
        if (sessionOpt.isEmpty()) {
            return createErrorResponse("Không tìm thấy thông tin buổi học");
        }
        BuoiHoc session = sessionOpt.get();

        // Check if student already attended this session
        Optional<DiemDanh> existingAttendance = findExistingAttendance(student, session);
        if (existingAttendance.isPresent()) {
            return createErrorResponse("Bạn đã điểm danh cho buổi học này");
        }

        // Create new attendance record
        DiemDanh attendance = new DiemDanh();
        attendance.setNguoiDung(student);
        attendance.setBuoiHoc(session);
        attendance.setPhuongThuc(DiemDanh.PhuongThuc.qr_code);
        attendance.setTrangThai(DiemDanh.TrangThai.present);
        attendance.setThoiGianDiemDanh(LocalDateTime.now());

        // Save attendance
        DiemDanh savedAttendance = saveAttendance(attendance);

        // Create response
        AttendanceResponse response = new AttendanceResponse();
        response.setAttendanceId(savedAttendance.getMaDiemDanh());
        response.setStudentId(studentId);
        response.setStudentName(student.getTenNguoiDung());
        response.setClassName(student.getLopSinhVien() != null ? student.getLopSinhVien().getTenLop() : null);
        response.setSessionId(session.getMaBuoiHoc());
        response.setSubjectName(session.getMonHoc().getTenMonHoc());
        response.setRoomName(session.getPhong().getTenPhong());
        response.setStatus(savedAttendance.getTrangThai());
        response.setMethod(savedAttendance.getPhuongThuc());
        response.setRecordTime(savedAttendance.getThoiGianDiemDanh());
        response.setSuccess(true);
        response.setMessage("Điểm danh thành công");

        return response;
    }


    public List<Map<String, Object>> getWeeklySchedule(LocalDate startDate) {
        String studentId = getCurrentStudentId();

        // If startDate is null, use the current week's Monday
        if (startDate == null) {
            startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        LocalDate endDate = startDate.plusDays(6); // One week

        // Get all sessions for the week
        List<BuoiHoc> weekSessions = new ArrayList<>();
        for (MonHoc subject : studentRepository.findStudentSubjects(studentId)) {
            weekSessions.addAll(studentRepository.findSubjectSessions(studentId, subject.getMaMonHoc(), startDate, endDate));
        }

        // Group sessions by day
        Map<DayOfWeek, List<BuoiHoc>> sessionsByDay = weekSessions.stream()
                .collect(Collectors.groupingBy(session -> session.getNgayHoc().getDayOfWeek()));

        // Create schedule
        List<Map<String, Object>> weeklySchedule = new ArrayList<>();

        // For each day of the week (Monday to Sunday)
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            Map<String, Object> daySchedule = new HashMap<>();
            daySchedule.put("date", currentDate);
            daySchedule.put("dayOfWeek", dayOfWeek);

            List<BuoiHoc> daySessions = sessionsByDay.getOrDefault(dayOfWeek, Collections.emptyList());

            // Convert sessions to DTOs
            List<SessionDto> sessionDtos = mapToSessionDtos(daySessions);
            daySchedule.put("sessions", sessionDtos);

            weeklySchedule.add(daySchedule);
        }

        return weeklySchedule;
    }


    public List<Map<String, Object>> getMySubjects() {
        String studentId = getCurrentStudentId();
        List<MonHoc> subjects = studentRepository.findStudentSubjects(studentId);

        return subjects.stream().map(subject -> {
            Map<String, Object> subjectMap = new HashMap<>();
            subjectMap.put("subjectId", subject.getMaMonHoc());
            subjectMap.put("subjectName", subject.getTenMonHoc());

            // You could add additional info here such as attendance statistics for each subject
            return subjectMap;
        }).collect(Collectors.toList());
    }


    public List<SessionDto> getSubjectSessions(String subjectId, LocalDate startDate, LocalDate endDate) {
        String studentId = getCurrentStudentId();
        List<BuoiHoc> sessions = studentRepository.findSubjectSessions(studentId, subjectId, startDate, endDate);
        return mapToSessionDtos(sessions);
    }

    // Helper methods

    private List<SessionDto> mapToSessionDtos(List<BuoiHoc> sessions) {
        return sessions.stream().map(session -> {
            SessionDto dto = new SessionDto();
            dto.setSessionId(session.getMaBuoiHoc());
            dto.setSubjectId(session.getMonHoc().getMaMonHoc());
            dto.setSubjectName(session.getMonHoc().getTenMonHoc());
            dto.setRoomId(session.getPhong().getMaPhong());
            dto.setRoomName(session.getPhong().getTenPhong());
            dto.setDate(session.getNgayHoc());
            dto.setStartPeriod(session.getTietBatDau());
            dto.setEndPeriod(session.getTietKetThuc());

            // Calculate attendance statistics if needed
            if (session.getDiemDanhs() != null) {
                dto.setTotalStudents(session.getDiemDanhs().size());
                dto.setPresentCount((int) session.getDiemDanhs().stream()
                        .filter(a -> a.getTrangThai() == DiemDanh.TrangThai.present)
                        .count());
                dto.setAbsentCount((int) session.getDiemDanhs().stream()
                        .filter(a -> a.getTrangThai() == DiemDanh.TrangThai.absent)
                        .count());
                dto.setLateCount((int) session.getDiemDanhs().stream()
                        .filter(a -> a.getTrangThai() == DiemDanh.TrangThai.late)
                        .count());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    private List<AttendanceResponse> mapToAttendanceResponses(List<DiemDanh> attendances) {
        return attendances.stream().map(attendance -> {
            AttendanceResponse response = new AttendanceResponse();
            response.setAttendanceId(attendance.getMaDiemDanh());
            response.setStudentId(attendance.getNguoiDung().getMaNguoiDung());
            response.setStudentName(attendance.getNguoiDung().getTenNguoiDung());

            if (attendance.getNguoiDung().getLopSinhVien() != null) {
                response.setClassName(attendance.getNguoiDung().getLopSinhVien().getTenLop());
            }

            response.setSessionId(attendance.getBuoiHoc().getMaBuoiHoc());
            response.setSubjectName(attendance.getBuoiHoc().getMonHoc().getTenMonHoc());
            response.setRoomName(attendance.getBuoiHoc().getPhong().getTenPhong());
            response.setStatus(attendance.getTrangThai());
            response.setMethod(attendance.getPhuongThuc());
            response.setRecordTime(attendance.getThoiGianDiemDanh());
            response.setSuccess(true);

            return response;
        }).collect(Collectors.toList());
    }

    private AttendanceResponse createErrorResponse(String message) {
        AttendanceResponse response = new AttendanceResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    // These methods would typically be in separate repository classes
    // They are included here as examples of what would be needed

    private Optional<BuoiHoc> findSessionById(Integer sessionId) {
        // This would typically use a dedicated repository
        // For example: return sessionRepository.findById(sessionId);

        // Mock implementation for this service
        return Optional.empty(); // Replace with actual implementation
    }

    private Optional<DiemDanh> findExistingAttendance(NguoiDung student, BuoiHoc session) {
        // This would typically use a dedicated repository
        // For example: return attendanceRepository.findByStudentAndSession(student, session);

        // Mock implementation for this service
        return Optional.empty(); // Replace with actual implementation
    }

    private DiemDanh saveAttendance(DiemDanh attendance) {
        // This would typically use a dedicated repository
        // For example: return attendanceRepository.save(attendance);

        // Mock implementation for this service
        return attendance; // Replace with actual implementation
    }
}