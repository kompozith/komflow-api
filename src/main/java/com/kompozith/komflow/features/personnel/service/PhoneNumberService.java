package com.kompozith.komflow.features.personnel.service;

import com.kompozith.komflow.features.personnel.dto.CreatePhoneNumberDto;
import com.kompozith.komflow.features.personnel.dto.PhoneNumberDto;

import java.util.List;

public interface PhoneNumberService {
    PhoneNumberDto addPhoneNumberToPerson(Long personId, CreatePhoneNumberDto createPhoneNumberDto);
    PhoneNumberDto updatePhoneNumber(Long phoneNumberId, CreatePhoneNumberDto createPhoneNumberDto);
    List<PhoneNumberDto> getPhoneNumbersByPersonId(Long personId);
    void deletePhoneNumber(Long phoneNumberId);
}