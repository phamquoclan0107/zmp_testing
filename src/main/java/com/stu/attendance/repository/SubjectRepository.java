package com.stu.attendance.repository;

import com.stu.attendance.entity.MonHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<MonHoc, String> {
    @Query("SELECT m FROM MonHoc m WHERE (:keyword IS NULL OR m.tenMonHoc LIKE %:keyword% OR m.maMonHoc LIKE %:keyword%)")
    List<MonHoc> findAllSubjects(@Param("keyword") String keyword);

    boolean existsByMaMonHoc(String maMonHoc);
}