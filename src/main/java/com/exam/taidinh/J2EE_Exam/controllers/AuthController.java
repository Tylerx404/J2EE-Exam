package com.exam.taidinh.J2EE_Exam.controllers;

import com.exam.taidinh.J2EE_Exam.models.Patient;
import com.exam.taidinh.J2EE_Exam.models.PatientRegistrationForm;
import com.exam.taidinh.J2EE_Exam.models.Role;
import com.exam.taidinh.J2EE_Exam.repositories.PatientRepository;
import com.exam.taidinh.J2EE_Exam.repositories.RoleRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class AuthController {

    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
        PatientRepository patientRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.patientRepository = patientRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("patientForm", new PatientRegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerPatient(
        @Valid @ModelAttribute("patientForm") PatientRegistrationForm patientForm,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (patientRepository.existsByUsername(patientForm.getUsername())) {
            bindingResult.rejectValue("username", "duplicate.username", "Username đã tồn tại");
        }
        if (patientRepository.existsByEmail(patientForm.getEmail())) {
            bindingResult.rejectValue("email", "duplicate.email", "Email đã tồn tại");
        }
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        Role patientRole = roleRepository.findByName("PATIENT")
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Thiếu role PATIENT"));

        Patient patient = new Patient(
            patientForm.getUsername(),
            passwordEncoder.encode(patientForm.getPassword()),
            patientForm.getEmail()
        );
        patient.addRole(patientRole);
        patientRepository.save(patient);

        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công. Hãy đăng nhập để đặt lịch.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(
        @RequestParam(required = false) String error,
        @RequestParam(required = false) String logout,
        Model model
    ) {
        model.addAttribute("hasError", error != null);
        model.addAttribute("loggedOut", logout != null);
        return "auth/login";
    }
}
