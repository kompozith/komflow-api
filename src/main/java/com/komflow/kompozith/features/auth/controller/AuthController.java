package com.komflow.kompozith.features.auth.controller;

import com.komflow.kompozith.features.auth.dto.LoginDto;
import com.komflow.kompozith.features.auth.dto.SignUpDto;
import com.komflow.kompozith.features.auth.dto.UserDetailsWithTokenDto;
import com.komflow.kompozith.features.auth.service.AuthService;
import com.komflow.kompozith.features.configuration.exception.ObjectExistException;
import com.komflow.kompozith.features.configuration.exception.ObjectNotFoundException;
import com.komflow.kompozith.features.personnel.dto.UserDetailsDto;
import com.komflow.kompozith.features.core.util.SimpleResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.komflow.kompozith.features.core.util.AppConstants.API_PREFIX_V1;

@RestController
@AllArgsConstructor
@RequestMapping(API_PREFIX_V1+"auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SimpleResponse<UserDetailsDto>> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(authService.signUp(signUpDto));
        } catch (ObjectExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new SimpleResponse<>(e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<SimpleResponse<UserDetailsWithTokenDto>> login(@Valid @RequestBody LoginDto loginDto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(authService.login(loginDto));
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new SimpleResponse<>(e.getMessage(), null));
        }
    }
}
