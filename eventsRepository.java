package account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface eventsRepository extends JpaRepository<eventField, Long> {
}
