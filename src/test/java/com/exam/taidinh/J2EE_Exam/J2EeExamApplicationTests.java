package com.exam.taidinh.J2EE_Exam;

import com.exam.taidinh.J2EE_Exam.models.Appointment;
import com.exam.taidinh.J2EE_Exam.models.Department;
import com.exam.taidinh.J2EE_Exam.models.Doctor;
import com.exam.taidinh.J2EE_Exam.models.Patient;
import com.exam.taidinh.J2EE_Exam.repositories.AppointmentRepository;
import com.exam.taidinh.J2EE_Exam.repositories.DepartmentRepository;
import com.exam.taidinh.J2EE_Exam.repositories.DoctorRepository;
import com.exam.taidinh.J2EE_Exam.repositories.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class J2EeExamApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void homePageShouldRenderSuccessfully() throws Exception {
        mockMvc.perform(get("/home"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Danh sách bác sĩ")));
    }

    @Test
    void coursesPageShouldBePublic() throws Exception {
        mockMvc.perform(get("/courses"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Danh sách bác sĩ")));
    }

    @Test
    void searchDoctorByNameShouldReturnMatchingResult() throws Exception {
        String doctorName = "BS. Search " + System.nanoTime();
        Department department = ensureDepartment();
        doctorRepository.save(new Doctor(doctorName, "/images/doctor-placeholder.svg", "Search Specialty", department));

        mockMvc.perform(get("/home").param("keyword", "Search"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(doctorName)));
    }

    @Test
    void registerShouldCreatePatientWithPatientRole() throws Exception {
        String username = "newpatient" + System.nanoTime();
        String email = username + "@mail.com";

        mockMvc.perform(post("/register")
                .param("username", username)
                .param("password", "Secret123")
                .param("email", email))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        Patient patient = patientRepository.findByUsername(username).orElseThrow();
        assertThat(patient.getEmail()).isEqualTo(email);
        assertThat(patient.getRoles())
            .extracting(role -> role.getName())
            .contains("PATIENT");
    }

    @Test
    void loginShouldRedirectToHomeWhenCredentialsAreValid() throws Exception {
        mockMvc.perform(formLogin().user("patient").password("Patient@123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"));
    }

    @Test
    void adminPagesShouldRedirectToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/admin/doctors"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "patient", roles = "PATIENT")
    void patientShouldNotAccessAdminPages() throws Exception {
        mockMvc.perform(get("/admin/doctors"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminDoctorListShouldRenderSuccessfully() throws Exception {
        mockMvc.perform(get("/admin/doctors"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Quản lý bác sĩ")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldCreateDoctorSuccessfully() throws Exception {
        long beforeCount = doctorRepository.count();
        Department department = ensureDepartment();

        mockMvc.perform(post("/admin/doctors")
                .param("name", "BS. Test Create")
                .param("specialty", "Test Specialty")
                .param("image", "/images/doctor-placeholder.svg")
                .param("departmentId", department.getId().toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/doctors"));

        assertThat(doctorRepository.count()).isEqualTo(beforeCount + 1);
        assertThat(doctorRepository.findAll())
            .anyMatch(doctor -> "BS. Test Create".equals(doctor.getName()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldUpdateDoctorSuccessfully() throws Exception {
        Doctor doctor = ensureDoctor();
        Department department = ensureDepartment();

        mockMvc.perform(post("/admin/doctors/" + doctor.getId())
                .param("name", "BS. Updated")
                .param("specialty", "Updated Specialty")
                .param("image", "/images/doctor-placeholder.svg")
                .param("departmentId", department.getId().toString()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/doctors"));

        Doctor updatedDoctor = doctorRepository.findById(doctor.getId()).orElseThrow();
        assertThat(updatedDoctor.getName()).isEqualTo("BS. Updated");
        assertThat(updatedDoctor.getSpecialty()).isEqualTo("Updated Specialty");
        assertThat(updatedDoctor.getDepartment().getId()).isEqualTo(department.getId());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldDeleteDoctorSuccessfully() throws Exception {
        Doctor doctor = ensureDoctor();
        long beforeCount = doctorRepository.count();

        mockMvc.perform(post("/admin/doctors/" + doctor.getId() + "/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/doctors"));

        assertThat(doctorRepository.count()).isEqualTo(beforeCount - 1);
        assertThat(doctorRepository.findById(doctor.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldNotAccessPatientEnrollPages() throws Exception {
        mockMvc.perform(get("/enroll/my-appointments"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "patient", roles = "PATIENT")
    void patientShouldCreateAppointmentSuccessfully() throws Exception {
        Doctor doctor = ensureDoctor();
        long beforeCount = appointmentRepository.count();

        mockMvc.perform(post("/enroll/doctors/" + doctor.getId())
                .param("appointmentDate", "2030-01-20"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/enroll/my-appointments"));

        assertThat(appointmentRepository.count()).isEqualTo(beforeCount + 1);
        assertThat(appointmentRepository.findByPatientUsernameOrderByAppointmentDateAsc("patient"))
            .extracting(Appointment::getDoctor)
            .extracting(Doctor::getId)
            .contains(doctor.getId());
    }

    @Test
    @WithMockUser(username = "patient", roles = "PATIENT")
    void patientShouldSeeMyAppointmentsPage() throws Exception {
        mockMvc.perform(get("/enroll/my-appointments"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("My Appointments")));
    }

    private Department ensureDepartment() {
        return departmentRepository.findAll().stream().findFirst()
            .orElseGet(() -> departmentRepository.save(new Department("Test Department")));
    }

    private Doctor ensureDoctor() {
        return doctorRepository.findAll().stream().findFirst()
            .orElseGet(() -> doctorRepository.save(
                new Doctor("BS. Seed Doctor", "/images/doctor-placeholder.svg", "Seed Specialty", ensureDepartment())
            ));
    }
}
