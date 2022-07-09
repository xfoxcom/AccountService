package account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface employeeRepository extends JpaRepository<Employee, String> {
    boolean existsByPeriodAndEmployee (String period, String employee);
    Employee findByPeriodAndEmployee (String period, String employee);
    Employee findByEmployee (String employee);
    Employee findByPeriod (String period);
    List<Employee> findByEmployeeOrderByPeriodDesc (String email);
}
