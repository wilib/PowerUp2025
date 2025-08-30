package co.com.pragma.model.user;
import co.com.pragma.model.role.Role;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private String userId;
    private String name;
    private String lastName;
    private LocalDate birthDate;
    private String address;
    private String phoneNumber;
    private String email;
    private BigDecimal baseSalary;
    private String identityDocument;
    private Role role;
}
