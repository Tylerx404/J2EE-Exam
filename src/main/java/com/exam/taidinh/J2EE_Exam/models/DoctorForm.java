package com.exam.taidinh.J2EE_Exam.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DoctorForm {

    @NotBlank(message = "Tên bác sĩ không được để trống")
    @Size(max = 120, message = "Tên bác sĩ tối đa 120 ký tự")
    private String name = "";

    @NotBlank(message = "Hình ảnh không được để trống")
    @Size(max = 255, message = "Đường dẫn hình ảnh tối đa 255 ký tự")
    private String image = "";

    @NotBlank(message = "Chuyên khoa không được để trống")
    @Size(max = 120, message = "Chuyên khoa tối đa 120 ký tự")
    private String specialty = "";

    @NotNull(message = "Vui lòng chọn khoa")
    private Long departmentId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
