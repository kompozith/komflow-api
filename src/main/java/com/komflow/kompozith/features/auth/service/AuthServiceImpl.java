package com.komflow.kompozith.features.auth.service;

import com.komflow.kompozith.features.auth.dto.LoginDto;
import com.komflow.kompozith.features.auth.dto.SignUpDto;
import com.komflow.kompozith.features.auth.dto.UserDetailsWithTokenDto;
import com.komflow.kompozith.features.auth.util.JwtHelper;
import com.komflow.kompozith.features.configuration.exception.ObjectExistException;
import com.komflow.kompozith.features.configuration.exception.ObjectNotFoundException;
import com.komflow.kompozith.features.core.service.BaseService;
import com.komflow.kompozith.features.personnel.dto.UserDetailsDto;
import com.komflow.kompozith.features.personnel.entity.Person;
import com.komflow.kompozith.features.personnel.entity.User;
import com.komflow.kompozith.features.personnel.repository.PersonRepository;
import com.komflow.kompozith.features.personnel.repository.UserRepository;
import com.komflow.kompozith.features.core.util.SimpleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        if(loginDto.username() != null) { // Find user by username
            optUser = userRepository.findByUsername(loginDto.email());
        }
        else if(loginDto.email() != null) { // Find user by email
            Optional<Person> optPerson = personRepository.findByEmail(loginDto.email());
            if(optPerson.isPresent()) {
                optUser = userRepository.findByPersonId(optPerson.get().getId());
            }
        }

        // Check if user was found
        if(optUser.isEmpty()) {
            throw new ObjectNotFoundException("invalid.userNameOrEmail");
        }

        User user = optUser.get();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));
        String token = JwtHelper.generateToken(loginDto.email());

        return new SimpleResponse<>(
                "userAccount.created",
                UserDetailsWithTokenDto.mapToUserDetailsWithTokenDto(user, token)
        );
    }
}
