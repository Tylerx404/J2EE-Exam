package com.exam.taidinh.J2EE_Exam.repositories;

import com.exam.taidinh.J2EE_Exam.models.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Page<Doctor> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
