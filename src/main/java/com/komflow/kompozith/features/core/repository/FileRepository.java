package com.komflow.kompozith.features.core.repository;

import com.komflow.kompozith.features.core.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
