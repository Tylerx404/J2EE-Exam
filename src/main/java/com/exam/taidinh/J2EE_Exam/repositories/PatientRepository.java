package com.exam.taidinh.J2EE_Exam.repositories;

import com.exam.taidinh.J2EE_Exam.models.Patient;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUsername(String username);

    Optional<Patient> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
