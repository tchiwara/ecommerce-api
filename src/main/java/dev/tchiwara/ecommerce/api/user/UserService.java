package dev.tchiwara.ecommerce.api.user;

import dev.tchiwara.ecommerce.api.global.ResourceNotFoundException;
import dev.tchiwara.ecommerce.api.user.dtos.UserRegisterRequestDTO;
import dev.tchiwara.ecommerce.api.user.dtos.UserResponseDTO;
import dev.tchiwara.ecommerce.api.user.dtos.UserUpdateRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final  UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO registerUser (UserRegisterRequestDTO userRegisterRequestDTO){

        User user=userMapper.toEntity(userRegisterRequestDTO);
        user.setPasswordHash(passwordEncoder.encode(userRegisterRequestDTO.getPassword()));
        var savedUser= userRepository.save(user);

        return  userMapper.toDto(savedUser);

    }

    public Page<UserResponseDTO> getAllUsers(
            int page
    ){
        Pageable pageable= PageRequest.of(page,10, Sort.by("name"));
        Page<User> users=userRepository.findAll(pageable);

        return users
                .map(userMapper::toDto);
    }

    public UserResponseDTO getUserById(Long id){
        var user=userRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User with id " + id + " not found")
                        );
        return userMapper.toDto(user);
    }

    public UserResponseDTO updateUser(
            UserUpdateRequestDTO userUpdateRequestDTO,
            Long id
    ){
        var user=userRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User with id " + id + " not found")
                );

        userMapper.updateUser(userUpdateRequestDTO,user);
        userRepository.save(user);
        return userMapper.toDto(user);

    }

}
