package com.stu.attendance.repository;

import com.stu.attendance.entity.BuoiHoc;
import com.stu.attendance.entity.DiemDanh;
import com.stu.attendance.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZaloRepository extends JpaRepository<NguoiDung, String> {

    // Find user for linking Zalo account
    Optional<NguoiDung> findByMaNguoiDung(String userId);

    // Find user by Zalo ID
    @Query("SELECT n FROM NguoiDung n WHERE n.sdt = :zaloId")
    Optional<NguoiDung> findByZaloId(@Param("zaloId") String zaloId);

    // Find attendance by ID
    @Query("SELECT d FROM DiemDanh d WHERE d.maDiemDanh = :attendanceId")
    Optional<DiemDanh> findAttendanceById(@Param("attendanceId") Integer attendanceId);

    // Get session details
    @Query("SELECT b FROM BuoiHoc b WHERE b.maBuoiHoc = :sessionId")
    Optional<BuoiHoc> findSessionById(@Param("sessionId") Integer sessionId);

    // Get students in a class
//    @Query("SELECT n FROM NguoiDung n WHERE n.lopSinhVien.maLop IN (SELECT s.lopSinhVien.maLop FROM NguoiDung s WHERE s.diemDanhs IN (SELECT d FROM DiemDanh d WHERE d.buoiHoc.maBuoiHoc = :sessionId)) AND n.taiKhoan.role = 'student'")
//    List<NguoiDung> findStudentsBySession(@Param("sessionId") Integer sessionId);

    // Find users with Zalo notifications enabled
    @Query(value = "SELECT n FROM NguoiDung n WHERE n.sdt IS NOT NULL AND LENGTH(n.sdt) > 0")
    List<NguoiDung> findUsersWithZaloLinked();
}