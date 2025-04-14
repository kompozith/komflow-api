package org.example.komflow.features.messaging.repository;

import org.example.komflow.features.messaging.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
