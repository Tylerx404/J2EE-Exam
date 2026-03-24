package com.exam.taidinh.J2EE_Exam.models;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class AppointmentForm {

    @NotNull(message = "Vui lòng chọn ngày khám")
    @FutureOrPresent(message = "Ngày khám phải từ hôm nay trở đi")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointmentDate;

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
}
