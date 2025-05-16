package otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import otp.model.OtpCode;
import otp.model.OtpConfig;
import otp.model.OtpStatus;
import otp.model.User;
import otp.repository.OtpCodeRepository;
import otp.repository.OtpConfigRepository;
import otp.repository.UserRepository;
import otp.service.notification.NotificationChannel;
import otp.service.notification.NotificationService;
import otp.service.notification.NotificationServiceFactory;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom random = new SecureRandom();

    private final OtpCodeRepository otpCodeRepository;
    private final OtpConfigRepository otpConfigRepository;
    private final UserRepository userRepository;
    private final NotificationServiceFactory notificationFactory;

    public OtpService(OtpCodeRepository otpCodeRepository,
                      OtpConfigRepository otpConfigRepository,
                      UserRepository userRepository,
                      NotificationServiceFactory notificationFactory) {
        this.otpCodeRepository = otpCodeRepository;
        this.otpConfigRepository = otpConfigRepository;
        this.userRepository = userRepository;
        this.notificationFactory = notificationFactory;
    }

    public String generateOtp(Long userId, String operationId) {
        OtpConfig config = otpConfigRepository.getConfig();
        int length = config.getLength();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        String code = sb.toString();

        OtpCode otp = new OtpCode(
                null,
                userId,
                operationId,
                code,
                OtpStatus.ACTIVE,
                LocalDateTime.now()
        );
        otpCodeRepository.save(otp);
        logger.info("Generated OTP {} for userId={}, operationId={}", code, userId, operationId);
        return code;
    }

    public void sendOtpToUser(Long userId, String operationId, NotificationChannel channel) {
        String code = generateOtp(userId, operationId);
        User user = userRepository.findById(userId);
        if (user == null) {
            logger.error("sendOtpToUser: user not found, id={}", userId);
            throw new IllegalArgumentException("User not found");
        }

        String recipient = null;
        NotificationService svc = notificationFactory.getService(channel);
        svc.sendCode(recipient, code);
        logger.info("Sent OTP code for userId={} via {}", userId, channel);
    }

    public boolean validateOtp(String inputCode) {
        OtpCode otp = otpCodeRepository.findByCode(inputCode);
        if (otp == null) {
            logger.warn("validateOtp: code not found {}", inputCode);
            return false;
        }

        if (otp.getStatus() != OtpStatus.ACTIVE) {
            logger.warn("validateOtp: code {} is not active (status={})", inputCode, otp.getStatus());
            return false;
        }

        OtpConfig config = otpConfigRepository.getConfig();
        LocalDateTime expiry = otp.getCreatedAt().plusSeconds(config.getTtlSeconds());
        if (LocalDateTime.now().isAfter(expiry)) {
            otpCodeRepository.markAsExpired(otp.getId());
            logger.warn("validateOtp: code {} expired at {}", inputCode, expiry);
            return false;
        }

        otpCodeRepository.markAsUsed(otp.getId());
        logger.info("validateOtp: code {} validated and marked as USED", inputCode);
        return true;
    }
}

