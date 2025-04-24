package com.stu.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nguoi_tham_gia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NguoiThamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "nguoidung_id")
    private NguoiDung nguoiDung;
    @ManyToOne
    @JoinColumn(name = "buoihoc_id")
    private BuoiHoc buoiHoc;
}
