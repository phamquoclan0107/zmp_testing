package com.stu.attendance.repository;

import com.stu.attendance.entity.Phong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Phong, String> {
    @Query("SELECT p FROM Phong p WHERE (:keyword IS NULL OR p.tenPhong LIKE %:keyword% OR p.maPhong LIKE %:keyword%)")
    List<Phong> findAllRooms(@Param("keyword") String keyword);

    boolean existsByMaPhong(String maPhong);
}