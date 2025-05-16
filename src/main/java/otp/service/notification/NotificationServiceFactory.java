package otp.service.notification;

public class NotificationServiceFactory {

    public NotificationService getService(NotificationChannel channel) {
        switch (channel) {
            case EMAIL:
                return new EmailNotificationService();
            case SMS:
                return new SmsNotificationService();
            case TELEGRAM:
                return new TelegramNotificationService();
            case FILE:
                return new FileNotificationService();
            default:
                throw new IllegalArgumentException("Unsupported channel: " + channel);
        }
    }
}

