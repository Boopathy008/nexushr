package com.nexushr.controller;

import com.nexushr.domain.entity.Employee;
import com.nexushr.domain.entity.User;
import com.nexushr.repository.EmployeeRepository;
import com.nexushr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TempPasswordController {
    
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/api/v1/temp-update-passwords")
    public String updatePasswords() {
        try {
            // Update Manager password
            userRepository.findByUsername("manager").ifPresent(manager -> {
                manager.setPassword(passwordEncoder.encode("Manager@1234"));
                userRepository.save(manager);
            });

            // Update all employee passwords
            List<Employee> employees = employeeRepository.findAll();
            for (Employee emp : employees) {
                User user = emp.getUser();
                if (user != null && emp.getFirstName() != null) {
                    String newPass = emp.getFirstName().toLowerCase() + "123";
                    user.setPassword(passwordEncoder.encode(newPass));
                    userRepository.save(user);
                }
            }
            
            return "Passwords updated successfully!";
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.toString()).append("\n");
            for(StackTraceElement el : e.getStackTrace()) {
                sb.append(el.toString()).append("\n");
            }
            return sb.toString();
        }
    }
}
