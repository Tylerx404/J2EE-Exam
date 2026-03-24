package com.exam.taidinh.J2EE_Exam.controllers;

import com.exam.taidinh.J2EE_Exam.models.Appointment;
import com.exam.taidinh.J2EE_Exam.models.AppointmentForm;
import com.exam.taidinh.J2EE_Exam.models.Doctor;
import com.exam.taidinh.J2EE_Exam.models.Patient;
import com.exam.taidinh.J2EE_Exam.repositories.AppointmentRepository;
import com.exam.taidinh.J2EE_Exam.repositories.DoctorRepository;
import com.exam.taidinh.J2EE_Exam.repositories.PatientRepository;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/enroll")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentController(
        AppointmentRepository appointmentRepository,
        DoctorRepository doctorRepository,
        PatientRepository patientRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping("/doctors/{id}")
    public String appointmentForm(@PathVariable Long id, Model model) {
        Doctor doctor = findDoctor(id);
        AppointmentForm appointmentForm = new AppointmentForm();
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointmentForm", appointmentForm);
        return "appointments/enroll";
    }

    @PostMapping("/doctors/{id}")
    public String createAppointment(
        @PathVariable Long id,
        @Valid @ModelAttribute("appointmentForm") AppointmentForm appointmentForm,
        BindingResult bindingResult,
        Principal principal,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Doctor doctor = findDoctor(id);
        Patient patient = findPatient(principal.getName());

        if (!bindingResult.hasErrors()
            && appointmentRepository.existsByPatientAndDoctorAndAppointmentDate(
                patient,
                doctor,
                appointmentForm.getAppointmentDate()
            )) {
            bindingResult.rejectValue(
                "appointmentDate",
                "duplicate.appointmentDate",
                "Bạn đã đặt lịch với bác sĩ này vào ngày đã chọn"
            );
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("doctor", doctor);
            return "appointments/enroll";
        }

        appointmentRepository.save(new Appointment(patient, doctor, appointmentForm.getAppointmentDate()));
        redirectAttributes.addFlashAttribute("successMessage", "Đặt lịch khám thành công.");
        return "redirect:/enroll/my-appointments";
    }

    @GetMapping("/my-appointments")
    public String myAppointments(Principal principal, Model model) {
        model.addAttribute(
            "appointments",
            appointmentRepository.findByPatientUsernameOrderByAppointmentDateAsc(principal.getName())
        );
        return "appointments/my-appointments";
    }

    private Doctor findDoctor(Long id) {
        return doctorRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bác sĩ"));
    }

    private Patient findPatient(String username) {
        return patientRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bệnh nhân"));
    }
}
