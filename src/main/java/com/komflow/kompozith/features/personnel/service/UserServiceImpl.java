package com.komflow.kompozith.features.personnel.service;

import com.komflow.kompozith.features.core.service.BaseService;
import com.komflow.kompozith.features.core.util.RequireExist;
import com.komflow.kompozith.features.core.util.SimpleResponse;
import com.komflow.kompozith.features.personnel.dto.UserDetailsDto;
import com.komflow.kompozith.features.personnel.entity.User;
import com.komflow.kompozith.features.personnel.repository.PhoneNumberRepository;
import com.komflow.kompozith.features.personnel.repository.UserRepository;

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
