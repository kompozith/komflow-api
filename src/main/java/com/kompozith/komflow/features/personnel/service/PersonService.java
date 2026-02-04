package com.kompozith.komflow.features.personnel.service;

import com.kompozith.komflow.features.personnel.dto.CreatePersonDto;
import com.kompozith.komflow.features.personnel.dto.PersonDetailsDto;
import com.kompozith.komflow.features.personnel.dto.PersonDto;
import com.kompozith.komflow.features.personnel.dto.UpdatePersonDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PersonService {
    PersonDto create(CreatePersonDto createPersonDto);
    Page<PersonDto> findAll(Pageable pageable, String search);
    PersonDetailsDto findById(Long id);
    PersonDto update(Long id, UpdatePersonDto updatePersonDto);
}
