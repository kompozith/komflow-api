package com.kompozith.komflow.features.personnel.service;

import com.kompozith.komflow.features.core.service.BaseService;
import com.kompozith.komflow.features.configuration.util.RequireExist;
import com.kompozith.komflow.features.configuration.record.SimpleResponse;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import com.kompozith.komflow.features.personnel.entity.User;
import com.kompozith.komflow.features.personnel.repository.PhoneNumberRepository;
import com.kompozith.komflow.features.personnel.repository.UserRepository;

public class UserServiceImpl extends BaseService implements UserService {


    private PhoneNumberRepository phoneNumberRepository;
    private UserRepository userRepository;

    // Check if the hone number is used when updating
    // Optional<PhoneNumber> foundPhoneNumber = phoneNumberRepository.findByNumber("");

    @Override
    public SimpleResponse<UserDetailsDto> findUserByUsername(String username) {

        User user = RequireExist.of(userRepository.findByUsername(username), "user.notFound") ;

        return new SimpleResponse<>(
                "user.information",
                UserDetailsDto.mapFromUser(user)
        );
    }
}
