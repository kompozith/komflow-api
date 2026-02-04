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
import com.kompozith.komflow.util.RequireExist;
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

        Optional<User> optUser = Optional.empty();
        User user = null;
        Person person = null;

        // Find user by username
        optUser = userRepository.findByUsername(loginDto.login());

        if(optUser.isEmpty()) {
            // Find user by email if not found by username
            try {
                person = RequireExist.of(personRepository.findByEmail(loginDto.login()),User.class.getSimpleName());
            } catch (ObjectNotFoundException e) {
                throw new InvalidCredentialsException("Invalid login or password");
            }

            user = RequireExist.of(userRepository.findByPersonId(person.getId()),User.class.getSimpleName());
        }
        else {
            user = optUser.get();
            person = user.getPerson();
        }

        // On authentifie utilisation a partir de son nom utilisation et son mot de passe.
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), loginDto.password()));
        } catch (ObjectNotFoundException | AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid login or password"); //Invalid password
        }

        // Generer le token de l'itilisateur a partir de son username
        String token = jwtUtil.generateToken(user.getUsername(), "");

        // Generate refresh token
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // Delete existing refresh token for user if exists and save new one
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            refreshTokenRepository.delete(existingToken.get());
            refreshTokenRepository.flush(); // Force immediate deletion
        }

        // Save new refresh token
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtUtil.getConfig().getRefreshExpirationMs()))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return new SimpleResponse<>(
                "authentication.success",
                UserDetailsDto.mapFromUser(user)
        );
    }

    @Override
    public SimpleResponse<UserDetailsDto> refreshToken(RefreshTokenDto refreshTokenDto) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenDto.getRefreshToken());

        if (refreshTokenOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidCredentialsException("Refresh token expired");
        }

        User user = refreshToken.getUser();

        // Generate new access token
        String newToken = jwtUtil.generateToken(user.getUsername(), "");

        // Generate new refresh token
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // Delete old refresh token and save new one
        refreshTokenRepository.delete(refreshToken);
        refreshTokenRepository.flush(); // Force immediate deletion

        // Save new refresh token
        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .token(newRefreshToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtUtil.getConfig().getRefreshExpirationMs()))
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        return new SimpleResponse<>(
                "token.refreshed",
                UserDetailsDto.mapFromUser(user)
        );
    }

    @Override
    public LoginResponseDto loginForFrontend(LoginDto loginDto) {
        SimpleResponse<UserDetailsDto> response = this.login(loginDto);
        // Set the authenticated user in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(response.getData().getUsername(), null, List.of()));
        UserPermissionsDto permissions = this.getUserPermissions();
        return LoginResponseDto.fromUserDetailsDto(
                response.getData(),
                jwtUtil.generateToken(response.getData().getUsername(), ""),
                jwtUtil.generateRefreshToken(response.getData().getUsername()),
                Math.toIntExact(jwtUtil.getConfig().getAccessTokenExpirationSeconds()),
                permissions
        );
    }

    @Override
    public LoginResponseDto refreshTokenForFrontend(RefreshTokenDto refreshTokenDto) {
        SimpleResponse<UserDetailsDto> response = this.refreshToken(refreshTokenDto);
        UserPermissionsDto permissions = this.getUserPermissions();
        return LoginResponseDto.fromUserDetailsDto(
                response.getData(),
                jwtUtil.generateToken(response.getData().getUsername(), ""),
                jwtUtil.generateRefreshToken(response.getData().getUsername()),
                Math.toIntExact(jwtUtil.getConfig().getAccessTokenExpirationSeconds()),
                permissions
        );
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
}
