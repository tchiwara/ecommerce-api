package dev.tchiwara.ecommerce.api.user;

import dev.tchiwara.ecommerce.api.auth.dtos.RegistrationResponseDTO;
import dev.tchiwara.ecommerce.api.user.dtos.UserRegisterRequestDTO;
import dev.tchiwara.ecommerce.api.user.dtos.UserResponseDTO;
import dev.tchiwara.ecommerce.api.user.dtos.UserUpdateRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private  final UserService userService;

    @PostMapping
    public ResponseEntity<RegistrationResponseDTO> registerUser(
            @Valid @RequestBody UserRegisterRequestDTO userRegisterRequestDTO) {
        return ResponseEntity.accepted().body(userService.registerUser(userRegisterRequestDTO));
    }

    @GetMapping
    public ResponseEntity<PagedModel<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page
    ){
        Page<UserResponseDTO> userPage=userService.getAllUsers(page);
        return ResponseEntity.ok(new PagedModel<>(userPage));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Valid @RequestBody UserUpdateRequestDTO userUpdateRequestDTO,
            @PathVariable Long id
            ){
        var response=userService.updateUser(userUpdateRequestDTO,id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
