package com.exam.taidinh.J2EE_Exam.controllers;

import com.exam.taidinh.J2EE_Exam.models.Department;
import com.exam.taidinh.J2EE_Exam.models.Doctor;
import com.exam.taidinh.J2EE_Exam.models.DoctorForm;
import com.exam.taidinh.J2EE_Exam.repositories.DepartmentRepository;
import com.exam.taidinh.J2EE_Exam.repositories.DoctorRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/admin/doctors")
public class AdminDoctorController {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    public AdminDoctorController(DoctorRepository doctorRepository, DepartmentRepository departmentRepository) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
        return "admin/doctor-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        DoctorForm doctorForm = new DoctorForm();
        doctorForm.setImage("/images/doctor-placeholder.svg");
        return renderForm(model, doctorForm, true, null);
    }

    @PostMapping
    public String createDoctor(
        @Valid @ModelAttribute("doctorForm") DoctorForm doctorForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Department department = resolveDepartment(doctorForm.getDepartmentId(), bindingResult);
        if (bindingResult.hasErrors()) {
            return renderForm(model, doctorForm, true, null);
        }

        Doctor doctor = new Doctor(
            doctorForm.getName(),
            doctorForm.getImage(),
            doctorForm.getSpecialty(),
            department
        );
        doctorRepository.save(doctor);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm bác sĩ thành công.");
        return "redirect:/admin/doctors";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Doctor doctor = findDoctor(id);
        DoctorForm doctorForm = new DoctorForm();
        doctorForm.setName(doctor.getName());
        doctorForm.setImage(doctor.getImage());
        doctorForm.setSpecialty(doctor.getSpecialty());
        doctorForm.setDepartmentId(doctor.getDepartment().getId());

        return renderForm(model, doctorForm, false, id);
    }

    @PostMapping("/{id}")
    public String updateDoctor(
        @PathVariable Long id,
        @Valid @ModelAttribute("doctorForm") DoctorForm doctorForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Doctor doctor = findDoctor(id);
        Department department = resolveDepartment(doctorForm.getDepartmentId(), bindingResult);
        if (bindingResult.hasErrors()) {
            return renderForm(model, doctorForm, false, id);
        }

        doctor.setName(doctorForm.getName());
        doctor.setImage(doctorForm.getImage());
        doctor.setSpecialty(doctorForm.getSpecialty());
        doctor.setDepartment(department);
        doctorRepository.save(doctor);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bác sĩ thành công.");
        return "redirect:/admin/doctors";
    }

    @PostMapping("/{id}/delete")
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Doctor doctor = findDoctor(id);
        doctorRepository.delete(doctor);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa bác sĩ thành công.");
        return "redirect:/admin/doctors";
    }

    private String renderForm(Model model, DoctorForm doctorForm, boolean createMode, Long doctorId) {
        List<Department> departments = departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        model.addAttribute("doctorForm", doctorForm);
        model.addAttribute("departments", departments);
        model.addAttribute("createMode", createMode);
        model.addAttribute("doctorId", doctorId);
        return "admin/doctor-form";
    }

    private Doctor findDoctor(Long id) {
        return doctorRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bác sĩ"));
    }

    private Department resolveDepartment(Long departmentId, BindingResult bindingResult) {
        if (departmentId == null) {
            return null;
        }

        return departmentRepository.findById(departmentId)
            .orElseGet(() -> {
                bindingResult.rejectValue("departmentId", "invalid.departmentId", "Khoa không tồn tại");
                return null;
            });
    }
}
