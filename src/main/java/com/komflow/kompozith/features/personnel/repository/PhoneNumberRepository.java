package com.komflow.kompozith.features.personnel.repository;

import com.komflow.kompozith.features.personnel.entity.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Long> {

    Optional<PhoneNumber> findByNumber(String number);
}
