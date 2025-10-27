package com.example.booking.service;

import com.example.booking.model.User;
import com.example.booking.repo.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final String jwtSecret; // Поле для хранения секрета

    @Autowired // Spring увидит этот конструктор и будет его использовать
    public AuthService(UserRepository userRepository, @Value("${security.jwt.secret}") String jwtSecret) {
        this.userRepository = userRepository;
        this.jwtSecret = jwtSecret; // Присваиваем значение полю
        // Теперь используем this.jwtSecret для создания ключа
        initializeSecretKey();
    }

    private SecretKey key; // Ключ для подписи JWT

    private void initializeSecretKey() {
        byte[] bytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public User register(String username, String password, boolean admin) {
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        u.setRole(admin ? "ADMIN" : "USER");
        return userRepository.save(u);
    }

    public String login(String username, String password) {
        User u = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!BCrypt.checkpw(password, u.getPasswordHash())) {
            throw new IllegalArgumentException("Bad credentials");
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(u.getId().toString())
                .addClaims(Map.of(
                        "scope", u.getRole(),
                        "username", u.getUsername()
                ))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600)))
                .signWith(key)
                .compact();
    }
}


