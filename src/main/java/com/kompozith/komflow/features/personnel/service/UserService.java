package com.kompozith.komflow.features.personnel.service;

import com.kompozith.komflow.util.SimpleResponse;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;

public interface UserService {

    SimpleResponse<UserDetailsDto> findUserByEmail(String email);
}
