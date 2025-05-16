package otp.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class FileNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FileNotificationService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void sendCode(String recipientName, String code) {
        String fileName;
        fileName = Objects.requireNonNullElse(recipientName, "opt.txt");

        Path projectRoot = Paths.get("").toAbsolutePath();
        Path path = projectRoot.resolve(fileName);
        String entry = String.format("%s - OTP: %s%n",
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                code);
        try {
            Files.write(path, entry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.info("OTP code written to file {}", fileName);
        } catch (IOException e) {
            logger.error("Failed to write OTP to file {}", fileName, e);
            throw new RuntimeException("File write failed", e);
        }
    }
}

