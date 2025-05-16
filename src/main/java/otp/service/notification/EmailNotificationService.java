package otp.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    private final Session session;
    private final String fromAddress;

    public EmailNotificationService() {
        Properties props = loadConfig();
        this.fromAddress = props.getProperty("email.from");
        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        props.getProperty("email.username"),
                        props.getProperty("email.password")
                );
            }
        });
    }

    private Properties loadConfig() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("email.properties")) {
            if (is == null) {
                throw new IllegalStateException("email.properties not found in classpath");
            }
            Properties props = new Properties();
            props.load(is);
            return props;
        } catch (IOException e) {
            logger.error("Failed to load email.properties", e);
            throw new RuntimeException("Could not load email configuration", e);
        }
    }

    @Override
    public void sendCode(String recipientEmail, String code) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject("Your OTP Code");
            message.setText("Your one-time confirmation code is: " + code);

            Transport.send(message);
            logger.info("OTP code sent via Email to {}", recipientEmail);
        } catch (MessagingException e) {
            logger.error("Failed to send OTP email to {}", recipientEmail, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}

