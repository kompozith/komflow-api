package com.komflow.kompozith.features.personnel.service;

import com.komflow.kompozith.features.core.util.SimpleResponse;
import com.komflow.kompozith.features.personnel.dto.UserDetailsDto;

public interface UserService {

    SimpleResponse<UserDetailsDto> findUserByUsername(String email);
}
