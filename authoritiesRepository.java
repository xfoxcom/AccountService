package account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface authoritiesRepository extends JpaRepository<Authorities, Long> {
}
