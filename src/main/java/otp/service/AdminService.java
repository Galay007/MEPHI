package otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import otp.model.User;
import otp.repository.OtpCodeRepository;
import otp.repository.OtpConfigRepository;
import otp.repository.UserRepository;

import java.util.List;

public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final OtpConfigRepository configRepository;
    private final UserRepository userRepository;

    public AdminService(OtpConfigRepository configRepository, UserRepository userRepository, OtpCodeRepository codeRepository) {
        this.configRepository = configRepository;
        this.userRepository = userRepository;
    }

    public void updateOtpConfig(int length, int ttlSeconds) {
        configRepository.updateConfig(length, ttlSeconds);
        logger.info("OTP config updated: length={}, ttlSeconds={}", length, ttlSeconds);
    }

    public List<User> getAllUsersWithoutAdmins() {
        return userRepository.findAllUsersWithoutAdmins();
    }

    public void deleteUserAndCodes(Long userId) {
        userRepository.delete(userId);
        logger.info("Deleted user {} and their OTP codes", userId);
    }
}


