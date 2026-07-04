package com.autoblog.identity.application;

import com.autoblog.identity.domain.UserAccountStatus;
import com.autoblog.identity.infrastructure.UserAccountEntity;
import com.autoblog.identity.infrastructure.UserAccountJpaRepository;
import com.autoblog.security.CurrentUser;
import com.autoblog.security.JwtTokenService;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserAccountJpaRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final EmailNormalizer emailNormalizer;
    private final CurrentUser currentUser;

    public AuthService(
            UserAccountJpaRepository users,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            EmailNormalizer emailNormalizer,
            CurrentUser currentUser
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.emailNormalizer = emailNormalizer;
        this.currentUser = currentUser;
    }

    @Transactional
    public AuthView register(String email, String password, String displayName) {
        String normalizedEmail = emailNormalizer.normalize(email);
        validateEmail(normalizedEmail);
        if (users.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        UserAccountEntity user = users.save(new UserAccountEntity(
                UUID.randomUUID(),
                normalizedEmail,
                passwordEncoder.encode(password),
                trimToNull(displayName),
                UserAccountStatus.ACTIVE
        ));

        return authView(user);
    }

    @Transactional(readOnly = true)
    public AuthView login(String email, String password) {
        String normalizedEmail = emailNormalizer.normalize(email);
        validateEmail(normalizedEmail);
        UserAccountEntity user = users.findByEmail(normalizedEmail)
                .filter(candidate -> candidate.getStatus() == UserAccountStatus.ACTIVE)
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        return authView(user);
    }

    @Transactional(readOnly = true)
    public UserView currentUser() {
        UserAccountEntity user = users.findById(currentUser.requireUserId())
                .filter(candidate -> candidate.getStatus() == UserAccountStatus.ACTIVE)
                .orElseThrow(InvalidCredentialsException::new);
        return toView(user);
    }

    private AuthView authView(UserAccountEntity user) {
        return new AuthView(
                jwtTokenService.createToken(user.getId(), user.getEmail()),
                TOKEN_TYPE,
                jwtTokenService.expiresInSeconds(),
                toView(user)
        );
    }

    private UserView toView(UserAccountEntity user) {
        return new UserView(user.getId(), user.getEmail(), user.getDisplayName());
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidAuthRequestException("email", "must be a well-formed email address");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
