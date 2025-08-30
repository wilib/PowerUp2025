package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.model.user.User;

public interface UserDTOMapper {
    User toDomain(UserDTO dto);
    UserDTO toDTO(User domain);
}
