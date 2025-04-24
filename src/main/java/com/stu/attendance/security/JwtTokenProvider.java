package com.stu.attendance.security;

import com.stu.attendance.entity.TaiKhoan;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration; // Thời gian hết hạn token (milliseconds)

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // Thời gian hết hạn refresh token (milliseconds)

    private Key key;

    @PostConstruct
    public void init() {
        // Generate a secure key for HS512 algorithm
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    /**
     * Tạo access token từ thông tin người dùng
     */
    public String generateAccessToken(TaiKhoan taiKhoan) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", taiKhoan.getNguoiDung().getMaNguoiDung());
        claims.put("role", taiKhoan.getRole().name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(taiKhoan.getTenTaiKhoan())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key) // Remove the explicit algorithm here, as it's already defined in the key
                .compact();
    }
    /**
     * Tạo refresh token từ thông tin người dùng
     */
    public String generateRefreshToken(TaiKhoan taiKhoan) {
        return Jwts.builder()
                .setSubject(taiKhoan.getTenTaiKhoan())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key) // Remove SignatureAlgorithm.HS512
                .compact();
    }

    /**
     * Lấy tên người dùng từ token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Lấy ngày hết hạn từ token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Lấy thông tin claim từ token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Lấy tất cả thông tin claim từ token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Kiểm tra token hết hạn
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Xác thực token
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token không hợp lệ: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Xác thực token với người dùng
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Lấy role từ token
     */
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Lấy mã người dùng từ token
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", String.class);
    }

    /**
     * Tạo token QR code cho điểm danh
     * @param sessionId Mã buổi học
     * @param validityMinutes Thời gian hiệu lực (phút)
     * @return Token JWT
     */
    public String generateQrToken(Integer sessionId, Integer validityMinutes) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sessionId", sessionId);
        claims.put("purpose", "attendance");
        claims.put("timestamp", new Date().getTime());

        long expirationTime = validityMinutes * 60 * 1000;

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key) // Remove SignatureAlgorithm.HS512
                .compact();
    }

    /**
     * Xác thực token QR và trích xuất thông tin
     * @param token Token JWT từ mã QR
     * @return Map các thông tin trong token nếu hợp lệ, null nếu không hợp lệ
     */
    public Map<String, Object> validateQrToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            if (isTokenExpired(token)) {
                log.warn("QR token đã hết hạn");
                return null;
            }

            // Kiểm tra mục đích sử dụng token
            if (!"attendance".equals(claims.get("purpose"))) {
                log.warn("Token không phải dành cho điểm danh");
                return null;
            }

            // Chuyển claims thành Map
            Map<String, Object> resultClaims = new HashMap<>();
            claims.forEach(resultClaims::put);
            return resultClaims;
        } catch (Exception e) {
            log.error("Lỗi xác thực QR token: {}", e.getMessage());
            return null;
        }
    }
}