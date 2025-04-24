package com.stu.attendance.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.stu.attendance.entity.BuoiHoc;
import com.stu.attendance.entity.DiemDanh;
import com.stu.attendance.entity.NguoiDung;
import com.stu.attendance.repository.ZaloRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling Zalo integration features including webhooks, account linking,
 * and notification sending.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ZaloService {

    private final ZaloRepository zaloRepository;

    /**
     * Process incoming webhook from Zalo
     *
     * @param payload The webhook payload from Zalo
     * @return Response map for the webhook
     */
    public Map<String, Object> processWebhook(Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Received Zalo webhook: {}", payload);

            // Extract event type
            String event = (String) payload.getOrDefault("event_name", "");

            // Process different event types
            switch (event) {
                case "user_send_text":
                    handleUserMessage(payload);
                    break;
                case "user_submit_info":
                    handleUserInfo(payload);
                    break;
                case "follow":
                    handleUserFollow(payload);
                    break;
                case "unfollow":
                    handleUserUnfollow(payload);
                    break;
                default:
                    log.warn("Unhandled Zalo event type: {}", event);
            }

            response.put("success", true);
            response.put("message", "Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing Zalo webhook: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error processing webhook: " + e.getMessage());
        }

        return response;
    }

    /**
     * Handle text messages sent by users
     */
    private void handleUserMessage(Map<String, Object> payload) {
        try {
            // Extract user ID and message content
            Map<String, Object> sender = (Map<String, Object>) payload.get("sender");
            String zaloId = (String) sender.get("id");

            Map<String, Object> message = (Map<String, Object>) payload.get("message");
            String messageText = (String) message.get("text");

            log.info("Received message from user {}: {}", zaloId, messageText);

            // Check if user is already linked
            Optional<NguoiDung> userOpt = zaloRepository.findByZaloId(zaloId);

            // Process message based on content
            if (messageText.toLowerCase().startsWith("diemdanh")) {
                handleAttendanceCommand(zaloId, messageText);
            } else if (messageText.toLowerCase().startsWith("lienket")) {
                handleLinkCommand(zaloId, messageText);
            } else {
                // Send help message if no command recognized
                sendMessage(zaloId, buildHelpMessage());
            }
        } catch (Exception e) {
            log.error("Error handling user message: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle attendance commands from users
     */
    private void handleAttendanceCommand(String zaloId, String messageText) {
        // Implementation for handling attendance command
        // This would involve extracting session ID if provided and checking if
        // the user is registered for that session

        // For example: "diemdanh 123" would attempt to mark attendance for session 123
        try {
            String[] parts = messageText.split("\\s+", 2);
            if (parts.length < 2) {
                sendMessage(zaloId, "Vui lòng cung cấp mã buổi học. Ví dụ: diemdanh 123");
                return;
            }

            Integer sessionId;
            try {
                sessionId = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                sendMessage(zaloId, "Mã buổi học không hợp lệ. Vui lòng nhập số.");
                return;
            }

            // Check if user is linked to an account
            Optional<NguoiDung> userOpt = zaloRepository.findByZaloId(zaloId);
            if (userOpt.isEmpty()) {
                sendMessage(zaloId, "Bạn chưa liên kết tài khoản. Vui lòng liên kết bằng cách gửi: lienket [mã sinh viên]");
                return;
            }

            // Check if session exists
            Optional<BuoiHoc> sessionOpt = zaloRepository.findSessionById(sessionId);
            if (sessionOpt.isEmpty()) {
                sendMessage(zaloId, "Buổi học không tồn tại, vui lòng kiểm tra lại mã buổi học.");
                return;
            }

            // Here would be the actual attendance marking logic
            // For demonstration, just returning a success message
            sendMessage(zaloId, "Đã ghi nhận điểm danh của bạn cho buổi học " + sessionId);

        } catch (Exception e) {
            log.error("Error handling attendance command: {}", e.getMessage(), e);
            sendMessage(zaloId, "Có lỗi xảy ra khi xử lý yêu cầu điểm danh. Vui lòng thử lại sau.");
        }
    }

    /**
     * Handle account linking commands from users
     */
    private void handleLinkCommand(String zaloId, String messageText) {
        try {
            String[] parts = messageText.split("\\s+", 2);
            if (parts.length < 2) {
                sendMessage(zaloId, "Vui lòng cung cấp mã sinh viên. Ví dụ: lienket DH51234567");
                return;
            }

            String studentId = parts[1].trim();

            // Check if this Zalo ID is already linked
            Optional<NguoiDung> existingUser = zaloRepository.findByZaloId(zaloId);
            if (existingUser.isPresent()) {
                sendMessage(zaloId, "Tài khoản Zalo của bạn đã được liên kết với mã sinh viên "
                        + existingUser.get().getMaNguoiDung());
                return;
            }

            // Check if student ID exists
            Optional<NguoiDung> studentOpt = zaloRepository.findByMaNguoiDung(studentId);
            if (studentOpt.isEmpty()) {
                sendMessage(zaloId, "Mã sinh viên không tồn tại. Vui lòng kiểm tra lại.");
                return;
            }

            // Link account
            NguoiDung student = studentOpt.get();
            student.setSdt(zaloId);
            zaloRepository.save(student);

            sendMessage(zaloId, "Liên kết thành công! Tài khoản Zalo của bạn đã được liên kết với "
                    + student.getTenNguoiDung() + " (" + student.getMaNguoiDung() + ")");

        } catch (Exception e) {
            log.error("Error handling link command: {}", e.getMessage(), e);
            sendMessage(zaloId, "Có lỗi xảy ra khi xử lý yêu cầu liên kết. Vui lòng thử lại sau.");
        }
    }

    /**
     * Handle user info submission events
     */
    private void handleUserInfo(Map<String, Object> payload) {
        // Implementation for handling user info submission
        log.info("User submitted info: {}", payload);
    }

    /**
     * Handle user follow events
     */
    private void handleUserFollow(Map<String, Object> payload) {
        try {
            Map<String, Object> follower = (Map<String, Object>) payload.get("follower");
            String zaloId = (String) follower.get("id");

            // Send welcome message
            sendMessage(zaloId, "Chào mừng bạn đến với Hệ thống Điểm danh STU! "
                    + "Vui lòng liên kết tài khoản bằng cách gửi: lienket [mã sinh viên]");

        } catch (Exception e) {
            log.error("Error handling user follow: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle user unfollow events
     */
    private void handleUserUnfollow(Map<String, Object> payload) {
        try {
            Map<String, Object> follower = (Map<String, Object>) payload.get("follower");
            String zaloId = (String) follower.get("id");

            // Find and update user record if exists
            Optional<NguoiDung> userOpt = zaloRepository.findByZaloId(zaloId);
            if (userOpt.isPresent()) {
                NguoiDung user = userOpt.get();
                // Just log the unfollow, don't unlink account in case it's temporary
                log.info("User {} has unfollowed but account remains linked", user.getMaNguoiDung());
            }

        } catch (Exception e) {
            log.error("Error handling user unfollow: {}", e.getMessage(), e);
        }
    }

    /**
     * Send message to Zalo user
     */
    private void sendMessage(String zaloId, String message) {
        // This would be implemented to call Zalo API
        // For now just logging the message that would be sent
        log.info("Sending message to {}: {}", zaloId, message);

        // Actual implementation would use Zalo API client
        // Example:
        // zaloApiClient.sendTextMessage(zaloId, message);
    }

    /**
     * Verify webhook callback from Zalo
     *
     * @param challenge The challenge string from Zalo
     * @param token Optional verification token
     * @return The challenge string if verification passes
     */
    public String verifyWebhook(String challenge, String token) {
        // In a real implementation, you'd verify the token against your configured secret
        // For simplicity, we're just echoing back the challenge
        log.info("Verifying webhook with challenge: {}", challenge);
        return challenge;
    }

    /**
     * Link a user account with their Zalo ID
     *
     * @param userId The user's ID in the system
     * @param zaloId The user's Zalo ID
     * @return Response map with operation result
     */
    public Map<String, Object> linkUserAccount(String userId, String zaloId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find user by ID
            Optional<NguoiDung> userOpt = zaloRepository.findByMaNguoiDung(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Người dùng không tồn tại");
                return response;
            }

            // Check if Zalo ID is already linked to another account
            Optional<NguoiDung> existingUserOpt = zaloRepository.findByZaloId(zaloId);
            if (existingUserOpt.isPresent() && !existingUserOpt.get().getMaNguoiDung().equals(userId)) {
                response.put("success", false);
                response.put("message", "Zalo ID đã được liên kết với tài khoản khác");
                return response;
            }

            // Update user with Zalo ID
            NguoiDung user = userOpt.get();
            user.setSdt(zaloId); // Using sdt field to store Zalo ID
            zaloRepository.save(user);

            // Send confirmation message to user on Zalo
            sendMessage(zaloId, "Tài khoản của bạn đã được liên kết thành công với hệ thống điểm danh STU!");

            response.put("success", true);
            response.put("message", "Liên kết tài khoản thành công");
            response.put("user", user.getMaNguoiDung());
            response.put("name", user.getTenNguoiDung());

        } catch (Exception e) {
            log.error("Error linking user account: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }

        return response;
    }

    /**
     * Send attendance notification to student
     *
     * @param userId The user's ID in the system
     * @param attendanceId The attendance record ID
     * @return Response map with operation result
     */
    public Map<String, Object> sendAttendanceNotification(String userId, Integer attendanceId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find user
            Optional<NguoiDung> userOpt = zaloRepository.findByMaNguoiDung(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Người dùng không tồn tại");
                return response;
            }

            NguoiDung user = userOpt.get();
            if (user.getSdt() == null || user.getSdt().isEmpty()) {
                response.put("success", false);
                response.put("message", "Người dùng chưa liên kết tài khoản Zalo");
                return response;
            }

            // Find attendance record
            Optional<DiemDanh> attendanceOpt = zaloRepository.findAttendanceById(attendanceId);
            if (attendanceOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Bản ghi điểm danh không tồn tại");
                return response;
            }

            DiemDanh attendance = attendanceOpt.get();
            BuoiHoc session = attendance.getBuoiHoc();

            // Generate notification message
            String status;
            switch (attendance.getTrangThai()) {
                case present:
                    status = "Có mặt";
                    break;
                case absent:
                    status = "Vắng mặt";
                    break;
                case late:
                    status = "Đi trễ";
                    break;
                default:
                    status = "Không xác định";
            }

            String message = String.format(
                    "Thông báo điểm danh:\n" +
                            "Môn học: %s\n" +
                            "Ngày: %s\n" +
                            "Tiết: %d-%d\n" +
                            "Phòng: %s\n" +
                            "Trạng thái: %s\n" +
                            "Thời gian: %s",
                    session.getMonHoc().getTenMonHoc(),
                    session.getNgayHoc().toString(),
                    session.getTietBatDau(),
                    session.getTietKetThuc(),
                    session.getPhong().getTenPhong(),
                    status,
                    attendance.getThoiGianDiemDanh().toString()
            );

            // Send message
            sendMessage(user.getSdt(), message);

            response.put("success", true);
            response.put("message", "Đã gửi thông báo thành công");

        } catch (Exception e) {
            log.error("Error sending attendance notification: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }

        return response;
    }

    /**
     * Send session reminder to all students in the class
     *
     * @param sessionId The session ID
     * @return Response map with operation result
     */
//    public Map<String, Object> sendSessionReminder(Integer sessionId) {
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            // Find session
//            Optional<BuoiHoc> sessionOpt = zaloRepository.findSessionById(sessionId);
//            if (sessionOpt.isEmpty()) {
//                response.put("success", false);
//                response.put("message", "Buổi học không tồn tại");
//                return response;
//            }
//
//            BuoiHoc session = sessionOpt.get();
//
//            // Find all students in the class
//            List<NguoiDung> students = zaloRepository.findStudentsBySession(sessionId);
//
//            int sentCount = 0;
//            int totalCount = students.size();
//
//            // Generate reminder message
//            String reminderMessage = String.format(
//                    "Nhắc nhở lịch học:\n" +
//                            "Môn học: %s\n" +
//                            "Ngày: %s\n" +
//                            "Tiết: %d-%d\n" +
//                            "Phòng: %s\n" +
//                            "Vui lòng đến đúng giờ!",
//                    session.getMonHoc().getTenMonHoc(),
//                    session.getNgayHoc().toString(),
//                    session.getTietBatDau(),
//                    session.getTietKetThuc(),
//                    session.getPhong().getTenPhong()
//            );
//
//            // Send reminders to all students with linked Zalo accounts
//            for (NguoiDung student : students) {
//                if (student.getSdt() != null && !student.getSdt().isEmpty()) {
//                    sendMessage(student.getSdt(), reminderMessage);
//                    sentCount++;
//                }
//            }
//
//            response.put("success", true);
//            response.put("message", String.format("Đã gửi nhắc nhở cho %d/%d sinh viên", sentCount, totalCount));
//            response.put("sentCount", sentCount);
//            response.put("totalCount", totalCount);
//
//        } catch (Exception e) {
//            log.error("Error sending session reminder: {}", e.getMessage(), e);
//            response.put("success", false);
//            response.put("message", "Lỗi: " + e.getMessage());
//        }
//
//        return response;
//    }

    /**
     * Build help message for users
     */
    private String buildHelpMessage() {
        return "Chào mừng bạn đến với Hệ thống Điểm danh STU!\n\n" +
                "Các lệnh hỗ trợ:\n" +
                "- lienket [mã sinh viên]: Liên kết tài khoản Zalo với hệ thống\n" +
                "- diemdanh [mã buổi học]: Điểm danh cho buổi học\n" +
                "- help: Hiển thị trợ giúp";
    }

    /**
     * Send notifications to users with Zalo linked accounts
     */
    public void sendBulkNotifications(String message) {
        try {
            List<NguoiDung> users = zaloRepository.findUsersWithZaloLinked();
            log.info("Sending bulk notification to {} users", users.size());

            for (NguoiDung user : users) {
                if (user.getSdt() != null && !user.getSdt().isEmpty()) {
                    sendMessage(user.getSdt(), message);
                }
            }
        } catch (Exception e) {
            log.error("Error sending bulk notifications: {}", e.getMessage(), e);
        }
    }
}