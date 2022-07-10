package account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
     User findByEmailIgnoreCase(String email);
     boolean existsByEmailIgnoreCase(String email);
     void deleteByEmail(String email);

}
