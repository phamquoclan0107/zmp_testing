package com.stu.attendance.dto;

import com.stu.attendance.entity.TaiKhoan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserInfoDto user;
    private TaiKhoan.Role role;
    private boolean isFirstLogin;

    public LoginResponse(String accessToken, String refreshToken, UserInfoDto user, TaiKhoan.Role role, boolean isFirstLogin) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.role = role;
        this.isFirstLogin = isFirstLogin;
    }
}