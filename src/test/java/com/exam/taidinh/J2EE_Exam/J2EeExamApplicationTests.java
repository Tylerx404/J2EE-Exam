package com.exam.taidinh.J2EE_Exam;

import com.exam.taidinh.J2EE_Exam.models.Department;
import com.exam.taidinh.J2EE_Exam.models.Doctor;
import com.exam.taidinh.J2EE_Exam.repositories.DepartmentRepository;
import com.exam.taidinh.J2EE_Exam.repositories.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

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

	@Test
	void contextLoads() {
	}

	@Test
	void homePageShouldRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/"))
			.andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Danh sách bác sĩ")));
	}

	@Test
	void adminDoctorListShouldRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/admin/doctors"))
			.andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Quản lý bác sĩ")));
	}

	@Test
	void adminShouldCreateDoctorSuccessfully() throws Exception {
		long beforeCount = doctorRepository.count();
		Department department = departmentRepository.findAll().getFirst();

		mockMvc.perform(post("/admin/doctors")
				.param("name", "BS. Test Create")
				.param("specialty", "Test Specialty")
				.param("image", "/images/doctor-placeholder.svg")
				.param("departmentId", department.getId().toString()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/admin/doctors"));

		org.assertj.core.api.Assertions.assertThat(doctorRepository.count()).isEqualTo(beforeCount + 1);
		org.assertj.core.api.Assertions.assertThat(doctorRepository.findAll())
			.anyMatch(doctor -> "BS. Test Create".equals(doctor.getName()));
	}

	@Test
	void adminShouldUpdateDoctorSuccessfully() throws Exception {
		Doctor doctor = doctorRepository.findAll().getFirst();
		Department department = departmentRepository.findAll().getLast();

		mockMvc.perform(post("/admin/doctors/" + doctor.getId())
				.param("name", "BS. Updated")
				.param("specialty", "Updated Specialty")
				.param("image", "/images/doctor-placeholder.svg")
				.param("departmentId", department.getId().toString()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/admin/doctors"));

		Doctor updatedDoctor = doctorRepository.findById(doctor.getId()).orElseThrow();
		org.assertj.core.api.Assertions.assertThat(updatedDoctor.getName()).isEqualTo("BS. Updated");
		org.assertj.core.api.Assertions.assertThat(updatedDoctor.getSpecialty()).isEqualTo("Updated Specialty");
		org.assertj.core.api.Assertions.assertThat(updatedDoctor.getDepartment().getId()).isEqualTo(department.getId());
	}

	@Test
	void adminShouldDeleteDoctorSuccessfully() throws Exception {
		Doctor doctor = doctorRepository.findAll().getFirst();
		long beforeCount = doctorRepository.count();

		mockMvc.perform(post("/admin/doctors/" + doctor.getId() + "/delete"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/admin/doctors"));

		org.assertj.core.api.Assertions.assertThat(doctorRepository.count()).isEqualTo(beforeCount - 1);
		org.assertj.core.api.Assertions.assertThat(doctorRepository.findById(doctor.getId())).isEmpty();
	}

}
