package com.stu.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "nguoi_dung")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDung {

    @Id
    @Column(name = "ma_nguoi_dung")
    private String maNguoiDung;

    @Column(name = "ten_nguoi_dung", nullable = false)
    private String tenNguoiDung;

    @ManyToOne
    @JoinColumn(name = "ma_lop")
    private LopSinhVien lopSinhVien;

    @Column(name = "sdt")
    private String sdt;

    @Column(name = "email", unique = true)
    private String email;

    @OneToOne
    @JoinColumn(name = "ma_tai_khoan", unique = true)
    private TaiKhoan taiKhoan;

    @OneToMany(mappedBy = "nguoiDung")
    private List<DiemDanh> diemDanhs;
}