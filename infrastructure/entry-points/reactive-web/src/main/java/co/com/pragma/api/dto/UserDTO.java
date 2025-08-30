package co.com.pragma.api.dto;

import co.com.pragma.model.role.Role;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserDTO(
        String userId,
        String name,
        String lastName,
        LocalDate birthDate,
        String address,
        String phoneNumber,
        String email,
        BigDecimal baseSalary,
        String identityDocument,
        Role role
) {}
