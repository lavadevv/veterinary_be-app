package ext.vnua.veterinary_beapp.config;

import ext.vnua.veterinary_beapp.modules.users.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class JwtConfig {

    @NonFinal
    @Value("${jwt.key}")
    String JWT_SECRET;

    // Thời gian có hiệu lực của chuỗi jwt
    final long JWT_EXPIRATION = 24 * 60 * 60 * 1000; // 1 ngày

    // Tạo JWT từ thông tin user
    public String generateToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + JWT_EXPIRATION);

        final String authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(user.getEmail())               // Email người dùng (subject)
                .issuedAt(issuedAt)                     // Thời gian tạo
                .expiration(expiration)                 // Thời gian hết hạn
                .signWith(key)                          // SecretKey
                .id(UUID.randomUUID().toString())       // ID duy nhất của token
                .claim("scope", authorities)            // Quyền
                .claim("userId", user.getId())          // Id của user (claim riêng)
                .compact();
    }

    /** Lấy toàn bộ Claims từ JWT */
    public Claims getClaims(String token){
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Xác thực token */
    public boolean validateToken(String authToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    /** Lấy email (subject) từ JWT */
    public String getUserEmailFromJWT(String token) {
        return getClaims(token).getSubject();
    }

    /** Lấy userId (claim) từ JWT; trả Long hoặc null nếu không có/không parse được */
    public Long getUserIdFromJWTClaims(String token) {
        Object userIdObj = getClaims(token).get("userId");
        if (userIdObj == null) return null;
        if (userIdObj instanceof Number) return ((Number) userIdObj).longValue();
        try {
            return Long.parseLong(userIdObj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @deprecated Tên hàm dễ gây hiểu nhầm (thực tế trả về subject/email).
     * Dùng {@link #getUserEmailFromJWT(String)} để lấy email,
     * hoặc {@link #getUserIdFromJWTClaims(String)} để lấy userId.
     */
    @Deprecated
    public String getUserIdFromJWT(String token) {
        return getUserEmailFromJWT(token);
    }
}
