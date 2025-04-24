package com.stu.attendance.repository;

import com.stu.attendance.entity.DiemDanh;
import com.stu.attendance.entity.NguoiDung;
import com.stu.attendance.entity.BuoiHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<DiemDanh, Integer> {

    // Find by session
    List<DiemDanh> findByBuoiHoc_MaBuoiHoc(Integer sessionId);

    // Find by student
    List<DiemDanh> findByNguoiDung_MaNguoiDung(String studentId);

    // Check if attendance already exists
    boolean existsByBuoiHocAndNguoiDung(BuoiHoc buoiHoc, NguoiDung nguoiDung);

    // Find specific attendance
    DiemDanh findByBuoiHocAndNguoiDung(BuoiHoc buoiHoc, NguoiDung nguoiDung);

    // Get attendance by student and date range
    @Query("SELECT d FROM DiemDanh d WHERE d.nguoiDung.maNguoiDung = :studentId AND (:startDate IS NULL OR d.buoiHoc.ngayHoc >= :startDate) AND (:endDate IS NULL OR d.buoiHoc.ngayHoc <= :endDate) AND (:subjectId IS NULL OR d.buoiHoc.monHoc.maMonHoc = :subjectId)")
    List<DiemDanh> findByStudentAndDateRange(
            @Param("studentId") String studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("subjectId") String subjectId);

    // Get attendance for a specific day
    @Query("SELECT d FROM DiemDanh d WHERE d.nguoiDung.maNguoiDung = :studentId AND d.buoiHoc.ngayHoc = :date")
    List<DiemDanh> findByStudentAndDate(@Param("studentId") String studentId, @Param("date") LocalDate date);

    // For batch operations
    @Query("SELECT d FROM DiemDanh d WHERE d.buoiHoc.maBuoiHoc = :sessionId AND d.nguoiDung.maNguoiDung IN :studentIds")
    List<DiemDanh> findBySessionAndStudentIds(@Param("sessionId") Integer sessionId, @Param("studentIds") List<String> studentIds);
}