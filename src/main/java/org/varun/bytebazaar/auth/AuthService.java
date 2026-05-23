package org.varun.bytebazaar.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
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
    private final SecureRandom secureRandom = new SecureRandom();

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
        return tokens.findByTokenHash(hash(rawToken))
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                .map(AuthToken::getUser)
                .orElse(null);
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            tokens.deleteByTokenHash(hash(rawToken));
        }
    }

    private AuthResponse issueToken(UserAccount user) {
        String rawToken = newToken();
        AuthToken token = new AuthToken();
        token.setTokenHash(hash(rawToken));
        token.setUser(user);
        token.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        tokens.save(token);
        return new AuthResponse(rawToken, UserResponse.from(user));
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
