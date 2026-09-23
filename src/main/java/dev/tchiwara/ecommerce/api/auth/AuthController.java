package dev.tchiwara.ecommerce.api.auth;

import dev.tchiwara.ecommerce.api.auth.dtos.JwtResponse;
import dev.tchiwara.ecommerce.api.auth.dtos.LoginRequest;
import dev.tchiwara.ecommerce.api.user.UserMapper;
import dev.tchiwara.ecommerce.api.user.UserRepository;
import dev.tchiwara.ecommerce.api.user.dtos.UserResponseDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        var user= userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        var cookie=new Cookie("refreshToken",refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(604800); // 7d
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponse(accessToken));
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


