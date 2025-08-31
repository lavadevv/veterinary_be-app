package ext.vnua.veterinary_beapp.config;

import ext.vnua.veterinary_beapp.modules.users.model.User;
import io.jsonwebtoken.*;
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
    private String JWT_SECRET;

    // Thời gian có hiệu lực của chuỗi jwt
    private final long JWT_EXPIRATION = 24 * 60 * 60 * 1000; // 1 ngày

    // Tạo ra jwt từ thông tin user
    public String generateToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + JWT_EXPIRATION);

        final String authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(user.getEmail()) //Email nguoi dung
                .issuedAt(issuedAt) //Thoi gian tao
                .expiration(expiration) //Thoi gian het han
                .signWith(key) // SecretKey trong .env
                .id(UUID.randomUUID().toString()) // ID duy nhat cua token
                .claim("scope", authorities)  //Quyen
                .claim("userId", user.getId()) //Id cua user
                .compact(); // Tao Strin JWT
    }

    // Lấy thông tin user từ jwt
    public String getUserIdFromJWT(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Claims getClaims(String token){
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

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
}
