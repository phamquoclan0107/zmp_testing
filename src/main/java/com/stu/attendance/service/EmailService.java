package com.stu.attendance.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * Gửi email văn bản đơn giản
     */
    @Async
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Đã gửi email đến: {}", to);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    /**
     * Gửi email HTML
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email HTML đến: {}", to);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email HTML: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email HTML: " + e.getMessage());
        }
    }

    /**
     * Gửi email khôi phục mật khẩu
     */
    public void sendPasswordResetEmail(String to, String fullName, String temporaryPassword) {
        String subject = "Khôi phục mật khẩu - Hệ thống điểm danh";
        String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px;'>" +
                "<h2>Khôi phục mật khẩu</h2>" +
                "<p>Xin chào <strong>" + fullName + "</strong>,</p>" +
                "<p>Mật khẩu tạm thời của bạn là: <strong>" + temporaryPassword + "</strong></p>" +
                "<p>Vui lòng đăng nhập và đổi mật khẩu ngay sau khi nhận được email này.</p>" +
                "<p>Trân trọng,<br>Hệ thống điểm danh STU</p>" +
                "</div>";

        sendHtmlEmail(to, subject, htmlContent);
    }
}