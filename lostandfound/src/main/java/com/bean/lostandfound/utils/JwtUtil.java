package com.bean.lostandfound.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtUtil {

    // 使用JWT推荐的方式生成安全密钥
    private SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    // token过期时间（毫秒）
    private long expire = 86400000; // 24小时

    // token在header中的名称
    private String header = "token";

    /**
     * 生成JWT token
     */
    public String generateToken(Integer userId, String username, Integer role) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expire);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析JWT token
     */
    public Claims getClaimsByToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取用户ID
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsByToken(token);
        return ((Number) claims.get("userId")).intValue();
    }

    /**
     * 获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsByToken(token);
        return claims.getSubject();
    }

    /**
     * 获取用户角色
     */
    public Integer getRoleFromToken(String token) {
        Claims claims = getClaimsByToken(token);
        return ((Number) claims.get("role")).intValue();
    }

    /**
     * 验证token是否过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsByToken(token);
            return !isTokenExpired(claims.getExpiration());
        } catch (Exception e) {
            return false;
        }
    }
}
