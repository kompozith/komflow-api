package com.kompozith.komflow.features.core.util;

import com.kompozith.komflow.features.auth.entity.Role;
import com.kompozith.komflow.features.auth.entity.RoleType;
import com.kompozith.komflow.features.auth.repository.RoleRepository;
import com.kompozith.komflow.features.contact.permissions.ContactPermissionEnum;
import com.kompozith.komflow.features.messaging.permissions.MessagePermissionEnum;
import com.kompozith.komflow.features.personnel.permissions.PersonnelPermissionEnum;
import com.kompozith.komflow.features.personnel.entity.Person;
import com.kompozith.komflow.features.personnel.entity.User;
import com.kompozith.komflow.features.personnel.repository.PersonRepository;
import com.kompozith.komflow.features.personnel.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static com.kompozith.komflow.features.core.util.AppConstants.*;

@Component
@RequiredArgsConstructor
public class DataInitializer {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonRepository personRepository;
    private final AdminBootstrapProperties adminBootstrapProperties;

    @PostConstruct
    public void init() {
        backfillRoleTypes();
        backfillRoleStatuses();
        initAdminRole();
        initAdminUser();
        assignAdminRoleToAdmin();
    }

    void backfillRoleTypes() {
        var rolesWithoutType = roleRepository.findByTypeIsNull();
        if (rolesWithoutType.isEmpty()) {
            return;
        }

        rolesWithoutType.forEach(role -> role.setType(RoleType.CUSTOM));
        roleRepository.saveAll(rolesWithoutType);
    }

    void backfillRoleStatuses() {
        var rolesWithoutStatus = roleRepository.findByActiveIsNull();
        if (rolesWithoutStatus.isEmpty()) {
            return;
        }

        rolesWithoutStatus.forEach(role -> role.setActive(true));
        roleRepository.saveAll(rolesWithoutStatus);
    }

    void initAdminRole() {

        Role adminRole = roleRepository.findByName(ADMIN_ROLE_NAME).orElse(null);

        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setName(ADMIN_ROLE_NAME);
            adminRole.setDescription(ADMIN_ROLE_DESCRIPTION);
            adminRole.setType(RoleType.SYSTEM);
            adminRole.setActive(true);

            Set<String> allPermissions = new HashSet<>();
            allPermissions.addAll(ContactPermissionEnum.getAllCodes());
            allPermissions.addAll(MessagePermissionEnum.getAllCodes());
            allPermissions.addAll(PersonnelPermissionEnum.getAllCodes());
            adminRole.setPermissions(allPermissions);
            adminRole.setType(RoleType.SYSTEM);

            roleRepository.save(adminRole);
        } else {
            // Update existing role with new permissions
            Set<String> allPermissions = new HashSet<>();
            allPermissions.addAll(ContactPermissionEnum.getAllCodes());
            allPermissions.addAll(MessagePermissionEnum.getAllCodes());
            allPermissions.addAll(PersonnelPermissionEnum.getAllCodes());
            adminRole.setPermissions(allPermissions);
            adminRole.setType(RoleType.SYSTEM);
            adminRole.setActive(true);

            roleRepository.save(adminRole);
        }
    }

    void initAdminUser() {

        String adminEmail = adminBootstrapProperties.getEmail();

        if (personRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setPassword(passwordEncoder.encode(adminBootstrapProperties.getPassword()));

            // Create a person for the user
            Person person = new Person();
            person.setEmail(adminEmail);

            personRepository.save(person);

            admin.setPerson(person);
            userRepository.save(admin);
        }
    }

    void assignAdminRoleToAdmin() {
        Role adminRole = roleRepository.findByName(ADMIN_ROLE_NAME).orElse(null);
        User admin = personRepository.findByEmail(adminBootstrapProperties.getEmail())
                .flatMap(person -> userRepository.findByPersonId(person.getId()))
                .orElse(null);

        assert adminRole != null;
        assert admin != null;
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);
    }
}
