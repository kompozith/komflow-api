package com.kompozith.komflow.features.auth.controller;

import com.kompozith.komflow.features.auth.dto.LoginDto;
import com.kompozith.komflow.features.auth.dto.SignUpDto;
import com.kompozith.komflow.features.auth.dto.UserDetailsWithTokenDto;
import com.kompozith.komflow.features.auth.service.AuthService;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import com.kompozith.komflow.util.SimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<SimpleResponse<UserDetailsWithTokenDto>> login(@Valid @RequestBody LoginDto loginDto) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.login(loginDto));
    }
}
