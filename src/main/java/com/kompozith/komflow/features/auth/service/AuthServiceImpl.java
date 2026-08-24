package com.kompozith.komflow.features.auth.service;

import com.kompozith.komflow.features.auth.dto.LoginDto;
import com.kompozith.komflow.features.auth.dto.LoginResponseDto;
import com.kompozith.komflow.features.auth.dto.RefreshTokenDto;
import com.kompozith.komflow.features.auth.dto.SignUpDto;
import com.kompozith.komflow.features.auth.dto.UserPermissionsDto;
import com.kompozith.komflow.features.auth.entity.RefreshToken;
import com.kompozith.komflow.features.auth.entity.Role;
import com.kompozith.komflow.features.auth.repository.RefreshTokenRepository;
import com.kompozith.komflow.features.auth.repository.RoleRepository;
import com.kompozith.komflow.exception.InvalidCredentialsException;
import com.kompozith.komflow.exception.ObjectExistException;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.core.service.BaseService;
import com.kompozith.komflow.features.organization.TenantContext;
import com.kompozith.komflow.features.organization.entity.Organization;
import com.kompozith.komflow.features.organization.entity.OrganizationMember;
import com.kompozith.komflow.features.organization.entity.OrganizationMember.MemberStatus;
import com.kompozith.komflow.features.organization.entity.OrganizationMember.WorkspaceRole;
import com.kompozith.komflow.features.organization.dto.WorkspaceSummaryDto;
import com.kompozith.komflow.features.organization.repository.OrganizationMemberRepository;
import com.kompozith.komflow.features.organization.repository.OrganizationRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends BaseService implements AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";

    private final UserRepository         userRepository;
    private final PersonRepository       personRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final PasswordEncoder        passwordEncoder;
    private final AuthenticationManager  authenticationManager;
    private final JwtUtil                jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository         roleRepository;

    @Override
    public SimpleResponse<UserDetailsDto>  signUp(SignUpDto signUpDto) {

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
    @Transactional
    public LoginResponseDto signUpForFrontend(SignUpDto signUpDto) {
        // ── Vérifications unicité ─────────────────────────────────────────
        personRepository.findByEmail(signUpDto.email()).ifPresent(p -> {
            userRepository.findByPersonId(p.getId()).ifPresent(u -> {
                throw new ObjectExistException(User.class.getSimpleName(), "email", signUpDto.email());
            });
        });

        // ── Création de l'organisation ────────────────────────────────────
        String slug = buildSlug(signUpDto.organizationSlug(), signUpDto.organizationName());
        if (organizationRepository.existsBySlug(slug)) {
            // Unicité du slug : on suffixe avec un timestamp court
            slug = slug + "-" + Long.toHexString(System.currentTimeMillis()).substring(4);
        }
        Organization org = Organization.builder()
                .name(signUpDto.organizationName())
                .slug(slug)
                .planCode("FREE")
                .active(true)
                .build();
        org = organizationRepository.save(org);

        // ── Création de la personne et de l'utilisateur ───────────────────
        Person person = personRepository.save(Person.builder()
                .email(signUpDto.email())
                .firstName(signUpDto.firstName())
                .lastName(signUpDto.lastName())
                .build());

        User user = userRepository.save(User.builder()
                .password(passwordEncoder.encode(signUpDto.password()))
                .person(person)
                .enabled(true)
                .build());

        // ── Création du membre OWNER dans l'organisation ──────────────────────
        OrganizationMember ownerMembership = OrganizationMember.builder()
                .organization(org)
                .user(user)
                .role(WorkspaceRole.OWNER)
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(ownerMembership);

        // ── Assigner le rôle ADMIN système au nouvel utilisateur ────────────
        roleRepository.findByName("ADMIN").ifPresent(adminRole ->
                user.setRoles(java.util.Set.of(adminRole)));
        userRepository.save(user);

        // ── Émission des tokens avec organizationId ───────────────────────
        TenantContext.setOrganizationId(org.getId());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getPerson().getEmail(), null, List.of()));

        return issueTokensForFrontend(user, org.getId());
    }

    /** Génère un slug URL-friendly depuis un slug fourni ou un nom libre. */
    private String buildSlug(String slugInput, String name) {
        String base = (slugInput != null && !slugInput.isBlank())
                ? slugInput
                : name.toLowerCase()
                      .replaceAll("[^a-z0-9]+", "-")
                      .replaceAll("^-|-$", "");
        return base.length() > 60 ? base.substring(0, 60) : base;
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
                new UsernamePasswordAuthenticationToken(user.getPerson().getEmail(), null, List.of())
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
        // Get current user (email) from security context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email != null) {
            // Find user by email
            Optional<Person> personOpt = personRepository.findByEmail(email);
            Optional<User> userOpt = personOpt.flatMap(p -> userRepository.findByPersonId(p.getId()));
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
        // Get current authenticated user (email)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null) {
            throw new InvalidCredentialsException("User not authenticated");
        }

        // Find user by email
        Optional<User> userOpt = personRepository.findByEmail(email)
                .flatMap(p -> userRepository.findByPersonId(p.getId()));
        if (userOpt.isEmpty()) {
            throw new ObjectNotFoundException(User.class.getSimpleName(), "email", email);
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
                    new UsernamePasswordAuthenticationToken(user.getPerson().getEmail(), loginDto.password())
            );
        } catch (ObjectNotFoundException | AuthenticationException e) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        return user;
    }

    private User resolveUserByLogin(String login) {
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
        Long orgId = TenantContext.getOrganizationId();
        return issueTokensForFrontend(user, orgId);
    }

    private LoginResponseDto issueTokensForFrontend(User user, Long orgId) {
        List<WorkspaceSummaryDto> workspaces = memberRepository
                .findByUserIdOrderByCreatedAtAsc(user.getId())
                .stream()
                .map(WorkspaceSummaryDto::from)
                .toList();

        // A plain login carries no organizationId claim (unlike signup, which sets
        // TenantContext right after creating the org). If the user belongs to
        // exactly one workspace, mint the token with it already selected instead
        // of forcing an extra /workspaces/{orgId}/switch round trip.
        Long effectiveOrgId = orgId;
        if (effectiveOrgId == null && workspaces.size() == 1) {
            effectiveOrgId = workspaces.get(0).orgId();
        }

        String email = user.getPerson().getEmail();
        String accessToken = jwtUtil.generateToken(email, "", effectiveOrgId);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        replaceRefreshToken(user, refreshToken);

        UserPermissionsDto permissions = buildPermissions(user);

        return LoginResponseDto.fromUserDetailsDto(
                UserDetailsDto.mapFromUser(user),
                accessToken,
                refreshToken,
                Math.toIntExact(jwtUtil.getConfig().getAccessTokenExpirationSeconds()),
                permissions,
                workspaces
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
