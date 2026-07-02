package com.novaerp.auth.service;

import com.novaerp.auth.dto.*;
import com.novaerp.auth.entity.RefreshToken;
import com.novaerp.auth.entity.Role;
import com.novaerp.auth.entity.User;
import com.novaerp.auth.entity.User.UserStatus;
import com.novaerp.auth.repository.RefreshTokenRepository;
import com.novaerp.auth.repository.RoleRepository;
import com.novaerp.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core authentication service — handles login, logout, registration, and token refresh.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ==================== LOGIN ====================

    /** Authenticate user and return JWT tokens */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findWithRolesByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Account is " + user.getStatus());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getTenantId(), roleNames);
        String refreshToken = jwtService.generateRefreshToken(String.valueOf(user.getId()), user.getTenantId());

        // Persist refresh token
        RefreshToken rt = new RefreshToken();
        rt.setUserId(String.valueOf(user.getId()));
        rt.setToken(refreshToken);
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000));
        refreshTokenRepository.save(rt);

        LoginResponse response = new LoginResponse(accessToken, refreshToken, jwtService.getAccessTokenExpiration() / 1000, roleNames);
        response.setUser(toUserProfile(user));
        return response;
    }

    // ==================== LOGOUT ====================

    /** Logout — revoke all refresh tokens for the user */
    @Transactional
    public void logout(String refreshToken) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);
        if (tokenOpt.isPresent()) {
            RefreshToken rt = tokenOpt.get();
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.info("User {} logged out", rt.getUserId());
        }
    }

    // ==================== REFRESH TOKEN ====================

    /** Exchange a valid refresh token for new access + refresh tokens */
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(String currentRefreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(currentRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!storedToken.isValid()) {
            throw new IllegalStateException("Refresh token has expired or been revoked");
        }

        User user = userRepository.findById(Long.parseLong(storedToken.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));;

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Revoke old refresh token and issue a new one
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), user.getTenantId(), roleNames);
        String newRefreshToken = jwtService.generateRefreshToken(String.valueOf(user.getId()), user.getTenantId());

        RefreshToken newRt = new RefreshToken();
        newRt.setUserId(storedToken.getUserId());
        newRt.setToken(newRefreshToken);
        newRt.setExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000));
        refreshTokenRepository.save(newRt);

        LoginResponse response = new LoginResponse(newAccessToken, newRefreshToken, jwtService.getAccessTokenExpiration() / 1000, roleNames);
        return response;
    }

    // ==================== VERIFY TOKEN ====================

    /** Verify a token and extract the user */
    @Transactional(readOnly = true)
    public Optional<User> verifyAndFetchUser(String token) {
        if (!jwtService.validateToken(token)) {
            return Optional.empty();
        }
        String username = jwtService.extractSubject(token);
        return userRepository.findWithRolesByUsername(username);
    }

    // ==================== REGISTER ====================

    /** Register a new user */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setTenantId(request.getTenantId());
        user.setStatus(UserStatus.ACTIVE);

        // Assign roles if provided
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + roleName));
                user.getRoles().add(role);
            }
        }

        User savedUser = userRepository.save(user);
        log.info("New user registered: {} (tenant={})", request.getUsername(), request.getTenantId());

        RegisterResponse response = new RegisterResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getStatus().name());
        return response;
    }

    // ==================== HELPERS ====================

    private UserProfile toUserProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setStatus(user.getStatus().name());
        profile.setTenantId(user.getTenantId());
        profile.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        return profile;
    }

}
