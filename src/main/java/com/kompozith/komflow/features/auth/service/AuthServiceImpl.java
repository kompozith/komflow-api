package com.kompozith.komflow.features.auth.service;

import com.kompozith.komflow.features.auth.dto.LoginDto;
import com.kompozith.komflow.features.auth.dto.SignUpDto;
import com.kompozith.komflow.features.auth.dto.UserDetailsWithTokenDto;
import com.kompozith.komflow.features.configuration.exception.InvalidCredentialsException;
import com.kompozith.komflow.features.configuration.exception.ObjectExistException;
import com.kompozith.komflow.features.configuration.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.configuration.util.RequireExist;
import com.kompozith.komflow.features.core.service.BaseService;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import com.kompozith.komflow.features.personnel.entity.Person;
import com.kompozith.komflow.features.personnel.entity.User;
import com.kompozith.komflow.features.personnel.repository.PersonRepository;
import com.kompozith.komflow.features.personnel.repository.UserRepository;
import com.kompozith.komflow.features.configuration.record.SimpleResponse;
import com.kompozith.komflow.features.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends BaseService implements AuthService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public SimpleResponse<UserDetailsDto>  signUp(SignUpDto signUpDto) {

        // Check is the username is used
        Optional<User> foundUserByUserName = userRepository.findByUsername(signUpDto.username());

        if (foundUserByUserName.isPresent()) {
            throw new ObjectExistException("username.exists");
        }

        // Check if email is used
        Optional<Person> foundPersonByEmail = personRepository.findByEmail(signUpDto.email());
        if (foundPersonByEmail.isPresent()) {

            Person person = foundPersonByEmail.get();
            Optional<User> foundUserByPersonId = userRepository.findByPersonId(person.getId());
            if(foundUserByPersonId.isPresent()) {
                throw new ObjectExistException("email.exists");
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
    public SimpleResponse<UserDetailsWithTokenDto>  login(LoginDto loginDto) {

        Optional<User> optUser = Optional.empty();
        User user = null;
        Person person = null;

        // Find user by username
        optUser = userRepository.findByUsername(loginDto.login());

        if(optUser.isEmpty()) {
            // Find user by email if not found by username
            try {
                person = RequireExist.of(personRepository.findByEmail(loginDto.login()),"");
            } catch (ObjectNotFoundException e) {
                throw new InvalidCredentialsException("Invalid login or password");
            }

            user = RequireExist.of(userRepository.findByPersonId(person.getId()),"person");
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

        return new SimpleResponse<>(
                "authentication.success",
                UserDetailsWithTokenDto.mapToUserDetailsWithTokenDto(user, token)
        );
    }
}
