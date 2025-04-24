package com.stu.attendance.service;

import com.stu.attendance.dto.AttendanceRequest;
import com.stu.attendance.dto.AttendanceResponse;
import com.stu.attendance.dto.QrCodeRequest;
import com.stu.attendance.entity.*;
import com.stu.attendance.exception.ResourceNotFoundException;
import com.stu.attendance.repository.AttendanceRepository;
import com.stu.attendance.repository.SessionRepository;
import com.stu.attendance.repository.UserRepository;
import com.stu.attendance.security.JwtTokenProvider;
import com.stu.attendance.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Ghi nhận điểm danh cho một sinh viên
     *
     * @param request Yêu cầu điểm danh
     * @return Thông tin điểm danh đã ghi nhận
     */
    @Transactional
    public AttendanceResponse recordAttendance(AttendanceRequest request) {
        try {
            // Lấy thông tin buổi học
            BuoiHoc buoiHoc = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi học với mã " + request.getSessionId()));

            // Lấy thông tin sinh viên
            NguoiDung nguoiDung = userRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với mã " + request.getStudentId()));

            // Kiểm tra đã điểm danh chưa
            DiemDanh existingAttendance = attendanceRepository.findByBuoiHocAndNguoiDung(buoiHoc, nguoiDung);
            if (existingAttendance != null) {
                return mapToAttendanceResponse(existingAttendance, "Sinh viên đã được điểm danh trước đó");
            }

            // Tạo bản ghi điểm danh mới
            DiemDanh diemDanh = new DiemDanh();
            diemDanh.setBuoiHoc(buoiHoc);
            diemDanh.setNguoiDung(nguoiDung);
            diemDanh.setPhuongThuc(DiemDanh.PhuongThuc.manual);
            diemDanh.setTrangThai(DiemDanh.TrangThai.present);
            diemDanh.setThoiGianDiemDanh(LocalDateTime.now());

            // Lưu và trả về kết quả
            DiemDanh savedDiemDanh = attendanceRepository.save(diemDanh);
            return mapToAttendanceResponse(savedDiemDanh, "Điểm danh thành công");

        } catch (ResourceNotFoundException e) {
            log.error("Lỗi khi ghi nhận điểm danh: {}", e.getMessage());
            AttendanceResponse response = new AttendanceResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            log.error("Lỗi khi ghi nhận điểm danh: {}", e.getMessage());
            AttendanceResponse response = new AttendanceResponse();
            response.setSuccess(false);
            response.setMessage("Có lỗi xảy ra khi ghi nhận điểm danh");
            return response;
        }
    }

    /**
     * Ghi nhận điểm danh cho nhiều sinh viên
     *
     * @param request Yêu cầu điểm danh hàng loạt
     * @return Danh sách thông tin điểm danh đã ghi nhận
     */
    @Transactional
    public List<AttendanceResponse> recordBatchAttendance(AttendanceRequest request) {
        List<AttendanceResponse> responses = new ArrayList<>();

        try {
            // Lấy thông tin buổi học
            BuoiHoc buoiHoc = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi học với mã " + request.getSessionId()));

            // Lấy danh sách mã sinh viên
            List<String> studentIds = request.getStudentAttendances().stream()
                    .map(AttendanceRequest.StudentAttendance::getStudentId)
                    .collect(Collectors.toList());

            // Tìm các bản ghi điểm danh đã tồn tại
            List<DiemDanh> existingAttendances = attendanceRepository.findBySessionAndStudentIds(request.getSessionId(), studentIds);
            Map<String, DiemDanh> existingMap = existingAttendances.stream()
                    .collect(Collectors.toMap(a -> a.getNguoiDung().getMaNguoiDung(), a -> a));

            // Lấy danh sách người dùng
            List<NguoiDung> nguoiDungs = userRepository.findAllById(studentIds);
            Map<String, NguoiDung> userMap = nguoiDungs.stream()
                    .collect(Collectors.toMap(NguoiDung::getMaNguoiDung, u -> u));

            // Xử lý từng sinh viên
            LocalDateTime now = LocalDateTime.now();
            for (AttendanceRequest.StudentAttendance studentAttendance : request.getStudentAttendances()) {
                String studentId = studentAttendance.getStudentId();
                NguoiDung nguoiDung = userMap.get(studentId);

                // Nếu không tìm thấy sinh viên
                if (nguoiDung == null) {
                    AttendanceResponse errorResponse = new AttendanceResponse();
                    errorResponse.setStudentId(studentId);
                    errorResponse.setSuccess(false);
                    errorResponse.setMessage("Không tìm thấy sinh viên với mã " + studentId);
                    responses.add(errorResponse);
                    continue;
                }

                // Nếu đã điểm danh trước đó
                if (existingMap.containsKey(studentId)) {
                    DiemDanh existingAttendance = existingMap.get(studentId);
                    // Cập nhật trạng thái nếu thay đổi
                    if (existingAttendance.getTrangThai() != studentAttendance.getStatus()) {
                        existingAttendance.setTrangThai(studentAttendance.getStatus());
                        existingAttendance.setThoiGianDiemDanh(now);
                        DiemDanh updatedAttendance = attendanceRepository.save(existingAttendance);
                        responses.add(mapToAttendanceResponse(updatedAttendance, "Cập nhật trạng thái điểm danh thành công"));
                    } else {
                        responses.add(mapToAttendanceResponse(existingAttendance, "Sinh viên đã được điểm danh trước đó"));
                    }
                    continue;
                }

                // Tạo bản ghi điểm danh mới
                DiemDanh diemDanh = new DiemDanh();
                diemDanh.setBuoiHoc(buoiHoc);
                diemDanh.setNguoiDung(nguoiDung);
                diemDanh.setPhuongThuc(DiemDanh.PhuongThuc.manual);
                diemDanh.setTrangThai(studentAttendance.getStatus());
                diemDanh.setThoiGianDiemDanh(now);

                // Lưu và thêm vào kết quả
                DiemDanh savedDiemDanh = attendanceRepository.save(diemDanh);
                responses.add(mapToAttendanceResponse(savedDiemDanh, "Điểm danh thành công"));
            }

            return responses;
        } catch (ResourceNotFoundException e) {
            log.error("Lỗi khi ghi nhận điểm danh hàng loạt: {}", e.getMessage());
            AttendanceResponse errorResponse = new AttendanceResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            responses.add(errorResponse);
            return responses;
        } catch (Exception e) {
            log.error("Lỗi khi ghi nhận điểm danh hàng loạt: {}", e.getMessage());
            AttendanceResponse errorResponse = new AttendanceResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Có lỗi xảy ra khi ghi nhận điểm danh hàng loạt: " + e.getMessage());
            responses.add(errorResponse);
            return responses;
        }
    }

    /**
     * Lấy thông tin điểm danh theo buổi học
     *
     * @param sessionId Mã buổi học
     * @return Danh sách thông tin điểm danh
     */
    public List<AttendanceResponse> getAttendancesBySession(Integer sessionId) {
        // Kiểm tra buổi học tồn tại
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Không tìm thấy buổi học với mã " + sessionId);
        }

        // Lấy danh sách điểm danh và chuyển đổi thành DTO
        List<DiemDanh> diemDanhs = attendanceRepository.findByBuoiHoc_MaBuoiHoc(sessionId);
        return diemDanhs.stream()
                .map(dd -> mapToAttendanceResponse(dd, "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin điểm danh theo sinh viên
     *
     * @param studentId Mã sinh viên
     * @return Danh sách thông tin điểm danh
     */
    public List<AttendanceResponse> getAttendancesByStudent(String studentId) {
        // Kiểm tra sinh viên tồn tại
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Không tìm thấy sinh viên với mã " + studentId);
        }

        // Lấy danh sách điểm danh và chuyển đổi thành DTO
        List<DiemDanh> diemDanhs = attendanceRepository.findByNguoiDung_MaNguoiDung(studentId);
        return diemDanhs.stream()
                .map(dd -> mapToAttendanceResponse(dd, "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * Tạo mã QR cho điểm danh
     *
     * @param request Yêu cầu tạo mã QR
     * @return Mảng byte chứa hình ảnh mã QR
     */
    public byte[] generateQrCode(QrCodeRequest request) {
        // Kiểm tra buổi học tồn tại
        if (!sessionRepository.existsById(request.getSessionId())) {
            throw new ResourceNotFoundException("Không tìm thấy buổi học với mã " + request.getSessionId());
        }

        // Tạo token JWT cho QR code
        String qrToken = jwtTokenProvider.generateQrToken(request.getSessionId(), request.getValidityMinutes());

        // Tạo mã QR
        return qrCodeGenerator.generateQrCode(qrToken, request.getSize());
    }

    /**
     * Xác thực mã QR và ghi nhận điểm danh
     *
     * @param token     Token JWT từ mã QR
     * @param studentId Mã sinh viên
     * @return Thông tin điểm danh đã ghi nhận
     */
    @Transactional
    public AttendanceResponse verifyQrCode(String token, String studentId) {
        try {
            // Xác thực token QR
            Map<String, Object> tokenClaims = jwtTokenProvider.validateQrToken(token);
            if (tokenClaims == null) {
                AttendanceResponse errorResponse = new AttendanceResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Mã QR không hợp lệ hoặc đã hết hạn");
                return errorResponse;
            }

            // Lấy mã buổi học từ token
            Integer sessionId = (Integer) tokenClaims.get("sessionId");
            if (sessionId == null) {
                AttendanceResponse errorResponse = new AttendanceResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Mã QR không chứa thông tin buổi học");
                return errorResponse;
            }

            // Lấy thông tin buổi học
            BuoiHoc buoiHoc = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi học với mã " + sessionId));

            // Lấy thông tin sinh viên
            NguoiDung nguoiDung = userRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với mã " + studentId));

            // Kiểm tra đã điểm danh chưa
            DiemDanh existingAttendance = attendanceRepository.findByBuoiHocAndNguoiDung(buoiHoc, nguoiDung);
            if (existingAttendance != null) {
                return mapToAttendanceResponse(existingAttendance, "Sinh viên đã được điểm danh trước đó");
            }

            // Tạo bản ghi điểm danh mới
            DiemDanh diemDanh = new DiemDanh();
            diemDanh.setBuoiHoc(buoiHoc);
            diemDanh.setNguoiDung(nguoiDung);
            diemDanh.setPhuongThuc(DiemDanh.PhuongThuc.qr_code);
            diemDanh.setTrangThai(DiemDanh.TrangThai.present);
            diemDanh.setThoiGianDiemDanh(LocalDateTime.now());

            // Lưu và trả về kết quả
            DiemDanh savedDiemDanh = attendanceRepository.save(diemDanh);
            return mapToAttendanceResponse(savedDiemDanh, "Điểm danh bằng mã QR thành công");

        } catch (ResourceNotFoundException e) {
            log.error("Lỗi khi xác thực mã QR: {}", e.getMessage());
            AttendanceResponse response = new AttendanceResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            log.error("Lỗi khi xác thực mã QR: {}", e.getMessage());
            AttendanceResponse response = new AttendanceResponse();
            response.setSuccess(false);
            response.setMessage("Có lỗi xảy ra khi xác thực mã QR: " + e.getMessage());
            return response;
        }
    }

    /**
     * Chuyển đổi từ entity DiemDanh sang DTO AttendanceResponse
     *
     * @param diemDanh Đối tượng DiemDanh
     * @param message  Thông báo kết quả
     * @return Đối tượng AttendanceResponse
     */
    private AttendanceResponse mapToAttendanceResponse(DiemDanh diemDanh, String message) {
        AttendanceResponse response = new AttendanceResponse();
        response.setAttendanceId(diemDanh.getMaDiemDanh());
        response.setStudentId(diemDanh.getNguoiDung().getMaNguoiDung());
        response.setStudentName(diemDanh.getNguoiDung().getTenNguoiDung());

        // Set class name if student has a class
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
        response.setMessage(message);

        return response;
    }
}