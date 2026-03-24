package com.exam.taidinh.J2EE_Exam.config;

import com.exam.taidinh.J2EE_Exam.models.Department;
import com.exam.taidinh.J2EE_Exam.models.Doctor;
import com.exam.taidinh.J2EE_Exam.repositories.DepartmentRepository;
import com.exam.taidinh.J2EE_Exam.repositories.DoctorRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DoctorDataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    public DoctorDataInitializer(DepartmentRepository departmentRepository, DoctorRepository doctorRepository) {
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public void run(String... args) {
        if (doctorRepository.count() > 0) {
            return;
        }

        Department noiTongQuat = saveDepartment("Nội tổng quát");
        Department timMach = saveDepartment("Tim mạch");
        Department nhi = saveDepartment("Nhi");
        Department thanKinh = saveDepartment("Thần kinh");

        doctorRepository.saveAll(List.of(
            new Doctor("BS. Nguyễn Minh Anh", "/images/doctor-placeholder.svg", "Khám nội tổng quát", noiTongQuat),
            new Doctor("BS. Trần Quốc Bảo", "/images/doctor-placeholder.svg", "Điều trị tim mạch", timMach),
            new Doctor("BS. Lê Thu Hà", "/images/doctor-placeholder.svg", "Tư vấn dinh dưỡng trẻ em", nhi),
            new Doctor("BS. Phạm Đức Huy", "/images/doctor-placeholder.svg", "Chẩn đoán thần kinh", thanKinh),
            new Doctor("BS. Vũ Ngọc Lan", "/images/doctor-placeholder.svg", "Tầm soát bệnh lý nội khoa", noiTongQuat),
            new Doctor("BS. Đỗ Gia Linh", "/images/doctor-placeholder.svg", "Siêu âm tim", timMach)
        ));
    }

    private Department saveDepartment(String name) {
        return departmentRepository.findByName(name)
            .orElseGet(() -> departmentRepository.save(new Department(name)));
    }
}
