package com.kompozith.komflow.features.auth.service;

import com.kompozith.komflow.features.auth.dto.LoginDto;
import com.kompozith.komflow.features.auth.dto.LoginResponseDto;
import com.kompozith.komflow.features.auth.dto.RefreshTokenDto;
import com.kompozith.komflow.features.auth.dto.SignUpDto;
import com.kompozith.komflow.features.auth.dto.UserPermissionsDto;
import com.kompozith.komflow.features.auth.entity.RefreshToken;
import com.kompozith.komflow.features.auth.entity.Role;
import com.kompozith.komflow.features.auth.repository.RefreshTokenRepository;
import com.kompozith.komflow.exception.InvalidCredentialsException;
import com.kompozith.komflow.exception.ObjectExistException;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.core.service.BaseService;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import com.kompozith.komflow.features.personnel.entity.Person;
import com.kompozith.komflow.features.personnel.entity.User;
import com.kompozith.komflow.features.personnel.repository.PersonRepository;
import com.kompozith.komflow.features.personnel.repository.UserRepository;
import com.kompozith.komflow.util.SimpleResponse;
import com.kompozith.komflow.configuration.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends BaseService implements AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public SimpleResponse<UserDetailsDto>  signUp(SignUpDto signUpDto) {

        // Check is the username is used
        Optional<User> foundUserByUserName = userRepository.findByUsername(signUpDto.username());

        if (foundUserByUserName.isPresent()) {
            throw new ObjectExistException(User.class.getSimpleName(),"username",signUpDto.username());
        }

        // Check if email is used
        Optional<Person> foundPersonByEmail = personRepository.findByEmail(signUpDto.email());
        if (foundPersonByEmail.isPresent()) {

            Person person = foundPersonByEmail.get();
            Optional<User> foundUserByPersonId = userRepository.findByPersonId(person.getId());
            if(foundUserByPersonId.isPresent()) {
                throw new ObjectExistException(User.class.getSimpleName(), "email", signUpDto.email());
            }
        }

        Person person = Person.builder()
                .email(signUpDto.email())
                .firstName(signUpDto.firstName())
                .lastName(signUpDto.lastName())
                .build();

        // Persist person
        person = personRepository.save(person);

        User user = User.builder()
                .username(signUpDto.username())
                .password(passwordEncoder.encode(signUpDto.password()))
                .person(person)
                .build();

        // Persist user
        user = userRepository.save(user);

        return new SimpleResponse<>(
                "userAccount.created",
                UserDetailsDto.mapFromUser(user)
        );
    }

    @Override
    public SimpleResponse<UserDetailsDto>  login(LoginDto loginDto) {
        User user = authenticateUser(loginDto);

        return new SimpleResponse<>(
                "authentication.success",
                UserDetailsDto.mapFromUser(user)
        );
    }

    @Override
    public SimpleResponse<UserDetailsDto> refreshToken(RefreshTokenDto refreshTokenDto) {
        User user = validateRefreshToken(refreshTokenDto);

        return new SimpleResponse<>(
                "token.refreshed",
                UserDetailsDto.mapFromUser(user)
        );
    }

    @Override
    public LoginResponseDto loginForFrontend(LoginDto loginDto) {
        User user = authenticateUser(loginDto);
        // Set the authenticated user in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, List.of())
        );
        return issueTokensForFrontend(user);
    }

    @Override
    public LoginResponseDto refreshTokenForFrontend(RefreshTokenDto refreshTokenDto) {
        User user = validateRefreshToken(refreshTokenDto);
        return issueTokensForFrontend(user);
    }

    @Override
    public void logout() {
        // Get current user from security context
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username != null) {
            // Find user by username
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Delete refresh token for the user
                Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
                if (existingToken.isPresent()) {
                    refreshTokenRepository.delete(existingToken.get());
                    refreshTokenRepository.flush(); // Force immediate deletion
                }
            }
        }
    }

    @Override
    public UserPermissionsDto getUserPermissions() {
        // Get current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null) {
            throw new InvalidCredentialsException("User not authenticated");
        }

        // Find user by username
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new ObjectNotFoundException(User.class.getSimpleName(), "username", username);
        }

        User user = userOpt.get();

        // Get roles and permissions from user's roles
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .distinct()
                .collect(Collectors.toList());

        return new UserPermissionsDto(permissions, roles);
    }

    private User authenticateUser(LoginDto loginDto) {
        User user = resolveUserByLogin(loginDto.login());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), loginDto.password())
            );
        } catch (ObjectNotFoundException | AuthenticationException e) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        return user;
    }

    private User resolveUserByLogin(String login) {
        Optional<User> optUser = userRepository.findByUsername(login);
        if (optUser.isPresent()) {
            return optUser.get();
        }

        Optional<Person> personOpt = personRepository.findByEmail(login);
        if (personOpt.isEmpty()) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        Optional<User> userByPerson = userRepository.findByPersonId(personOpt.get().getId());
        if (userByPerson.isEmpty()) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        return userByPerson.get();
    }

    private User validateRefreshToken(RefreshTokenDto refreshTokenDto) {
        String token = refreshTokenDto != null ? refreshTokenDto.getRefreshToken() : null;
        if (token == null || token.isBlank()) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);

        if (refreshTokenOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidCredentialsException("Refresh token expired");
        }

        return refreshToken.getUser();
    }

    private LoginResponseDto issueTokensForFrontend(User user) {
        String accessToken = jwtUtil.generateToken(user.getUsername(), "");
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        replaceRefreshToken(user, refreshToken);

        UserPermissionsDto permissions = buildPermissions(user);

        return LoginResponseDto.fromUserDetailsDto(
                UserDetailsDto.mapFromUser(user),
                accessToken,
                refreshToken,
                Math.toIntExact(jwtUtil.getConfig().getAccessTokenExpirationSeconds()),
                permissions
        );
    }

    private void replaceRefreshToken(User user, String refreshToken) {
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            refreshTokenRepository.delete(existingToken.get());
            refreshTokenRepository.flush(); // Force immediate deletion
        }

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtUtil.getConfig().getRefreshExpirationMs()))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
    }

    private UserPermissionsDto buildPermissions(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .distinct()
                .collect(Collectors.toList());

        return new UserPermissionsDto(permissions, roles);
    }
}
