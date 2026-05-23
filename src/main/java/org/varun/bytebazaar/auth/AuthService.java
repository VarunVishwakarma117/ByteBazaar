package org.varun.bytebazaar.auth;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.varun.bytebazaar.auth.AuthDtos.AuthResponse;
import org.varun.bytebazaar.auth.AuthDtos.LoginRequest;
import org.varun.bytebazaar.auth.AuthDtos.RegisterRequest;
import org.varun.bytebazaar.auth.AuthDtos.UserResponse;
import org.varun.bytebazaar.users.Role;
import org.varun.bytebazaar.users.UserAccount;
import org.varun.bytebazaar.users.UserRepository;

@Service
public class AuthService {
    private final UserRepository users;
    private final AuthTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository users, AuthTokenRepository tokens, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        UserAccount user = new UserAccount();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        users.save(user);
        return issueToken(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public UserAccount authenticate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        return tokens.findByToken(rawToken)
                .map(AuthToken::getUser)
                .orElse(null);
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            tokens.deleteByToken(rawToken);
        }
    }

    private AuthResponse issueToken(UserAccount user) {
        String rawToken = UUID.randomUUID().toString();
        AuthToken token = new AuthToken();
        token.setToken(rawToken);
        token.setUser(user);
        tokens.save(token);
        return new AuthResponse(rawToken, UserResponse.from(user));
    }
}
