package com.stu.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrCodeRequest {

    @NotNull(message = "Mã buổi học không được để trống")
    private Integer sessionId;//mã buổi học

    private Integer validityMinutes = 5;//mặc định là 5

    private Integer size = 250;//mặc định là 250
}
