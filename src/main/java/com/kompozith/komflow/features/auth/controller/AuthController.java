package com.kompozith.komflow.features.auth.controller;

import com.kompozith.komflow.features.auth.dto.LoginDto;
import com.kompozith.komflow.features.auth.dto.LoginResponseDto;
import com.kompozith.komflow.features.auth.dto.RefreshTokenDto;
import com.kompozith.komflow.features.auth.dto.SignUpDto;
import com.kompozith.komflow.features.auth.dto.UserPermissionsDto;
import com.kompozith.komflow.features.auth.service.AuthService;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import com.kompozith.komflow.util.SimpleResponse;
import com.kompozith.komflow.configuration.security.AuthCookieConfig;
import com.kompozith.komflow.configuration.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AuthCookieConfig authCookieConfig;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    public ResponseEntity<SimpleResponse<UserDetailsDto>> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signUp(signUpDto));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response) {
        LoginResponseDto body = authService.loginForFrontend(loginDto);
        ResponseCookie refreshCookie = buildRefreshCookie(body.getRefreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        body.setRefreshToken(null);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Refresh JWT token using refresh token")
    public ResponseEntity<LoginResponseDto> refresh(
            @RequestBody(required = false) RefreshTokenDto refreshTokenDto,
            @CookieValue(name = "${auth.cookie.name}", required = false) String refreshTokenCookie,
            HttpServletResponse response
    ) {
        RefreshTokenDto effectiveDto = resolveRefreshToken(refreshTokenDto, refreshTokenCookie);
        LoginResponseDto body = authService.refreshTokenForFrontend(effectiveDto);
        ResponseCookie refreshCookie = buildRefreshCookie(body.getRefreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        body.setRefreshToken(null);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate server-side session/token")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout();
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get user permissions", description = "Retrieve current user's permissions and roles")
    public ResponseEntity<UserPermissionsDto> getPermissions() {
        UserPermissionsDto permissions = authService.getUserPermissions();
        return ResponseEntity.status(HttpStatus.OK).body(permissions);
    }

    private RefreshTokenDto resolveRefreshToken(RefreshTokenDto refreshTokenDto, String refreshTokenCookie) {
        if (refreshTokenDto != null && StringUtils.hasText(refreshTokenDto.getRefreshToken())) {
            return refreshTokenDto;
        }
        if (StringUtils.hasText(refreshTokenCookie)) {
            RefreshTokenDto dto = new RefreshTokenDto();
            dto.setRefreshToken(refreshTokenCookie);
            return dto;
        }
        RefreshTokenDto dto = new RefreshTokenDto();
        dto.setRefreshToken(null);
        return dto;
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        long maxAgeSeconds = Math.max(0, jwtUtil.getConfig().getRefreshExpirationMs() / 1000);
        return ResponseCookie.from(authCookieConfig.getName(), refreshToken)
                .httpOnly(authCookieConfig.isHttpOnly())
                .secure(authCookieConfig.isSecure())
                .path(authCookieConfig.getPath())
                .sameSite(authCookieConfig.getSameSite())
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(authCookieConfig.getName(), "")
                .httpOnly(authCookieConfig.isHttpOnly())
                .secure(authCookieConfig.isSecure())
                .path(authCookieConfig.getPath())
                .sameSite(authCookieConfig.getSameSite())
                .maxAge(0)
                .build();
    }
}
