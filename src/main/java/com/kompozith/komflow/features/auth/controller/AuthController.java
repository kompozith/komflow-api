package com.kompozith.komflow.features.auth.controller;

import com.kompozith.komflow.features.auth.dto.LoginDto;
import com.kompozith.komflow.features.auth.dto.LoginResponseDto;
import com.kompozith.komflow.features.auth.dto.RefreshTokenDto;
import com.kompozith.komflow.features.auth.dto.SignUpDto;
import com.kompozith.komflow.features.auth.dto.UserPermissionsDto;
import com.kompozith.komflow.features.auth.service.AuthService;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import com.kompozith.komflow.util.SimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    public ResponseEntity<SimpleResponse<UserDetailsDto>> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signUp(signUpDto));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        LoginResponseDto response = authService.loginForFrontend(loginDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Refresh JWT token using refresh token")
    public ResponseEntity<LoginResponseDto> refresh(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        LoginResponseDto response = authService.refreshTokenForFrontend(refreshTokenDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate server-side session/token")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get user permissions", description = "Retrieve current user's permissions and roles")
    public ResponseEntity<UserPermissionsDto> getPermissions() {
        UserPermissionsDto permissions = authService.getUserPermissions();
        return ResponseEntity.status(HttpStatus.OK).body(permissions);
    }
}
