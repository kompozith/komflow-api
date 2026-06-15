package com.kompozith.komflow.configuration.security;

import com.kompozith.komflow.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {
    private final JwtConfig config;

    public JwtConfig getConfig() {
        return config;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(config.getSecretKey().getBytes());
    }

    public String generateToken(String username, String role) {
        return generateToken(username, role, null);
    }

    public String generateToken(String username, String role, Long organizationId) {
        var builder = Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuer(config.getIssuer())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + config.getExpirationMs()));
        if (organizationId != null) {
            builder.claim("organizationId", organizationId);
        }
        return builder.signWith(getSigningKey(), Jwts.SIG.HS256).compact();
    }

    public Long extractOrganizationId(String token) {
        try {
            Object claim = extractAllClaims(token).get("organizationId");
            if (claim instanceof Number n) return n.longValue();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuer(config.getIssuer())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + config.getRefreshExpirationMs()))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return getClaims(token).getPayload();
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null) {
            return null;
        }
        String prefix = "Bearer ";
        if (authHeader.startsWith(prefix)) {
            return authHeader.substring(prefix.length());
        }
        return null;
    }

    public String getUsernameFromToken(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw new JwtAuthenticationException("JWT token has expired", e, JwtAuthenticationException.JwtErrorType.EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token unsupported: {}", e.getMessage());
            throw new JwtAuthenticationException("JWT token is unsupported", e, JwtAuthenticationException.JwtErrorType.UNSUPPORTED);
        } catch (MalformedJwtException e) {
            log.warn("JWT token malformed: {}", e.getMessage());
            throw new JwtAuthenticationException("JWT token is malformed", e, JwtAuthenticationException.JwtErrorType.MALFORMED);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
            throw new JwtAuthenticationException("JWT token signature is invalid", e, JwtAuthenticationException.JwtErrorType.INVALID_SIGNATURE);
        } catch (IllegalArgumentException e) {
            log.warn("JWT token format invalid: {}", e.getMessage());
            throw new JwtAuthenticationException("JWT token format is invalid", e, JwtAuthenticationException.JwtErrorType.INVALID_FORMAT);
        } catch (JwtException e) {
            log.warn("JWT token invalid: {}", e.getMessage());
            throw new JwtAuthenticationException("JWT token is invalid", e, JwtAuthenticationException.JwtErrorType.INVALID_CLAIMS);
        }
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("JWT token has expired", e, JwtAuthenticationException.JwtErrorType.EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException("JWT token is unsupported", e, JwtAuthenticationException.JwtErrorType.UNSUPPORTED);
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException("JWT token is malformed", e, JwtAuthenticationException.JwtErrorType.MALFORMED);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new JwtAuthenticationException("JWT token signature is invalid", e, JwtAuthenticationException.JwtErrorType.INVALID_SIGNATURE);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException("JWT token format is invalid", e, JwtAuthenticationException.JwtErrorType.INVALID_FORMAT);
        } catch (JwtException e) {
            throw new JwtAuthenticationException("JWT token is invalid", e, JwtAuthenticationException.JwtErrorType.INVALID_CLAIMS);
        }
    }

    public boolean isTokenValid(String token, String username) {
        final String subject = extractAllClaims(token).getSubject();
        return subject.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
    }
}
