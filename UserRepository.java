package account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    public User findByEmailIgnoreCase(String email);
    public boolean existsByEmail(String email);
}
