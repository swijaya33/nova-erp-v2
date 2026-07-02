package com.novaerp.auth.service;

import com.novaerp.auth.dto.UserProfile;
import com.novaerp.auth.entity.Role;
import com.novaerp.auth.entity.User;
import com.novaerp.auth.entity.User.UserStatus;
import com.novaerp.auth.repository.RoleRepository;
import com.novaerp.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User management service — CRUD operations with role assignment.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    // ==================== READ ====================

    @Transactional(readOnly = true)
    public UserProfile getUserById(Long id) {
        User user = userRepository.findWithRolesByUsername(String.valueOf(id))
                .or(() -> userRepository.findById(id).map(u -> u))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toUserProfile(user);
    }

    @Transactional(readOnly = true)
    public UserProfile getUserByUsername(String username) {
        User user = userRepository.findWithRolesByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return toUserProfile(user);
    }

    @Transactional(readOnly = true)
    public List<UserProfile> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserProfile)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfile> getUsersByTenant(String tenantId) {
        return userRepository.findByTenantId(tenantId).stream()
                .map(this::toUserProfile)
                .collect(Collectors.toList());
    }

    // ==================== CREATE ====================

    /** Create a new user with optional roles */
    @Transactional
    public UserProfile createUser(String username, String password, String email, String tenantId, List<String> roleNames) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setTenantId(tenantId);
        user.setStatus(UserStatus.ACTIVE);

        if (roleNames != null && !roleNames.isEmpty()) {
            for (String roleName : roleNames) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + roleName));
                user.getRoles().add(role);
            }
        }

        User savedUser = userRepository.save(user);
        log.info("Admin created user: {} (tenant={})", username, tenantId);
        return toUserProfile(savedUser);
    }

    // ==================== UPDATE ====================

    /** Update user profile fields */
    @Transactional
    public UserProfile updateUser(Long id, String email, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (email != null) {
            user.setEmail(email);
        }
        if (status != null) {
            user.setStatus(status);
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated user {}: status={}", id, status);
        return toUserProfile(updatedUser);
    }

    /** Assign roles to a user */
    @Transactional
    public UserProfile assignRoles(Long userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + roleName));
            if (!user.getRoles().contains(role)) {
                user.getRoles().add(role);
            }
        }

        User updatedUser = userRepository.save(user);
        log.info("Assigned roles {} to user {}", roleNames, userId);
        return toUserProfile(updatedUser);
    }

    // ==================== DELETE ====================

    /** Soft-delete a user by locking the account */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("Deactivated user {}", id);
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
