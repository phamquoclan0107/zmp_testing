package com.stu.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "lop_sinh_vien")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LopSinhVien {

    @Id
    @Column(name = "ma_lop")
    private String maLop;

    @Column(name = "ten_lop", nullable = false)
    private String tenLop;

    @OneToMany(mappedBy = "lopSinhVien", fetch = FetchType.EAGER)
    private List<NguoiDung> nguoiDungs;
}