package org.example.komflow.features.general.repository;

import org.example.komflow.features.general.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
