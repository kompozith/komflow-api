package com.kompozith.komflow.features.personnel.repository;

import com.kompozith.komflow.KomflowApplication;
import com.kompozith.komflow.features.auth.entity.Role;
import com.kompozith.komflow.features.auth.repository.RoleRepository;
import com.kompozith.komflow.features.personnel.dto.UserDetailsInterfaceDto;
import com.kompozith.komflow.features.personnel.entity.Person;
import com.kompozith.komflow.features.personnel.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user;
    private Person person;
    private String testAppPermission;
    private String testRolePermission;
    private String testRoleName;
    private Role role;

    /*Given or Arrange*/
    @BeforeEach
    void setUp() {

        // User
        user = User.builder()
                .password("testPassword")
                .build();

        // Person
        person = Person.builder()
                .email("testUserEmail@example.com")
                .firstName("testFirstName")
                .lastName("testLastName")
                .build();

        // Permissions
        testAppPermission = "TEST_APP";
        testRolePermission = "TEST_ROLE";

        // Role
        testRoleName = "TEST_ROLE_NAME";
        role = new Role();
        role.setName(testRoleName);
    }

    @Test
    void shouldFindUserByPersonId() {

        // Given / Arrange
        Person savedPerson = this.personRepository.save(person);

        user.setPerson(savedPerson);
        this.userRepository.save(user);

        //When or Act
        Optional<User> foundUser = userRepository.findByPersonId(savedPerson.getId());

        //Then or Assert
        assertTrue(foundUser.isPresent());
        assertEquals(user.getPassword(), foundUser.get().getPassword());
        assertEquals(user.getPerson().getEmail(), foundUser.get().getPerson().getEmail());
    }

    @Test
    void shouldFindUserByPersonEmailContainingIgnoreCase() {

        // Given / Arrange
        Person savedPerson = this.personRepository.save(person);

        user.setPerson(savedPerson);
        this.userRepository.save(user);

        //When or Act
        var foundUsers = userRepository.findByPersonEmailContainingIgnoreCase("testuseremail");

        //Then or Assert
        assertEquals(1, foundUsers.size());
        assertEquals(user.getPerson().getEmail(), foundUsers.get(0).getPerson().getEmail());
    }

    @Test
    void shouldFindUserByEmail() {

        /*Given or Arrange*/
        Person savedPerson = this.personRepository.save(person);

        user.setPerson(savedPerson);
        this.userRepository.save(user);

        /*When or Act*/
        Optional<UserDetailsInterfaceDto> optUserDto = userRepository.findByEmail(savedPerson.getEmail());

        /*Then or Assert*/
        assertTrue(optUserDto.isPresent());
        UserDetailsInterfaceDto foundUserDto = optUserDto.get();

        assertEquals(person.getEmail(), foundUserDto.getEmail());
        assertEquals(person.getFirstName(), foundUserDto.getFirstName());
        assertEquals(person.getLastName(), foundUserDto.getLastName());
    }

    @Test
    void shouldFindUserByPersonEmailWithRolesAndPermissions() {

        /*Given or Arrange*/
        role.setPermissions(Set.of(testAppPermission, testRolePermission));

        Role savedRole = roleRepository.save(role);
        Person savedPerson = this.personRepository.save(person);

        user.setPerson(savedPerson);
        user.setRoles(Set.of(savedRole));
        this.userRepository.save(user);

        /*When or Act*/
        Optional<User> foundUser = userRepository.findByPersonEmailWithRolesAndPermissions(savedPerson.getEmail());

        /*Then or Assert*/

        // Role
        assertEquals(role.getName(), testRoleName);
        assertEquals(role.getName(), savedRole.getName());
        assertTrue(role.getPermissions().contains(testAppPermission));
        assertTrue(role.getPermissions().contains(testRolePermission));
        assertEquals(2, (role.getPermissions().size()));

        // User
        assertTrue(foundUser.isPresent());
        assertEquals(user.getPassword(), foundUser.get().getPassword());
        assertEquals(user.getPerson().getEmail(), foundUser.get().getPerson().getEmail());
        assertTrue(foundUser.get().getRoles().contains(role));
        assertEquals(1, foundUser.get().getRoles().size());
    }
}
