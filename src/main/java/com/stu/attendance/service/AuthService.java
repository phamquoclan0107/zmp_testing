package com.stu.attendance.service;

import com.stu.attendance.dto.LoginRequest;
import com.stu.attendance.dto.LoginResponse;
import com.stu.attendance.dto.UserInfoDto;
import com.stu.attendance.entity.NguoiDung;
import com.stu.attendance.entity.TaiKhoan;
import com.stu.attendance.repository.AuthRepository;
import com.stu.attendance.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Authenticate user and generate tokens
     */
    public LoginResponse authenticate(LoginRequest loginRequest) {
        // Authenticate with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details from repository
        TaiKhoan taiKhoan = authRepository.findByTenTaiKhoan(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại"));

        NguoiDung nguoiDung = authRepository.findNguoiDungByTaiKhoan(taiKhoan)
                .orElseThrow(() -> new RuntimeException("Thông tin người dùng không tồn tại"));

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(taiKhoan);
        String refreshToken = jwtTokenProvider.generateRefreshToken(taiKhoan);

        // Check if this is first login (assuming default password is being used)
        boolean isFirstLogin = isDefaultPassword(taiKhoan);

        // Create user info DTO
        UserInfoDto userInfoDto = mapToUserInfoDto(nguoiDung, taiKhoan);

        return new LoginResponse(accessToken, refreshToken, userInfoDto, taiKhoan.getRole(), isFirstLogin);
    }

    /**
     * Refresh access token using refresh token
     */
    public LoginResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // Extract username from token
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // Get user details
        TaiKhoan taiKhoan = authRepository.findByTenTaiKhoan(username)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại"));

        NguoiDung nguoiDung = authRepository.findNguoiDungByTaiKhoan(taiKhoan)
                .orElseThrow(() -> new RuntimeException("Thông tin người dùng không tồn tại"));

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(taiKhoan);

        // Create user info DTO
        UserInfoDto userInfoDto = mapToUserInfoDto(nguoiDung, taiKhoan);

        return new LoginResponse(newAccessToken, refreshToken, userInfoDto, taiKhoan.getRole(), false);
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        TaiKhoan taiKhoan = authRepository.findByTenTaiKhoan(username)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, taiKhoan.getMatKhau())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác");
        }

        // Check if new password is same as old password
        if (passwordEncoder.matches(newPassword, taiKhoan.getMatKhau())) {
            throw new RuntimeException("Mật khẩu mới không được giống mật khẩu cũ");
        }

        // Validate password complexity
        if (!isPasswordComplex(newPassword)) {
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số");
        }

        // Update password
        taiKhoan.setMatKhau(passwordEncoder.encode(newPassword));
        authRepository.save(taiKhoan);
    }

    private boolean isPasswordComplex(String password) {
        // Password must be at least 8 characters long and contain at least one uppercase letter,
        // one lowercase letter, and one digit
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        return password.matches(pattern);
    }

    /**
     * Reset password and send email
     */
    @Transactional
    public void resetPassword(String email) {
        // Find user by email
        NguoiDung nguoiDung = authRepository.findNguoiDungByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        // Get associated account
        TaiKhoan taiKhoan = authRepository.findByUserId(nguoiDung.getMaNguoiDung())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        // Generate temporary password
        String temporaryPassword = generateTemporaryPassword();

        // Update password in database
        taiKhoan.setMatKhau(passwordEncoder.encode(temporaryPassword));
        authRepository.save(taiKhoan);

        // Send email with temporary password
        String subject = "Khôi phục mật khẩu - Hệ thống điểm danh";
        String content = "Xin chào " + nguoiDung.getTenNguoiDung() + ",\n\n"
                + "Mật khẩu tạm thời của bạn là: " + temporaryPassword + "\n"
                + "Vui lòng đăng nhập và đổi mật khẩu ngay sau khi nhận được email này.\n\n"
                + "Trân trọng,\nHệ thống điểm danh STU";

        emailService.sendEmail(email, subject, content);
    }

    /**
     * Logout current user
     */
    public void logout() {
        // Clear security context
        SecurityContextHolder.clearContext();
        // Note: In a stateful setup, we would also invalidate the JWT token
        // but in a stateless JWT setup, we typically rely on token expiration
    }

    /**
     * Map entity to DTO
     */
    private UserInfoDto mapToUserInfoDto(NguoiDung nguoiDung, TaiKhoan taiKhoan) {
        UserInfoDto dto = new UserInfoDto();
        dto.setUserId(nguoiDung.getMaNguoiDung());
        dto.setFullName(nguoiDung.getTenNguoiDung());
        dto.setEmail(nguoiDung.getEmail());
        dto.setPhone(nguoiDung.getSdt());
        dto.setUsername(taiKhoan.getTenTaiKhoan());
        dto.setRole(taiKhoan.getRole());

        if (nguoiDung.getLopSinhVien() != null) {
            dto.setClassId(nguoiDung.getLopSinhVien().getMaLop());
            dto.setClassName(nguoiDung.getLopSinhVien().getTenLop());
        }

        return dto;
    }

    /**
     * Check if user is using default password
     * This is a placeholder implementation - you would replace with your actual logic
     */
    private boolean isDefaultPassword(TaiKhoan taiKhoan) {
        // Example: Check if password matches a pattern or is identical to username
        // In a real app, you might track this with a database flag
        return passwordEncoder.matches(taiKhoan.getTenTaiKhoan(), taiKhoan.getMatKhau()) ||
                passwordEncoder.matches("default123", taiKhoan.getMatKhau());
    }

    /**
     * Generate a random temporary password
     */
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}