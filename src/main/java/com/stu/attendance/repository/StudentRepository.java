package com.stu.attendance.repository;

import com.stu.attendance.entity.BuoiHoc;
import com.stu.attendance.entity.DiemDanh;
import com.stu.attendance.entity.MonHoc;
import com.stu.attendance.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<NguoiDung, String> {

    // Get sessions for today
//    @Query("SELECT b FROM BuoiHoc b " +
//            "WHERE b.ngayHoc = CURRENT_DATE " +
//            "AND EXISTS (SELECT 1 FROM NguoiDung n " +
//            "JOIN NguoiDung s ON s.lopSinhVien = n.lopSinhVien " +
//            "JOIN DiemDanh d ON d.nguoiDung = s " +
//            "WHERE n.maNguoiDung = :studentId AND d.buoiHoc = b)")
//    List<BuoiHoc> findTodaySessions(@Param("studentId") String studentId);

    // Get upcoming sessions
//    @Query("SELECT b FROM BuoiHoc b " +
//            "WHERE b.ngayHoc > CURRENT_DATE AND b.ngayHoc <= :endDate " +
//            "AND EXISTS (SELECT 1 FROM NguoiDung n " +
//            "JOIN NguoiDung s ON s.lopSinhVien = n.lopSinhVien " +
//            "JOIN DiemDanh d ON d.nguoiDung = s " +
//            "WHERE n.maNguoiDung = :studentId AND d.buoiHoc = b)")
//    List<BuoiHoc> findUpcomingSessions(@Param("studentId") String studentId, @Param("endDate") LocalDate endDate);

    // Get student's attendance
    @Query("SELECT d FROM DiemDanh d WHERE d.nguoiDung.maNguoiDung = :studentId AND (:startDate IS NULL OR d.buoiHoc.ngayHoc >= :startDate) AND (:endDate IS NULL OR d.buoiHoc.ngayHoc <= :endDate) AND (:subjectId IS NULL OR d.buoiHoc.monHoc.maMonHoc = :subjectId)")
    List<DiemDanh> findStudentAttendance(
            @Param("studentId") String studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("subjectId") String subjectId);

    // Get today's attendance
    @Query("SELECT d FROM DiemDanh d WHERE d.nguoiDung.maNguoiDung = :studentId AND d.buoiHoc.ngayHoc = CURRENT_DATE")
    List<DiemDanh> findTodayAttendance(@Param("studentId") String studentId);

    // Get subjects of a student
    @Query("SELECT DISTINCT m FROM MonHoc m WHERE m.maMonHoc IN (SELECT b.monHoc.maMonHoc FROM BuoiHoc b WHERE b IN (SELECT d.buoiHoc FROM DiemDanh d WHERE d.nguoiDung.maNguoiDung = :studentId))")
    List<MonHoc> findStudentSubjects(@Param("studentId") String studentId);

    // Get sessions for a specific subject
    @Query("SELECT b FROM BuoiHoc b WHERE b.monHoc.maMonHoc = :subjectId AND (:startDate IS NULL OR b.ngayHoc >= :startDate) AND (:endDate IS NULL OR b.ngayHoc <= :endDate) AND EXISTS (SELECT 1 FROM DiemDanh d WHERE d.buoiHoc = b AND d.nguoiDung.maNguoiDung = :studentId)")
    List<BuoiHoc> findSubjectSessions(
            @Param("studentId") String studentId,
            @Param("subjectId") String subjectId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}