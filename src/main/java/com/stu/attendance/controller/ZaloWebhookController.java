package com.stu.attendance.controller;

import com.stu.attendance.service.ZaloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/zalo-webhook")
@RequiredArgsConstructor
public class ZaloWebhookController {

    private final ZaloService zaloService;

    @PostMapping//xu ly webhook
    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(zaloService.processWebhook(payload));
    }

    @GetMapping("/verify")//xu ly xác thức webhook
    public ResponseEntity<String> verifyWebhook(
            @RequestParam String challenge,
            @RequestParam(required = false) String token) {
        return ResponseEntity.ok(zaloService.verifyWebhook(challenge, token));
    }

    @PostMapping("/link-account")//xữ lý lien ket
    public ResponseEntity<Map<String, Object>> linkZaloAccount(
            @RequestParam String userId,
            @RequestParam String zaloId) {
        return ResponseEntity.ok(zaloService.linkUserAccount(userId, zaloId));
    }

    @PostMapping("/send-notification")//xu ly gui thong bao
    public ResponseEntity<Map<String, Object>> sendAttendanceNotification(
            @RequestParam String userId,
            @RequestParam Integer attendanceId) {
        return ResponseEntity.ok(zaloService.sendAttendanceNotification(userId, attendanceId));
    }

//    @PostMapping("/send-reminder")
//    public ResponseEntity<Map<String, Object>> sendSessionReminder(
//            @RequestParam Integer sessionId) {
//        return ResponseEntity.ok(zaloService.sendSessionReminder(sessionId));
//    }
}