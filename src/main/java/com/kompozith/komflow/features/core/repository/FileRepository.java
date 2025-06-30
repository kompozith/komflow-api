package com.kompozith.komflow.features.core.repository;

import com.kompozith.komflow.features.core.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
