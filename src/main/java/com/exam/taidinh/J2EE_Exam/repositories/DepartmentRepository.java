package com.exam.taidinh.J2EE_Exam.repositories;

import com.exam.taidinh.J2EE_Exam.models.Department;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);
}
