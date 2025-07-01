package com.kompozith.komflow.features.personnel.service;

import com.kompozith.komflow.configuration.util.SimpleResponse;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;

public interface UserService {

    SimpleResponse<UserDetailsDto> findUserByUsername(String email);
}
