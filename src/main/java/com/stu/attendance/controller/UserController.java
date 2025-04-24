package com.stu.attendance.controller;

import com.stu.attendance.dto.UserInfoDto;
import com.stu.attendance.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")//lay thong tin nguoi dung
    public ResponseEntity<UserInfoDto> getCurrentUserProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @GetMapping("/{userId}")//lay thong tin nguoi dung
    public ResponseEntity<UserInfoDto> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/update-profile")//cap nhat thong tin
    public ResponseEntity<UserInfoDto> updateProfile(@RequestBody UserInfoDto userInfoDto) {
        return ResponseEntity.ok(userService.updateUserProfile(userInfoDto));
    }

    @PostMapping("/update-notification-settings")//cap nhat thong bao
    public ResponseEntity<String> updateNotificationSettings(
            @RequestParam boolean emailNotifications,
            @RequestParam boolean zaloNotifications) {
        userService.updateNotificationSettings(emailNotifications, zaloNotifications);
        return ResponseEntity.ok("Cài đặt thông báo đã được cập nhật.");
    }

    @PostMapping("/link-zalo")//lien ket zalo
    public ResponseEntity<String> linkZaloAccount(@RequestParam String zaloId) {
        userService.linkZaloAccount(zaloId);
        return ResponseEntity.ok("Tài khoản Zalo đã được liên kết thành công.");
    }
}