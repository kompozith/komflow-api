package com.kompozith.komflow.features.personnel.repository;

import com.kompozith.komflow.features.personnel.entity.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Long> {

    Optional<PhoneNumber> findByNumber(String number);

    List<PhoneNumber> findByPersonId(Long personId);
}
