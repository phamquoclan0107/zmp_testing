package com.stu.attendance.service;

import com.stu.attendance.dto.SessionDto;
import com.stu.attendance.dto.UserCreationDto;
import com.stu.attendance.dto.UserInfoDto;
import com.stu.attendance.entity.*;
import com.stu.attendance.exception.ResourceAlreadyExistsException;
import com.stu.attendance.exception.ResourceNotFoundException;
import com.stu.attendance.repository.*;
import jakarta.mail.Session;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    // Thay thế AdminRepository bằng các repository riêng cho từng entity
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final RoomRepository roomRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaiKhoanRepository taiKhoanRepository;

    // User Management
    public List<UserInfoDto> getAllUsers(String keyword, TaiKhoan.Role role) {
        List<NguoiDung> users = userRepository.findAllUsers(keyword, role);
        return users.stream()
                .map(this::convertToUserInfoDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserInfoDto createUser(UserInfoDto userInfoDto) {
        // Validate user doesn't exist already
        if (userRepository.existsByMaNguoiDung(userInfoDto.getUserId())) {
            throw new IllegalArgumentException("Mã người dùng đã tồn tại");
        }
        if (userRepository.existsByEmail(userInfoDto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        // Create account
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setMaTaiKhoan(UUID.randomUUID().toString());
        taiKhoan.setTenTaiKhoan(userInfoDto.getUsername());
        // Default password is the user ID, or a specific default
        String defaultPassword = userInfoDto.getUserId();
        taiKhoan.setMatKhau(passwordEncoder.encode(defaultPassword));
        taiKhoan.setRole(userInfoDto.getRole());

        // Create user
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setMaNguoiDung(userInfoDto.getUserId());
        nguoiDung.setTenNguoiDung(userInfoDto.getFullName());
        nguoiDung.setEmail(userInfoDto.getEmail());
        nguoiDung.setSdt(userInfoDto.getPhone());

        // Set class if provided
        if (userInfoDto.getClassId() != null && !userInfoDto.getClassId().isEmpty()) {
            // Tìm lớp thực tế từ database
            LopSinhVien lopSinhVien = classRepository.findById(userInfoDto.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lớp"));
            nguoiDung.setLopSinhVien(lopSinhVien);
        }

        nguoiDung.setTaiKhoan(taiKhoan);

        NguoiDung savedUser = userRepository.save(nguoiDung);
        return convertToUserInfoDto(savedUser);
    }

    @Transactional
    public UserInfoDto updateUser(String userId, UserInfoDto userInfoDto) {
        NguoiDung existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        // Check email uniqueness if changed
        if (!existingUser.getEmail().equals(userInfoDto.getEmail()) &&
                userRepository.existsByEmail(userInfoDto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        // Update user info
        existingUser.setTenNguoiDung(userInfoDto.getFullName());
        existingUser.setEmail(userInfoDto.getEmail());
        existingUser.setSdt(userInfoDto.getPhone());

        // Update class if provided
        if (userInfoDto.getClassId() != null && !userInfoDto.getClassId().isEmpty()) {
            // Tìm lớp thực tế từ database
            LopSinhVien lopSinhVien = classRepository.findById(userInfoDto.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lớp"));
            existingUser.setLopSinhVien(lopSinhVien);
        } else {
            existingUser.setLopSinhVien(null);
        }

        // Update account role if needed
        if (userInfoDto.getRole() != null && existingUser.getTaiKhoan() != null) {
            existingUser.getTaiKhoan().setRole(userInfoDto.getRole());
        }

        NguoiDung updatedUser = userRepository.save(existingUser);
        return convertToUserInfoDto(updatedUser);
    }

    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Không tìm thấy người dùng");
        }
        userRepository.deleteById(userId);
    }

    // Class Management
    public List<Map<String, Object>> getAllClasses(String keyword) {
        List<LopSinhVien> classes = classRepository.findAllClasses(keyword);
        return classes.stream()
                .map(c -> {
                    Map<String, Object> classMap = new HashMap<>();
                    classMap.put("classId", c.getMaLop());
                    classMap.put("className", c.getTenLop());
                    return classMap;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> createClass(Map<String, String> classInfo) {
        String classId = classInfo.get("classId");
        String className = classInfo.get("className");

        if (classId == null || classId.isEmpty()) {
            throw new IllegalArgumentException("Mã lớp không được để trống");
        }
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("Tên lớp không được để trống");
        }

        if (classRepository.existsByMaLop(classId)) {
            throw new IllegalArgumentException("Mã lớp đã tồn tại");
        }

        LopSinhVien lopSinhVien = new LopSinhVien();
        lopSinhVien.setMaLop(classId);
        lopSinhVien.setTenLop(className);

        LopSinhVien savedClass = classRepository.save(lopSinhVien);

        Map<String, Object> result = new HashMap<>();
        result.put("classId", savedClass.getMaLop());
        result.put("className", savedClass.getTenLop());
        return result;
    }

    @Transactional
    public Map<String, Object> updateClass(String classId, Map<String, String> classInfo) {
        LopSinhVien existingClass = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lớp"));

        String className = classInfo.get("className");
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("Tên lớp không được để trống");
        }

        existingClass.setTenLop(className);
        LopSinhVien updatedClass = classRepository.save(existingClass);

        Map<String, Object> result = new HashMap<>();
        result.put("classId", updatedClass.getMaLop());
        result.put("className", updatedClass.getTenLop());
        return result;
    }

    @Transactional
    public void deleteClass(String classId) {
        if (!classRepository.existsById(classId)) {
            throw new EntityNotFoundException("Không tìm thấy lớp");
        }
        classRepository.deleteById(classId);
    }

    // Subject Management
    public List<Map<String, Object>> getAllSubjects(String keyword) {
        List<MonHoc> subjects = subjectRepository.findAllSubjects(keyword);
        return subjects.stream()
                .map(s -> {
                    Map<String, Object> subjectMap = new HashMap<>();
                    subjectMap.put("subjectId", s.getMaMonHoc());
                    subjectMap.put("subjectName", s.getTenMonHoc());
                    return subjectMap;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> createSubject(Map<String, String> subjectInfo) {
        String subjectId = subjectInfo.get("subjectId");
        String subjectName = subjectInfo.get("subjectName");

        if (subjectId == null || subjectId.isEmpty()) {
            throw new IllegalArgumentException("Mã môn học không được để trống");
        }
        if (subjectName == null || subjectName.isEmpty()) {
            throw new IllegalArgumentException("Tên môn học không được để trống");
        }

        if (subjectRepository.existsByMaMonHoc(subjectId)) {
            throw new IllegalArgumentException("Mã môn học đã tồn tại");
        }

        MonHoc monHoc = new MonHoc();
        monHoc.setMaMonHoc(subjectId);
        monHoc.setTenMonHoc(subjectName);

        MonHoc savedSubject = subjectRepository.save(monHoc);

        Map<String, Object> result = new HashMap<>();
        result.put("subjectId", savedSubject.getMaMonHoc());
        result.put("subjectName", savedSubject.getTenMonHoc());
        return result;
    }

    @Transactional
    public Map<String, Object> updateSubject(String subjectId, Map<String, String> subjectInfo) {
        MonHoc existingSubject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy môn học"));

        String subjectName = subjectInfo.get("subjectName");
        if (subjectName == null || subjectName.isEmpty()) {
            throw new IllegalArgumentException("Tên môn học không được để trống");
        }

        existingSubject.setTenMonHoc(subjectName);
        MonHoc updatedSubject = subjectRepository.save(existingSubject);

        Map<String, Object> result = new HashMap<>();
        result.put("subjectId", updatedSubject.getMaMonHoc());
        result.put("subjectName", updatedSubject.getTenMonHoc());
        return result;
    }

    @Transactional
    public void deleteSubject(String subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new EntityNotFoundException("Không tìm thấy môn học");
        }
        subjectRepository.deleteById(subjectId);
    }

    // Room Management
    public List<Map<String, Object>> getAllRooms(String keyword) {
        List<Phong> rooms = roomRepository.findAllRooms(keyword);
        return rooms.stream()
                .map(r -> {
                    Map<String, Object> roomMap = new HashMap<>();
                    roomMap.put("roomId", r.getMaPhong());
                    roomMap.put("roomName", r.getTenPhong());
                    return roomMap;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> createRoom(Map<String, String> roomInfo) {
        String roomId = roomInfo.get("roomId");
        String roomName = roomInfo.get("roomName");

        if (roomId == null || roomId.isEmpty()) {
            throw new IllegalArgumentException("Mã phòng không được để trống");
        }
        if (roomName == null || roomName.isEmpty()) {
            throw new IllegalArgumentException("Tên phòng không được để trống");
        }

        if (roomRepository.existsByMaPhong(roomId)) {
            throw new IllegalArgumentException("Mã phòng đã tồn tại");
        }

        Phong phong = new Phong();
        phong.setMaPhong(roomId);
        phong.setTenPhong(roomName);

        Phong savedRoom = roomRepository.save(phong);

        Map<String, Object> result = new HashMap<>();
        result.put("roomId", savedRoom.getMaPhong());
        result.put("roomName", savedRoom.getTenPhong());
        return result;
    }

    @Transactional
    public Map<String, Object> updateRoom(String roomId, Map<String, String> roomInfo) {
        Phong existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phòng"));

        String roomName = roomInfo.get("roomName");
        if (roomName == null || roomName.isEmpty()) {
            throw new IllegalArgumentException("Tên phòng không được để trống");
        }

        existingRoom.setTenPhong(roomName);
        Phong updatedRoom = roomRepository.save(existingRoom);

        Map<String, Object> result = new HashMap<>();
        result.put("roomId", updatedRoom.getMaPhong());
        result.put("roomName", updatedRoom.getTenPhong());
        return result;
    }

    @Transactional
    public void deleteRoom(String roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new EntityNotFoundException("Không tìm thấy phòng");
        }
        roomRepository.deleteById(roomId);
    }

    // Session Management
    public List<SessionDto> getAllSessions(LocalDate startDate, LocalDate endDate, String subjectId, String roomId) {
        List<BuoiHoc> sessions = sessionRepository.findAllSessions(startDate, endDate, subjectId, roomId);
        return sessions.stream()
                .map(this::convertToSessionDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SessionDto createSession(SessionDto sessionDto) {
        // Validate inputs
        if (sessionDto.getSubjectId() == null || sessionDto.getSubjectId().isEmpty()) {
            throw new IllegalArgumentException("Mã môn học không được để trống");
        }
        if (sessionDto.getRoomId() == null || sessionDto.getRoomId().isEmpty()) {
            throw new IllegalArgumentException("Mã phòng không được để trống");
        }
        if (sessionDto.getDate() == null) {
            throw new IllegalArgumentException("Ngày học không được để trống");
        }
        if (sessionDto.getStartPeriod() == null || sessionDto.getEndPeriod() == null) {
            throw new IllegalArgumentException("Tiết học không được để trống");
        }
        if (sessionDto.getStartPeriod() > sessionDto.getEndPeriod()) {
            throw new IllegalArgumentException("Tiết bắt đầu phải nhỏ hơn hoặc bằng tiết kết thúc");
        }

        // Create new session
        BuoiHoc buoiHoc = new BuoiHoc();

        // Tìm môn học từ repository
        MonHoc monHoc = subjectRepository.findById(sessionDto.getSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy môn học"));
        buoiHoc.setMonHoc(monHoc);

        // Tìm phòng từ repository
        Phong phong = roomRepository.findById(sessionDto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phòng"));
        buoiHoc.setPhong(phong);

        buoiHoc.setNgayHoc(sessionDto.getDate());
        buoiHoc.setTietBatDau(sessionDto.getStartPeriod());
        buoiHoc.setTietKetThuc(sessionDto.getEndPeriod());
        String maThamGia = UUID.randomUUID().toString().replace("-", "").substring(0, 10); // 10 ký tự
        buoiHoc.setMaThamGia(maThamGia);
        buoiHoc.setGvId(sessionDto.getGvId());
        BuoiHoc savedSession = sessionRepository.save(buoiHoc);

        return convertToSessionDto(savedSession);
    }

    @Transactional
    public SessionDto updateSession(Integer sessionId, SessionDto sessionDto) {
        // Check if session exists
        BuoiHoc existingSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy buổi học"));

        // Validate inputs
        if (sessionDto.getSubjectId() == null || sessionDto.getSubjectId().isEmpty()) {
            throw new IllegalArgumentException("Mã môn học không được để trống");
        }
        if (sessionDto.getRoomId() == null || sessionDto.getRoomId().isEmpty()) {
            throw new IllegalArgumentException("Mã phòng không được để trống");
        }
        if (sessionDto.getDate() == null) {
            throw new IllegalArgumentException("Ngày học không được để trống");
        }
        if (sessionDto.getStartPeriod() == null || sessionDto.getEndPeriod() == null) {
            throw new IllegalArgumentException("Tiết học không được để trống");
        }
        if (sessionDto.getStartPeriod() > sessionDto.getEndPeriod()) {
            throw new IllegalArgumentException("Tiết bắt đầu phải nhỏ hơn hoặc bằng tiết kết thúc");
        }

        // Update session
        // Tìm môn học từ repository
        MonHoc monHoc = subjectRepository.findById(sessionDto.getSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy môn học"));
        existingSession.setMonHoc(monHoc);

        // Tìm phòng từ repository
        Phong phong = roomRepository.findById(sessionDto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phòng"));
        existingSession.setPhong(phong);

        existingSession.setNgayHoc(sessionDto.getDate());
        existingSession.setTietBatDau(sessionDto.getStartPeriod());
        existingSession.setTietKetThuc(sessionDto.getEndPeriod());

        BuoiHoc updatedSession = sessionRepository.save(existingSession);

        return convertToSessionDto(updatedSession);
    }

    @Transactional
    public void deleteSession(Integer sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new EntityNotFoundException("Không tìm thấy buổi học");
        }
        sessionRepository.deleteById(sessionId);
    }

    // Helper methods
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

    private SessionDto convertToSessionDto(BuoiHoc buoiHoc) {
        SessionDto dto = new SessionDto();
        dto.setSessionId(buoiHoc.getMaBuoiHoc());

        if (buoiHoc.getMonHoc() != null) {
            dto.setSubjectId(buoiHoc.getMonHoc().getMaMonHoc());
            dto.setSubjectName(buoiHoc.getMonHoc().getTenMonHoc());
        }

        if (buoiHoc.getPhong() != null) {
            dto.setRoomId(buoiHoc.getPhong().getMaPhong());
            dto.setRoomName(buoiHoc.getPhong().getTenPhong());
        }

        dto.setDate(buoiHoc.getNgayHoc());
        dto.setStartPeriod(buoiHoc.getTietBatDau());
        dto.setEndPeriod(buoiHoc.getTietKetThuc());

        // Calculate attendance statistics
        if (buoiHoc.getDiemDanhs() != null && !buoiHoc.getDiemDanhs().isEmpty()) {
            int present = 0, absent = 0, late = 0;

            for (DiemDanh diemDanh : buoiHoc.getDiemDanhs()) {
                if (diemDanh.getTrangThai() == DiemDanh.TrangThai.present) {
                    present++;
                } else if (diemDanh.getTrangThai() == DiemDanh.TrangThai.absent) {
                    absent++;
                } else if (diemDanh.getTrangThai() == DiemDanh.TrangThai.late) {
                    late++;
                }
            }

            dto.setTotalStudents(buoiHoc.getDiemDanhs().size());
            dto.setPresentCount(present);
            dto.setAbsentCount(absent);
            dto.setLateCount(late);
        }

        return dto;
    }
    @Transactional
    public List<SessionDto> getAllSessions(String gvId){
        List<BuoiHoc> sessions =  sessionRepository.findByGvId(gvId);
        List<SessionDto>sessionDtos = new ArrayList<>();
        for (BuoiHoc s : sessions){
            SessionDto t = new SessionDto();
            t.setSessionId(s.getMaBuoiHoc());
            t.setDate(s.getNgayHoc());
            t.setEndPeriod(s.getTietKetThuc());
            t.setGvId(s.getGvId());
            t.setMaThamGia(s.getMaThamGia());
            t.setRoomId(s.getPhong().getMaPhong());
            t.setRoomName(s.getPhong().getTenPhong());
            t.setStartPeriod(s.getTietBatDau());
            t.setSubjectId((s.getMonHoc().getMaMonHoc()));
            t.setSubjectName(s.getMonHoc().getTenMonHoc());
            sessionDtos.add(t);
        }
        return sessionDtos;
    }
    public SessionDto getById(Integer id){
        Optional<BuoiHoc> buoiHoc = sessionRepository.findById(id);
        if(buoiHoc.isPresent()){
            BuoiHoc s = buoiHoc.get();
            SessionDto t = new SessionDto();
            t.setSessionId(s.getMaBuoiHoc());
            t.setDate(s.getNgayHoc());
            t.setEndPeriod(s.getTietKetThuc());
            t.setGvId(s.getGvId());
            t.setMaThamGia(s.getMaThamGia());
            t.setRoomId(s.getPhong().getMaPhong());
            t.setRoomName(s.getPhong().getTenPhong());
            t.setStartPeriod(s.getTietBatDau());
            t.setSubjectId((s.getMonHoc().getMaMonHoc()));
            t.setSubjectName(s.getMonHoc().getTenMonHoc());
            return t;
        }
        return null;
    }
    public List<SessionDto> getSSByUser(String id){
        List<BuoiHoc>ds =  sessionRepository.findAllBuoiHocByNguoiDungId(id);
        List<SessionDto>sessionDtos = new ArrayList<>();
        for (BuoiHoc s : ds){
            SessionDto t = new SessionDto();
            t.setSessionId(s.getMaBuoiHoc());
            t.setDate(s.getNgayHoc());
            t.setEndPeriod(s.getTietKetThuc());
            t.setGvId(s.getGvId());
            t.setMaThamGia(s.getMaThamGia());
            t.setRoomId(s.getPhong().getMaPhong());
            t.setRoomName(s.getPhong().getTenPhong());
            t.setStartPeriod(s.getTietBatDau());
            t.setSubjectId((s.getMonHoc().getMaMonHoc()));
            t.setSubjectName(s.getMonHoc().getTenMonHoc());
            sessionDtos.add(t);
        }
        return sessionDtos;
    }
    public List<UserInfoDto> getStudentBySS(Integer id){

        List<NguoiDung> ds = userRepository.findNguoiDungByBuoiHocId(id);
        List<UserInfoDto> ds2 = new ArrayList<>();
        for (NguoiDung n : ds){
            UserInfoDto dt = new UserInfoDto();
            dt.setUserId(n.getMaNguoiDung());
            dt.setPhone(n.getSdt());
            dt.setClassId(n.getLopSinhVien().getMaLop());
            dt.setClassName(n.getLopSinhVien().getTenLop());
            ds2.add(dt);
        }
        return ds2;
    }



    /**
     * Creates a new user with associated account
     *
     * @param userDto the user creation data
     * @return the created user
     */
    @Transactional
    public NguoiDung createUser(UserCreationDto userDto) {
        // Check if user already exists
        if (userRepository.existsById(userDto.getMaNguoiDung())) {
            throw new ResourceAlreadyExistsException("Người dùng với mã " + userDto.getMaNguoiDung() + " đã tồn tại");
        }

        // Check if account already exists
        if (taiKhoanRepository.existsById(userDto.getMaNguoiDung())) {
            throw new ResourceAlreadyExistsException("Tài khoản với mã " + userDto.getMaNguoiDung() + " đã tồn tại");
        }

        // Create and save the account first
        TaiKhoan taiKhoan = userDto.toTaiKhoan();
        taiKhoan.setMatKhau(passwordEncoder.encode(userDto.getMaNguoiDung())); // Encrypt password
        taiKhoan = taiKhoanRepository.save(taiKhoan);

        // Create and save the user
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setMaNguoiDung(userDto.getMaNguoiDung());
        nguoiDung.setTenNguoiDung(userDto.getTenNguoiDung());
        nguoiDung.setSdt(userDto.getSdt());
        nguoiDung.setEmail(userDto.getEmail());
        nguoiDung.setTaiKhoan(taiKhoan);

        // Set class if provided
        if (userDto.getMaLop() != null && !userDto.getMaLop().isBlank()) {
            LopSinhVien lopSinhVien = classRepository.findById(userDto.getMaLop())
                    .orElseThrow(() -> new ResourceNotFoundException("Lớp với mã " + userDto.getMaLop() + " không tồn tại"));
            nguoiDung.setLopSinhVien(lopSinhVien);
        }

        return userRepository.save(nguoiDung);
    }

    /**
     * Updates an existing user and their account
     */
    @Transactional
    public NguoiDung updateUser(String maNguoiDung, UserCreationDto userDto) {
        // Find existing user
        NguoiDung existingUser = userRepository.findById(maNguoiDung)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng với mã " + maNguoiDung + " không tồn tại"));

        // Update user fields
        existingUser.setTenNguoiDung(userDto.getTenNguoiDung());
        existingUser.setSdt(userDto.getSdt());
        existingUser.setEmail(userDto.getEmail());

        // Update class if provided
        if (userDto.getMaLop() != null) {
            if (userDto.getMaLop().isBlank()) {
                existingUser.setLopSinhVien(null);
            } else {
                LopSinhVien lopSinhVien = classRepository.findById(userDto.getMaLop())
                        .orElseThrow(() -> new ResourceNotFoundException("Lớp với mã " + userDto.getMaLop() + " không tồn tại"));
                existingUser.setLopSinhVien(lopSinhVien);
            }
        }

        // Update account if needed
        TaiKhoan existingAccount = existingUser.getTaiKhoan();
        if (existingAccount != null) {
            // Update role if provided
            if (userDto.getRole() != null) {
                existingAccount.setRole(userDto.getRole());
            }

            // Update password if provided
            if (userDto.getMatKhau() != null && !userDto.getMatKhau().isBlank()) {
                existingAccount.setMatKhau(passwordEncoder.encode(userDto.getMatKhau()));
            }

            taiKhoanRepository.save(existingAccount);
        }

        return userRepository.save(existingUser);
    }
}