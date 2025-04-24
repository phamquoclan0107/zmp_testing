package com.stu.attendance.controller;

import com.stu.attendance.dto.*;
import com.stu.attendance.entity.NguoiDung;
import com.stu.attendance.entity.TaiKhoan;
import com.stu.attendance.repository.NguoiThamGiaRepository;
import com.stu.attendance.service.AdminService;
import com.stu.attendance.service.NguoiThamGiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")//quan tri nguoi dung
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final NguoiThamGiaService nguoiThamGiaService;

    // User Management
    @GetMapping("/users")//lay danh sach nguoi dung
    public ResponseEntity<List<UserInfoDto>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaiKhoan.Role role) {
        return ResponseEntity.ok(adminService.getAllUsers(keyword, role));
    }

    @PostMapping("/users")//Tạo nguoi dung
    public ResponseEntity<UserInfoDto> createUser(@Valid @RequestBody UserCreationDto userDto) {
        NguoiDung createdUser = adminService.createUser(userDto);
        return new ResponseEntity<>(convertToUserInfoDto(createdUser), HttpStatus.CREATED);
    }

    @PutMapping("/users/{userId}")//update nguoi dung
    public ResponseEntity<UserInfoDto> updateUser(@PathVariable String userId, @Valid @RequestBody UserInfoDto userInfoDto) {
        return ResponseEntity.ok(adminService.updateUser(userId, userInfoDto));
    }

    @DeleteMapping("/users/{userId}")//xoa nguoi dung
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/users/import")
//    public ResponseEntity<Map<String, Object>> importUsers(@RequestParam("file") MultipartFile file) {
//        return ResponseEntity.ok(adminService.importUsersFromExcel(file));
//    }

    // Class Management
    @GetMapping("/classes")//lay danh sach lop
    public ResponseEntity<List<Map<String, Object>>> getAllClasses(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(adminService.getAllClasses(keyword));
    }

    @PostMapping("/classes")//them lop
    public ResponseEntity<Map<String, Object>> createClass(@RequestBody Map<String, String> classInfo) {
        return ResponseEntity.ok(adminService.createClass(classInfo));
    }

    @PutMapping("/classes/{classId}")//update lop
    public ResponseEntity<Map<String, Object>> updateClass(@PathVariable String classId, @RequestBody Map<String, String> classInfo) {
        return ResponseEntity.ok(adminService.updateClass(classId, classInfo));
    }

    @DeleteMapping("/classes/{classId}")//xoa lop
    public ResponseEntity<Void> deleteClass(@PathVariable String classId) {
        adminService.deleteClass(classId);
        return ResponseEntity.noContent().build();
    }

    // Subject Management
    @GetMapping("/subjects")//lay danh sach mon
    public ResponseEntity<List<Map<String, Object>>> getAllSubjects(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(adminService.getAllSubjects(keyword));
    }

    @PostMapping("/subjects")//them mon
    public ResponseEntity<Map<String, Object>> createSubject(@RequestBody Map<String, String> subjectInfo) {
        return ResponseEntity.ok(adminService.createSubject(subjectInfo));
    }

    @PutMapping("/subjects/{subjectId}")//update mon
    public ResponseEntity<Map<String, Object>> updateSubject(@PathVariable String subjectId, @RequestBody Map<String, String> subjectInfo) {
        return ResponseEntity.ok(adminService.updateSubject(subjectId, subjectInfo));
    }

    @DeleteMapping("/subjects/{subjectId}")//xoa mon
    public ResponseEntity<Void> deleteSubject(@PathVariable String subjectId) {
        adminService.deleteSubject(subjectId);
        return ResponseEntity.noContent().build();
    }

    // Room Management
    @GetMapping("/rooms")//lay danh sach phong
    public ResponseEntity<List<Map<String, Object>>> getAllRooms(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(adminService.getAllRooms(keyword));
    }

    @PostMapping("/rooms")//them phong
    public ResponseEntity<Map<String, Object>> createRoom(@RequestBody Map<String, String> roomInfo) {
        return ResponseEntity.ok(adminService.createRoom(roomInfo));
    }

    @PutMapping("/rooms/{roomId}")//update phong
    public ResponseEntity<Map<String, Object>> updateRoom(@PathVariable String roomId, @RequestBody Map<String, String> roomInfo) {
        return ResponseEntity.ok(adminService.updateRoom(roomId, roomInfo));
    }

    @DeleteMapping("/rooms/{roomId}")//xoa phong
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
        adminService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    // Session Management
    @GetMapping("/sessions")//lay danh sach buoi
    public ResponseEntity<List<SessionDto>> getAllSessions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String roomId) {
        return ResponseEntity.ok(adminService.getAllSessions(startDate, endDate, subjectId, roomId));
    }
    @GetMapping("/sessions/get")//lay danh sach buoi
    public ResponseEntity<List<SessionDto>> getSSByGV(@RequestParam(required = false) String gvId){
        System.out.println(gvId);
        return ResponseEntity.ok(adminService.getAllSessions(gvId));

    }

    @GetMapping("/session/getid")//lay danh sach buoi
    public ResponseEntity<SessionDto> getSSByID(@RequestParam(required = false) Integer id){
        return ResponseEntity.ok(adminService.getById(id));
    }


    @PostMapping("/sessions")//them buoi
    public ResponseEntity<SessionDto> createSession(@Valid @RequestBody SessionDto sessionDto) {
        return ResponseEntity.ok(adminService.createSession(sessionDto));
    }

    @PutMapping("/sessions/{sessionId}")//update buoi
    public ResponseEntity<SessionDto> updateSession(@PathVariable Integer sessionId, @Valid @RequestBody SessionDto sessionDto) {
        return ResponseEntity.ok(adminService.updateSession(sessionId, sessionDto));
    }

    @DeleteMapping("/sessions/{sessionId}")//xoa buoi
    public ResponseEntity<Void> deleteSession(@PathVariable Integer sessionId) {
        adminService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }
//    @GetMapping("/session/student")
//    public ResponseEntity<List<SessionDto>> getSSByID(@RequestParam(required = false) Integer id){
//        return null;
//    }
@PostMapping("/student/add")//them buoi cho sinh vien
public void addStudent(@RequestBody NguoiThamGiaDTO request){
    nguoiThamGiaService.addStudent(request.getUserId(), request.getMaThamGia());
}

    @GetMapping("/sessions/student")//lay danh sach buoi theo sinh vien
    public ResponseEntity<List<SessionDto>> getSSByStudent(@RequestParam(required = false) String id){
        return ResponseEntity.ok(adminService.getSSByUser(id));
    }
    @GetMapping("/session/sinh-vien")//lay danh sach sinh vien theo buoi
    public ResponseEntity<List<UserInfoDto>> getStudentBySS(@RequestParam(required = false) Integer id){
        return ResponseEntity.ok(adminService.getStudentBySS(id));
    }
    /**
     * Convert NguoiDung entity to UserInfoDto
     */
    private UserInfoDto convertToUserInfoDto(NguoiDung nguoiDung) {
        UserInfoDto dto = new UserInfoDto();
        dto.setUserId(nguoiDung.getMaNguoiDung());
        dto.setFullName(nguoiDung.getTenNguoiDung());
        dto.setEmail(nguoiDung.getEmail());
        dto.setPhone(nguoiDung.getSdt());

        if (nguoiDung.getLopSinhVien() != null) {
            dto.setClassId(nguoiDung.getLopSinhVien().getMaLop());
            dto.setClassName(nguoiDung.getLopSinhVien().getTenLop());
        }

        if (nguoiDung.getTaiKhoan() != null) {
            dto.setRole(nguoiDung.getTaiKhoan().getRole());
            dto.setUsername(nguoiDung.getTaiKhoan().getTenTaiKhoan());
        }

        return dto;
    }
}