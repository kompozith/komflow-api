package features.contact.repository;

import features.contact.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepositoty extends JpaRepository<Tag, Long> {
}
