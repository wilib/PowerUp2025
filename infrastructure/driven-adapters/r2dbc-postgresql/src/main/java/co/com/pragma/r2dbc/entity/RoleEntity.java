package co.com.pragma.r2dbc.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("roles")
public class RoleEntity {
    @Id
    private String roleId;
    private String roleName;
}
