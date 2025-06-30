package com.kompozith.komflow.features.auth.controller;

import com.kompozith.komflow.features.auth.dto.LoginDto;
import com.kompozith.komflow.features.auth.dto.SignUpDto;
import com.kompozith.komflow.features.auth.dto.UserDetailsWithTokenDto;
import com.kompozith.komflow.features.auth.service.AuthService;
import com.kompozith.komflow.features.configuration.exception.ObjectExistException;
import com.kompozith.komflow.features.configuration.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import com.kompozith.komflow.features.configuration.record.SimpleResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.kompozith.komflow.features.core.util.AppConstants.API_PREFIX_V1;

@RestController
@AllArgsConstructor
@RequestMapping(API_PREFIX_V1+"/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SimpleResponse<UserDetailsDto>> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signUp(signUpDto));
    }

    @PostMapping("/login")
    public ResponseEntity<SimpleResponse<UserDetailsWithTokenDto>> login(@Valid @RequestBody LoginDto loginDto) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.login(loginDto));
    }
}
