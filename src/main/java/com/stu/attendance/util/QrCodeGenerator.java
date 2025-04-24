package com.stu.attendance.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@Slf4j
public class QrCodeGenerator {

    /**
     * Tạo mã QR dưới dạng mảng byte
     *
     * @param content Nội dung cần mã hóa trong mã QR (thường là token JWT)
     * @param size Kích thước của hình ảnh mã QR (chiều rộng và chiều cao tính bằng pixel)
     * @return Mảng byte chứa hình ảnh mã QR ở định dạng PNG
     */
    public byte[] generateQrCode(String content, int size) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();
        } catch (WriterException e) {
            log.error("Lỗi tạo mã QR: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo mã QR", e);
        } catch (IOException e) {
            log.error("Lỗi ghi mã QR ra output stream: {}", e.getMessage());
            throw new RuntimeException("Không thể ghi mã QR", e);
        }
    }

    /**
     * Tạo mã QR với các tham số tùy chỉnh
     *
     * @param content Nội dung cần mã hóa trong mã QR
     * @param width Chiều rộng của hình ảnh mã QR
     * @param height Chiều cao của hình ảnh mã QR
     * @return Mảng byte chứa hình ảnh mã QR ở định dạng PNG
     */
    public byte[] generateQrCode(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();
        } catch (WriterException e) {
            log.error("Lỗi tạo mã QR: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo mã QR", e);
        } catch (IOException e) {
            log.error("Lỗi ghi mã QR ra output stream: {}", e.getMessage());
            throw new RuntimeException("Không thể ghi mã QR", e);
        }
    }
}