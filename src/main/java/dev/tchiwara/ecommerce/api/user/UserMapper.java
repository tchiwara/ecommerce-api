package dev.tchiwara.ecommerce.api.user;

import dev.tchiwara.ecommerce.api.user.dtos.UserRegisterRequestDTO;
import dev.tchiwara.ecommerce.api.user.dtos.UserResponseDTO;
import dev.tchiwara.ecommerce.api.user.dtos.UserUpdateRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel="spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(UserRegisterRequestDTO userRegisterRequestDTO);

    UserResponseDTO toDto(User user);

    void updateUser(UserUpdateRequestDTO userUpdateRequestDTO, @MappingTarget User user);

}
