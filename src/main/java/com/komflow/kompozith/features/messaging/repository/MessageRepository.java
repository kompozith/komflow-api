package com.komflow.kompozith.features.messaging.repository;

import com.komflow.kompozith.features.messaging.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
