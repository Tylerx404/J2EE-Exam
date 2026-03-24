package com.exam.taidinh.J2EE_Exam.config;

import com.exam.taidinh.J2EE_Exam.models.Patient;
import com.exam.taidinh.J2EE_Exam.models.Role;
import com.exam.taidinh.J2EE_Exam.repositories.PatientRepository;
import com.exam.taidinh.J2EE_Exam.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SecurityDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public SecurityDataInitializer(
        RoleRepository roleRepository,
        PatientRepository patientRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseGet(() -> roleRepository.save(new Role("ADMIN")));
        Role patientRole = roleRepository.findByName("PATIENT")
            .orElseGet(() -> roleRepository.save(new Role("PATIENT")));

        if (!patientRepository.existsByUsername("admin")) {
            Patient admin = new Patient("admin", passwordEncoder.encode("Admin@123"), "admin@clinic.local");
            admin.addRole(adminRole);
            patientRepository.save(admin);
        }

        if (!patientRepository.existsByUsername("patient")) {
            Patient patient = new Patient("patient", passwordEncoder.encode("Patient@123"), "patient@clinic.local");
            patient.addRole(patientRole);
            patientRepository.save(patient);
        }
    }
}
