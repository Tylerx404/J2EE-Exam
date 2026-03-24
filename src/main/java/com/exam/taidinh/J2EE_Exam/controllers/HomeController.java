package com.exam.taidinh.J2EE_Exam.controllers;

import com.exam.taidinh.J2EE_Exam.models.Doctor;
import com.exam.taidinh.J2EE_Exam.repositories.DoctorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private static final int PAGE_SIZE = 5;

    private final DoctorRepository doctorRepository;

    public HomeController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping({"/", "/home", "/courses"})
    public String home(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "") String keyword,
        Model model
    ) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        int currentPage = Math.max(page, 0);
        PageRequest pageRequest = PageRequest.of(currentPage, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id"));
        Page<Doctor> doctorPage = normalizedKeyword.isBlank()
            ? doctorRepository.findAll(pageRequest)
            : doctorRepository.findByNameContainingIgnoreCase(normalizedKeyword, pageRequest);
        long totalDoctors = doctorPage.getTotalElements();
        int lastPage = totalDoctors == 0 ? 0 : (int) Math.ceil((double) totalDoctors / PAGE_SIZE) - 1;
        if (currentPage > lastPage) {
            currentPage = lastPage;
            pageRequest = PageRequest.of(currentPage, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id"));
            doctorPage = normalizedKeyword.isBlank()
                ? doctorRepository.findAll(pageRequest)
                : doctorRepository.findByNameContainingIgnoreCase(normalizedKeyword, pageRequest);
        }

        model.addAttribute("doctorPage", doctorPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", doctorPage.getTotalPages());
        model.addAttribute("previousPage", Math.max(currentPage - 1, 0));
        model.addAttribute("nextPage", currentPage + 1);
        model.addAttribute("keyword", normalizedKeyword);

        return "home";
    }
}
