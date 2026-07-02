package com.novaerp.auth.controller;

import com.novaerp.auth.dto.UserProfile;
import com.novaerp.auth.entity.User.UserStatus;
import com.novaerp.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User management REST endpoints — admin only.
 */
@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /users — list all users (admin only) */
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** GET /users/tenant/{tenantId} — list users for a tenant */
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<UserProfile>> getUsersByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(userService.getUsersByTenant(tenantId));
    }

    /** GET /users/{id} — get user by ID */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /** POST /users — create a new user (admin only) */
    @PostMapping
    public ResponseEntity<UserProfile> createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String email,
            @RequestParam String tenantId,
            @RequestParam(required = false) List<String> roles) {
        UserProfile user = userService.createUser(username, password, email, tenantId, roles);
        return ResponseEntity.status(201).body(user);
    }

    /** PUT /users/{id} — update user */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateUser(
            @PathVariable Long id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) UserStatus status) {
        return ResponseEntity.ok(userService.updateUser(id, email, status));
    }

    /** PUT /users/{id}/roles — assign roles to user */
    @PutMapping("/{id}/roles")
    public ResponseEntity<UserProfile> assignRoles(
            @PathVariable Long id,
            @RequestBody List<String> roleNames) {
        return ResponseEntity.ok(userService.assignRoles(id, roleNames));
    }

    /** DELETE /users/{id} — deactivate user */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
