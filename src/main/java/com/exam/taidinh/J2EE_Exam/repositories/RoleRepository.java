package com.exam.taidinh.J2EE_Exam.repositories;

import com.exam.taidinh.J2EE_Exam.models.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
