package dev.tchiwara.ecommerce.api.user;

import dev.tchiwara.ecommerce.api.user.dtos.UserRegisterRequestDTO;
import dev.tchiwara.ecommerce.api.user.dtos.UserResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private  final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid @RequestBody UserRegisterRequestDTO userRegisterRequestDTO,
            UriComponentsBuilder uriBuilder
            ){

        UserResponseDTO response=userService.registerUser(userRegisterRequestDTO);
        var uri=uriBuilder
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedModel<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page
    ){
        Page<UserResponseDTO> userPage=userService.getAllUsers(page);
        return ResponseEntity.ok(new PagedModel<>(userPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(userService.getUserById(id));
    }

}
