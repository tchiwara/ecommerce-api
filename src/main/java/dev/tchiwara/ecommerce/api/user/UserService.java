package dev.tchiwara.ecommerce.api.user;

import dev.tchiwara.ecommerce.api.user.dtos.UserRegisterRequestDTO;
import dev.tchiwara.ecommerce.api.user.dtos.UserResponseDTO;
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

}
