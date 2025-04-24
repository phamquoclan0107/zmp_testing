package com.stu.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "phong")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Phong {

    @Id
    @Column(name = "ma_phong")
    private String maPhong;

    @Column(name = "ten_phong", nullable = false)
    private String tenPhong;

    @OneToMany(mappedBy = "phong")
    private List<BuoiHoc> buoiHocs;
}