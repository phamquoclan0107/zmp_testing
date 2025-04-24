package com.stu.attendance.exception;  // Thay đổi package cho phù hợp với dự án của bạn

public class ResourceAlreadyExistsException extends RuntimeException {

    // Constructor chỉ với message
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    // Constructor với message và cause (nguyên nhân lỗi)
    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
