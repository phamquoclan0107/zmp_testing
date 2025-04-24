package com.stu.attendance.dto;

import com.stu.attendance.entity.TaiKhoan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {

    private String userId;//id nguoi dung
    private String fullName;//ten nguoi dung
    private String email;//email
    private String phone;//sdt
    private String className;//ten lop
    private String classId;//id lop
    private TaiKhoan.Role role;//role
    private String username;//username
}
