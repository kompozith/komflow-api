package com.kompozith.komflow.features.personnel.service;

import com.kompozith.komflow.features.configuration.record.SimpleResponse;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;

public interface UserService {

    SimpleResponse<UserDetailsDto> findUserByUsername(String email);
}
