package otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import otp.model.User;
import otp.model.UserRole;
import otp.repository.UserRepository;
import otp.utilities.JwtUtilities;
import otp.utilities.PasswordEncoder;

import java.util.List;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(String username, String password, UserRole role) {
        if (userRepository.findByUsername(username) != null) {
            logger.warn("Attempt to register with existing username: {}", username);
            throw new IllegalArgumentException("Username already exists");
        }
        if (role == UserRole.ADMIN && adminExists()) {
            logger.warn("Attempt to register second ADMIN: {}", username);
            throw new IllegalStateException("Administrator already exists");
        }

        String hashed = PasswordEncoder.hash(password);
        User user = new User(null, username, hashed, role);
        userRepository.create(user);
        logger.info("Registered new user: {} with role {}", username, role);
    }

    public boolean adminExists() {
        return userRepository.adminExists();
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.warn("Login failed: user not found {}", username);
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (!PasswordEncoder.matches(password, user.getPasswordHash())) {
            logger.warn("Login failed: wrong password for {}", username);
            throw new IllegalArgumentException("Invalid username or password");
        }
        String token = JwtUtilities.generateToken(user);
        logger.info("User {} logged in, token generated", username);
        return token;
    }
}
