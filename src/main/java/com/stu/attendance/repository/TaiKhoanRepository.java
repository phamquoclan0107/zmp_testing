package com.stu.attendance.repository;

import com.stu.attendance.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, String> {
    // Find account by username
    TaiKhoan findByTenTaiKhoan(String tenTaiKhoan);

    // Check if account exists by username
    boolean existsByTenTaiKhoan(String tenTaiKhoan);
}