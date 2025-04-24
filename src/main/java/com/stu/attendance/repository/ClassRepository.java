package com.stu.attendance.repository;

import com.stu.attendance.entity.LopSinhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<LopSinhVien, String> {
    @Query("SELECT l FROM LopSinhVien l WHERE (:keyword IS NULL OR l.tenLop LIKE %:keyword% OR l.maLop LIKE %:keyword%)")
    List<LopSinhVien> findAllClasses(@Param("keyword") String keyword);

    boolean existsByMaLop(String maLop);
}