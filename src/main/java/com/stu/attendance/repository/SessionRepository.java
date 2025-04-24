package com.stu.attendance.repository;

import com.stu.attendance.entity.BuoiHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<BuoiHoc, Integer> {
    @Query("SELECT b FROM BuoiHoc b WHERE (:startDate IS NULL OR b.ngayHoc >= :startDate) AND (:endDate IS NULL OR b.ngayHoc <= :endDate) AND (:subjectId IS NULL OR b.monHoc.maMonHoc = :subjectId) AND (:roomId IS NULL OR b.phong.maPhong = :roomId)")
    List<BuoiHoc> findAllSessions(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("subjectId") String subjectId,
            @Param("roomId") String roomId);
    @Query("SELECT b FROM BuoiHoc b LEFT JOIN FETCH b.diemDanhs WHERE b.gvId = :gvId")
    List<BuoiHoc> findAllSessions(@Param("gvId") String gvId);

    List<BuoiHoc> findByGvId(String gvId);
    BuoiHoc findByMaThamGia(String maThamGia);
    @Query("SELECT ntg.buoiHoc FROM NguoiThamGia ntg WHERE ntg.nguoiDung.id = :nguoiDungId")
    List<BuoiHoc> findAllBuoiHocByNguoiDungId(@Param("nguoiDungId") String nguoiDungId);

}