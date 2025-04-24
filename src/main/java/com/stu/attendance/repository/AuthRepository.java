package com.stu.attendance.repository;

import com.stu.attendance.entity.TaiKhoan;
import com.stu.attendance.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<TaiKhoan, String> {

    // Find account by username
    Optional<TaiKhoan> findByTenTaiKhoan(String username);

    // Check if username exists
    boolean existsByTenTaiKhoan(String username);

    // Find user by email for password reset
    @Query("SELECT n FROM NguoiDung n WHERE n.email = :email")
    Optional<NguoiDung> findNguoiDungByEmail(@Param("email") String email);

    // Find account by user ID
    @Query("SELECT t FROM TaiKhoan t WHERE t.nguoiDung.maNguoiDung = :userId")
    Optional<TaiKhoan> findByUserId(@Param("userId") String userId);

    // Find user info by account
    @Query("SELECT n FROM NguoiDung n WHERE n.taiKhoan = :taiKhoan")
    Optional<NguoiDung> findNguoiDungByTaiKhoan(@Param("taiKhoan") TaiKhoan taiKhoan);
}