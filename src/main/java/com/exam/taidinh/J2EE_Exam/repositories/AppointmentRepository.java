package com.exam.taidinh.J2EE_Exam.repositories;

import com.exam.taidinh.J2EE_Exam.models.Appointment;
import com.exam.taidinh.J2EE_Exam.models.Doctor;
import com.exam.taidinh.J2EE_Exam.models.Patient;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientUsernameOrderByAppointmentDateAsc(String username);

    boolean existsByPatientAndDoctorAndAppointmentDate(Patient patient, Doctor doctor, LocalDate appointmentDate);
}
