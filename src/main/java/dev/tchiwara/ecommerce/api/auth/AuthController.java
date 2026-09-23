package dev.tchiwara.ecommerce.api.auth;

import dev.tchiwara.ecommerce.api.auth.dtos.JwtResponse;
import dev.tchiwara.ecommerce.api.auth.dtos.LoginRequest;
import dev.tchiwara.ecommerce.api.user.UserMapper;
import dev.tchiwara.ecommerce.api.user.UserRepository;
import dev.tchiwara.ecommerce.api.user.dtos.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> loginRequest(
            @Valid @RequestBody LoginRequest loginRequest
    ){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        var user= userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        var token = jwtService.generateToken(user);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    /*the @AuthenticationPrincipal annotation is used to inject the currently authenticated
    user's principal directly into controller handler methods*/
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal Long userId){

        var user=userRepository.findById(userId).orElse(null);
        if(user==null) return ResponseEntity.notFound().build();
        var userDto=userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }

}


