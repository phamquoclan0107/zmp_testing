package com.stu.attendance.repository;

import com.stu.attendance.entity.BuoiHoc;
import com.stu.attendance.entity.LopSinhVien;
import com.stu.attendance.entity.MonHoc;
import com.stu.attendance.entity.NguoiDung;
import com.stu.attendance.entity.Phong;
import com.stu.attendance.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<NguoiDung, String> {

    // User Management
    @Query("SELECT n FROM NguoiDung n WHERE (:keyword IS NULL OR n.tenNguoiDung LIKE %:keyword% OR n.maNguoiDung LIKE %:keyword% OR n.email LIKE %:keyword%) AND (:role IS NULL OR n.taiKhoan.role = :role)")
    List<NguoiDung> findAllUsers(@Param("keyword") String keyword, @Param("role") TaiKhoan.Role role);

    // Check if user exists
    boolean existsByEmail(String email);
    boolean existsByMaNguoiDung(String maNguoiDung);

    // For account management
    @Query("SELECT t FROM TaiKhoan t WHERE t.tenTaiKhoan = :username")
    TaiKhoan findTaiKhoanByUsername(String username);

    // Class Management
    @Query("SELECT l FROM LopSinhVien l WHERE (:keyword IS NULL OR l.tenLop LIKE %:keyword% OR l.maLop LIKE %:keyword%)")
    List<LopSinhVien> findAllClasses(@Param("keyword") String keyword);

//    boolean existsByMaLop(String maLop);

    // Subject Management
    @Query("SELECT m FROM MonHoc m WHERE (:keyword IS NULL OR m.tenMonHoc LIKE %:keyword% OR m.maMonHoc LIKE %:keyword%)")
    List<MonHoc> findAllSubjects(@Param("keyword") String keyword);

//    boolean existsByMaMonHoc(String maMonHoc);

    // Room Management
    @Query("SELECT p FROM Phong p WHERE (:keyword IS NULL OR p.tenPhong LIKE %:keyword% OR p.maPhong LIKE %:keyword%)")
    List<Phong> findAllRooms(@Param("keyword") String keyword);

//    boolean existsByMaPhong(String maPhong);

    // Session Management
    @Query("SELECT b FROM BuoiHoc b WHERE (:startDate IS NULL OR b.ngayHoc >= :startDate) AND (:endDate IS NULL OR b.ngayHoc <= :endDate) AND (:subjectId IS NULL OR b.monHoc.maMonHoc = :subjectId) AND (:roomId IS NULL OR b.phong.maPhong = :roomId)")
    List<BuoiHoc> findAllSessions(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("subjectId") String subjectId,
            @Param("roomId") String roomId);


}