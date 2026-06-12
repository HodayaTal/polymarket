package com.example.polymarket.service;

import com.example.polymarket.config.PolymarketProperties;
import com.example.polymarket.domain.AppUser;
import com.example.polymarket.domain.AuthToken;
import com.example.polymarket.domain.UserRole;
import com.example.polymarket.domain.WalletMovement;
import com.example.polymarket.domain.WalletMovementType;
import com.example.polymarket.dto.AuthResponse;
import com.example.polymarket.dto.LoginRequest;
import com.example.polymarket.dto.RegisterRequest;
import com.example.polymarket.exception.ApiException;
import com.example.polymarket.repository.AppUserRepository;
import com.example.polymarket.repository.AuthTokenRepository;
import com.example.polymarket.repository.WalletMovementRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final WalletMovementRepository walletMovementRepository;
    private final PasswordEncoder passwordEncoder;
    private final PolymarketProperties properties;
    private final ApiMapper mapper;

    public AuthService(
            AppUserRepository appUserRepository,
            AuthTokenRepository authTokenRepository,
            WalletMovementRepository walletMovementRepository,
            PasswordEncoder passwordEncoder,
            PolymarketProperties properties,
            ApiMapper mapper
    ) {
        this.appUserRepository = appUserRepository;
        this.authTokenRepository = authTokenRepository;
        this.walletMovementRepository = walletMovementRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        ensureStrongPassword(request.password());

        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            throw ApiException.conflict("Username already exists.");
        }
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("Email already exists.");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setWalletBalance(MoneyMath.money(properties.getInitialWalletPoints()));
        user.setRole(resolveRole(request.adminCode()));
        appUserRepository.save(user);

        WalletMovement movement = new WalletMovement();
        movement.setUser(user);
        movement.setType(WalletMovementType.INITIAL_GRANT);
        movement.setAmount(user.getWalletBalance());
        movement.setBalanceAfter(user.getWalletBalance());
        movement.setDescription("Initial virtual points grant");
        walletMovementRepository.save(movement);

        String token = createToken(user);
        return new AuthResponse(token, mapper.toUserResponse(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AppUser user = findByIdentifier(request.identifier());
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid username/email or password.");
        }
        String token = createToken(user);
        return new AuthResponse(token, mapper.toUserResponse(user));
    }

    @Transactional(readOnly = true)
    public AppUser requireUser(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        String tokenHash = hashToken(token);
        return authTokenRepository.findActiveToken(tokenHash, Instant.now())
                .map(AuthToken::getUser)
                .orElseThrow(() -> ApiException.unauthorized("Missing or invalid login token."));
    }

    @Transactional(readOnly = true)
    public AppUser requireAdmin(String authorizationHeader) {
        AppUser user = requireUser(authorizationHeader);
        if (user.getRole() != UserRole.ADMIN) {
            throw ApiException.forbidden("Admin privileges are required.");
        }
        return user;
    }

    private AppUser findByIdentifier(String identifier) {
        String value = identifier.trim();
        if (value.contains("@")) {
            return appUserRepository.findByEmailIgnoreCase(value.toLowerCase(Locale.ROOT))
                    .orElseThrow(() -> ApiException.unauthorized("Invalid username/email or password."));
        }
        return appUserRepository.findByUsernameIgnoreCase(value)
                .orElseThrow(() -> ApiException.unauthorized("Invalid username/email or password."));
    }

    private UserRole resolveRole(String adminCode) {
        boolean firstUser = appUserRepository.count() == 0;
        boolean adminCodeMatches = properties.getAdminRegistrationCode() != null
                && !properties.getAdminRegistrationCode().isBlank()
                && properties.getAdminRegistrationCode().equals(adminCode);
        return firstUser || adminCodeMatches ? UserRole.ADMIN : UserRole.USER;
    }

    private void ensureStrongPassword(String password) {
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!(hasLower && hasUpper && hasDigit)) {
            throw ApiException.badRequest("Password must include lowercase, uppercase, and numeric characters.");
        }
    }

    private String createToken(AppUser user) {
        String token = UUID.randomUUID() + "." + UUID.randomUUID();
        AuthToken authToken = new AuthToken();
        authToken.setUser(user);
        authToken.setTokenHash(hashToken(token));
        authToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        authTokenRepository.save(authToken);
        return token;
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw ApiException.unauthorized("Missing or invalid Authorization header.");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
