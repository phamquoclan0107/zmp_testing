package com.stu.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tai_khoan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaiKhoan {

    @Id
    @Column(name = "ma_tai_khoan")
    private String maTaiKhoan;

    @Column(name = "ten_tai_khoan", nullable = false)
    private String tenTaiKhoan;

    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne(mappedBy = "taiKhoan")
    private NguoiDung nguoiDung;

    public enum Role {
        admin, teacher, student
    }
}