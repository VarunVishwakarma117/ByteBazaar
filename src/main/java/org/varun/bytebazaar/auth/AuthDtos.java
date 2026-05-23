package org.varun.bytebazaar.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.varun.bytebazaar.users.Role;
import org.varun.bytebazaar.users.UserAccount;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @Size(min = 6, message = "Password must be at least 6 characters") String password) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record UserResponse(Long id, String name, String email, Role role) {
        static UserResponse from(UserAccount user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
        }
    }

    public record AuthResponse(String token, UserResponse user) {
    }
}
