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
    // Contains main authentication logic used by controller and token filter.
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
        // Do not allow two accounts with same email.
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Create user account from registration form.
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
        // Find user by email and check password.
        UserAccount user = users.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public UserAccount authenticate(String rawToken) {
        // Used by filter to convert token into logged-in user.
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        return tokens.findByToken(rawToken)
                .map(AuthToken::getUser)
                .orElse(null);
    }

    @Transactional
    public void logout(String rawToken) {
        // Delete token on logout.
        if (rawToken != null && !rawToken.isBlank()) {
            tokens.deleteByToken(rawToken);
        }
    }

    private AuthResponse issueToken(UserAccount user) {
        // Create simple random token and save it in database.
        String rawToken = UUID.randomUUID().toString();
        AuthToken token = new AuthToken();
        token.setToken(rawToken);
        token.setUser(user);
        tokens.save(token);
        return new AuthResponse(rawToken, UserResponse.from(user));
    }
}
