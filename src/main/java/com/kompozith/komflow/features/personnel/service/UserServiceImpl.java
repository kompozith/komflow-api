package com.kompozith.komflow.features.personnel.service;

import com.kompozith.komflow.features.core.service.BaseService;
import com.kompozith.komflow.util.RequireExist;
import com.kompozith.komflow.util.SimpleResponse;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import com.kompozith.komflow.features.personnel.entity.Person;
import com.kompozith.komflow.features.personnel.entity.User;
import com.kompozith.komflow.features.personnel.repository.PersonRepository;
import com.kompozith.komflow.features.personnel.repository.PhoneNumberRepository;
import com.kompozith.komflow.features.personnel.repository.UserRepository;

public class UserServiceImpl extends BaseService implements UserService {


    private PhoneNumberRepository phoneNumberRepository;
    private UserRepository userRepository;
    private PersonRepository personRepository;

    // Check if the hone number is used when updating
    // Optional<PhoneNumber> foundPhoneNumber = phoneNumberRepository.findByNumber("");

    @Override
    public SimpleResponse<UserDetailsDto> findUserByEmail(String email) {

        Person person = RequireExist.of(personRepository.findByEmail(email), Person.class.getSimpleName());
        User user = RequireExist.of(userRepository.findByPersonId(person.getId()), User.class.getSimpleName());

        return new SimpleResponse<>(
                "user.information",
                UserDetailsDto.mapFromUser(user)
        );
    }
}
