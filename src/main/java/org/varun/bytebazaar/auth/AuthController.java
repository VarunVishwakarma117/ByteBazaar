package org.varun.bytebazaar.auth;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.varun.bytebazaar.auth.AuthDtos.AuthResponse;
import org.varun.bytebazaar.auth.AuthDtos.LoginRequest;
import org.varun.bytebazaar.auth.AuthDtos.RegisterRequest;
import org.varun.bytebazaar.auth.AuthDtos.UserResponse;
import org.varun.bytebazaar.users.UserAccount;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    void logout(@RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.logout(extractBearer(authorization));
    }

    @GetMapping("/me")
    UserResponse me(@AuthenticationPrincipal UserAccount user) {
        return UserResponse.from(user);
    }

    private String extractBearer(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }
}
