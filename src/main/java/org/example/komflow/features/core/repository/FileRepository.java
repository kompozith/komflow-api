package org.example.komflow.features.core.repository;

import org.example.komflow.features.core.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
