package com.exam.taidinh.J2EE_Exam.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PatientRegistrationForm {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 60, message = "Username từ 3 đến 60 ký tự")
    private String username = "";

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, max = 100, message = "Password tối thiểu 6 ký tự")
    private String password = "";

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 120, message = "Email tối đa 120 ký tự")
    private String email = "";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
