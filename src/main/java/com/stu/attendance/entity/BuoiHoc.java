package com.stu.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "buoi_hoc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuoiHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_buoi_hoc")
    private Integer maBuoiHoc;

    @ManyToOne
    @JoinColumn(name = "ma_mon_hoc", nullable = false)
    private MonHoc monHoc;

    @ManyToOne
    @JoinColumn(name = "ma_phong", nullable = false)
    private Phong phong;

    @Column(name = "ngay_hoc", nullable = false)
    private LocalDate ngayHoc;

    @Column(name = "tiet_bat_dau", nullable = false)
    private Integer tietBatDau;

    @Column(name = "tiet_ket_thuc", nullable = false)
    private Integer tietKetThuc;

    @OneToMany(mappedBy = "buoiHoc", fetch = FetchType.EAGER)
    private List<DiemDanh> diemDanhs;

    @Column(name = "ma_tham_gia", nullable = false)
    private String maThamGia;
    @Column(name = "gv_id")
    private String gvId;
}