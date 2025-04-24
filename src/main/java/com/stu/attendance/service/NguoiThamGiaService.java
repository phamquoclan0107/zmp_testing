package com.stu.attendance.service;

import com.stu.attendance.entity.BuoiHoc;
import com.stu.attendance.entity.NguoiDung;
import com.stu.attendance.entity.NguoiThamGia;
import com.stu.attendance.repository.NguoiThamGiaRepository;
import com.stu.attendance.repository.SessionRepository;
import com.stu.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NguoiThamGiaService {
    private final NguoiThamGiaRepository nguoiThamGiaRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public void addStudent(String userId, String maThamGia){
        NguoiThamGia nguoiThamGia = new NguoiThamGia();
        NguoiDung nguoiDung = userRepository.findByMaNguoiDung(userId).get();
        BuoiHoc buoiHoc = sessionRepository.findByMaThamGia(maThamGia);
        if(!userRepository.findByMaNguoiDung(userId).isPresent() || buoiHoc == null){
            return;
        }
        nguoiThamGia.setNguoiDung(nguoiDung);
        nguoiThamGia.setBuoiHoc(buoiHoc);
        nguoiThamGiaRepository.save(nguoiThamGia);
    }
}
