package com.stu.attendance.repository;

import com.stu.attendance.entity.BuoiHoc;
import com.stu.attendance.entity.DiemDanh;
import com.stu.attendance.entity.LopSinhVien;
import com.stu.attendance.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<BuoiHoc, Integer> {

    // Get teacher's sessions
//    @Query("SELECT DISTINCT b FROM BuoiHoc b " +
//            "WHERE (:startDate IS NULL OR b.ngayHoc >= :startDate) " +
//            "AND (:endDate IS NULL OR b.ngayHoc <= :endDate) " +
//            "AND EXISTS (SELECT 1 FROM DiemDanh d JOIN d.buoiHoc bh " +
//            "WHERE bh.monHoc = b.monHoc AND d.nguoiDung.taiKhoan.role = 'teacher' " +
//            "AND d.nguoiDung.maNguoiDung = :teacherId)")
//    List<BuoiHoc> findTeacherSessions(
//            @Param("teacherId") String teacherId,
//            @Param("startDate") LocalDate startDate,
//            @Param("endDate") LocalDate endDate);

    // Get today's sessions for a teacher
//    @Query("SELECT DISTINCT b FROM BuoiHoc b " +
//            "WHERE b.ngayHoc = CURRENT_DATE " +
//            "AND EXISTS (SELECT 1 FROM DiemDanh d JOIN d.buoiHoc bh " +
//            "WHERE bh.monHoc = b.monHoc AND d.nguoiDung.taiKhoan.role = 'teacher' " +
//            "AND d.nguoiDung.maNguoiDung = :teacherId)")
//    List<BuoiHoc> findTodaySessions(@Param("teacherId") String teacherId);

    // Get session details
    BuoiHoc findByMaBuoiHoc(Integer sessionId);

    // Find attendance by ID
    @Query("SELECT d FROM DiemDanh d WHERE d.maDiemDanh = :attendanceId")
    DiemDanh findAttendanceById(@Param("attendanceId") Integer attendanceId);

    // Get students for a session
//    @Query("SELECT n FROM NguoiDung n WHERE n.lopSinhVien IN (SELECT s.lopSinhVien FROM NguoiDung s WHERE s.diemDanhs IN (SELECT d FROM DiemDanh d WHERE d.buoiHoc.maBuoiHoc = :sessionId))")
//    List<NguoiDung> findStudentsBySession(@Param("sessionId") Integer sessionId);

    // Get classes taught by teacher
//    @Query("SELECT DISTINCT l FROM LopSinhVien l " +
//            "JOIN NguoiDung n ON n.lopSinhVien = l " +
//            "JOIN DiemDanh d ON d.nguoiDung = n " +
//            "JOIN BuoiHoc b ON d.buoiHoc = b " +
//            "WHERE EXISTS (SELECT 1 FROM DiemDanh d2 " +
//            "JOIN d2.buoiHoc b2 WHERE b2.monHoc = b.monHoc " +
//            "AND d2.nguoiDung.taiKhoan.role = 'teacher' " +
//            "AND d2.nguoiDung.maNguoiDung = :teacherId)")
//    List<LopSinhVien> findTeacherClasses(@Param("teacherId") String teacherId);

    // Get students by class
    @Query("SELECT n FROM NguoiDung n WHERE n.lopSinhVien.maLop = :classId AND n.taiKhoan.role = 'student'")
    List<NguoiDung> findStudentsByClass(@Param("classId") String classId);
}