package com.stu.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "diem_danh")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiemDanh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_diem_danh")
    private Integer maDiemDanh;

    @ManyToOne
    @JoinColumn(name = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne
    @JoinColumn(name = "ma_buoi_hoc", nullable = false)
    private BuoiHoc buoiHoc;

    @Enumerated(EnumType.STRING)
    @Column(name = "phuong_thuc", nullable = false)
    private PhuongThuc phuongThuc;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThai trangThai;

    @Column(name = "thoi_gian_diem_danh")
    private LocalDateTime thoiGianDiemDanh;

    public enum PhuongThuc {
        qr_code, manual
    }

    public enum TrangThai {
        present, absent, late
    }
}