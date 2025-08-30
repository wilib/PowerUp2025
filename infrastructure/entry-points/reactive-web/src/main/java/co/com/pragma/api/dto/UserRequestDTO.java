package co.com.pragma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserRequestDTO {
    @Schema(example = "Juan")
    private String name;

    @Schema(example = "Pérez")
    private String lastName;

    @Schema(example = "1990-05-12")
    private LocalDate birthDate;

    @Schema(example = "Calle 123 #45-67")
    private String address;

    @Schema(example = "3001234567")
    private String phoneNumber;

    @Schema(example = "juan.perez@example.com")
    private String email;

    @Schema(example = "2500000")
    private BigDecimal baseSalary;

    @Schema(example = "CC123456789")
    private String identityDocument;

    @Schema(example = "{\"roleId\": \"user\"}")
    private RoleDTO role;

    @Data
    public static class RoleDTO {
        @Schema(example = "user")
        private String roleId;
    }
}
