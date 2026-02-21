package com.example.demo.auth;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Registers a new user with encrypted password.
     * Time Complexity: O(1) - BCrypt hashing with constant cost factor
     * Space Complexity: O(1) - single user document
     */

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Username already exists");
        }

        String role = request.getRole() != null ? request.getRole() : "USER";
        
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                role
        );

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }

    /**
     * Authenticates user and generates JWT token.
     * Time Complexity: O(1) - indexed username lookup + BCrypt verification
     * Space Complexity: O(1) - single JWT token (~256 bytes)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }

    /**
     * Retrieves all registered users (Admin endpoint).
     * Time Complexity: O(n) where n = total users
     * Space Complexity: O(n) - list of all users
     */

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(Map.of(
                "totalUsers", users.size(),
                "users", users.stream().map(u -> Map.of(
                        "id", (Object) u.getId(),
                        "username", (Object) u.getUsername(),
                        "role", (Object) u.getRole(),
                        "createdAt", (Object) u.getCreatedAt().toString()
                )).toList()
        ));
    }

    /**
     * Deletes a user by ID.
     * Time Complexity: O(1) - indexed ID lookup + delete
     * Space Complexity: O(1)
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with id: " + userId);
        }

        try {
            userRepository.deleteById(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "User deleted successfully",
                    "userId", userId
            ));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "User cannot be deleted because related records exist",
                    "userId", userId
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Failed to delete user. Please try again.",
                    "userId", userId
            ));
        }
    }
}
