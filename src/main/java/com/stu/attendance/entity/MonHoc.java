package com.stu.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "mon_hoc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonHoc {

    @Id
    @Column(name = "ma_mon_hoc")
    private String maMonHoc;

    @Column(name = "ten_mon_hoc", nullable = false)
    private String tenMonHoc;

    @OneToMany(mappedBy = "monHoc")
    private List<BuoiHoc> buoiHocs;
}
