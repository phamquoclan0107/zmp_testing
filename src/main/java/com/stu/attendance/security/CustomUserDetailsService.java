package com.stu.attendance.security;

import com.stu.attendance.entity.TaiKhoan;
import com.stu.attendance.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm tài khoản theo tên đăng nhập
        TaiKhoan taiKhoan = authRepository.findByTenTaiKhoan(username)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại: " + username));

        // Tạo quyền truy cập dựa trên vai trò
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + taiKhoan.getRole().name().toUpperCase());

        // Tạo UserDetails từ thông tin tài khoản
        return new User(
                taiKhoan.getTenTaiKhoan(),
                taiKhoan.getMatKhau(),
                Collections.singleton(authority)
        );
    }
}