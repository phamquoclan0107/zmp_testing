package com.stu.attendance.repository;

import com.stu.attendance.entity.NguoiDung;
import com.stu.attendance.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<NguoiDung, String> {

    // Find user by ID
    Optional<NguoiDung> findByMaNguoiDung(String userId);

    // Find current user profile
    @Query("SELECT n FROM NguoiDung n WHERE n.taiKhoan.tenTaiKhoan = :username")
    Optional<NguoiDung> findByUsername(@Param("username") String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find by email
    Optional<NguoiDung> findByEmail(String email);

    // For notification settings
    @Query("SELECT n FROM NguoiDung n WHERE n.taiKhoan.maTaiKhoan = :accountId")
    Optional<NguoiDung> findByAccountId(@Param("accountId") String accountId);

    @Query("SELECT n FROM NguoiDung n WHERE (:keyword IS NULL OR n.tenNguoiDung LIKE %:keyword% OR n.maNguoiDung LIKE %:keyword% OR n.email LIKE %:keyword%) AND (:role IS NULL OR n.taiKhoan.role = :role)")
    List<NguoiDung> findAllUsers(@Param("keyword") String keyword, @Param("role") TaiKhoan.Role role);
    //Phần bị loi
    boolean existsByMaNguoiDung(String maNguoiDung);

    @Query("SELECT t FROM TaiKhoan t WHERE t.tenTaiKhoan = :username")
    TaiKhoan findTaiKhoanByUsername(String username);

    @Query("SELECT ntg.nguoiDung FROM NguoiThamGia ntg WHERE ntg.buoiHoc.id = :buoiHocId")
    List<NguoiDung> findNguoiDungByBuoiHocId(@Param("buoiHocId") Integer buoiHocId);

}