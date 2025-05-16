package otp.api;

import com.sun.net.httpserver.HttpExchange;
import otp.model.User;
import otp.repository.OtpCodeRepositoryImpl;
import otp.repository.OtpConfigRepositoryImpl;
import otp.repository.UserRepositoryImpl;
import otp.service.OtpService;
import otp.service.notification.NotificationChannel;
import otp.service.notification.NotificationServiceFactory;
import otp.utilities.HttpUtilities;
import otp.utilities.JsonUtilities;

import java.io.IOException;

public class OtpApi {
    private final OtpService otpService = new OtpService(
            new OtpCodeRepositoryImpl(),
            new OtpConfigRepositoryImpl(),
            new UserRepositoryImpl(),
            new NotificationServiceFactory()
    );

    public void generateOtp(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtilities.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("application/json")) {
            HttpUtilities.sendError(exchange, 415, "Content-Type must be application/json");
            return;
        }

        try {
            GenerateOtpDto req = JsonUtilities.fromJson(exchange.getRequestBody(), GenerateOtpDto.class);
            User user = (User) exchange.getAttribute("user");
            otpService.sendOtpToUser(user.getId(), req.operationId,
                    NotificationChannel.valueOf(req.channel));
            HttpUtilities.sendEmptyResponse(exchange, 202);
        } catch (IllegalArgumentException e) {
            HttpUtilities.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtilities.sendError(exchange, 500, "Internal server error");
        }
    }

    public void validateOtp(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtilities.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("application/json")) {
            HttpUtilities.sendError(exchange, 415, "Content-Type must be application/json");
            return;
        }

        try {
            ValidateOtpDto req = JsonUtilities.fromJson(exchange.getRequestBody(), ValidateOtpDto.class);
            boolean valid = otpService.validateOtp(req.code);
            if (valid) {
                HttpUtilities.sendEmptyResponse(exchange, 200);
            } else {
                HttpUtilities.sendError(exchange, 400, "Invalid or expired code");
            }
        } catch (IllegalArgumentException e) {
            HttpUtilities.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtilities.sendError(exchange, 500, "Internal server error");
        }
    }

    public static class GenerateOtpDto {
        public String operationId;
        public String channel;
    }

    private static class ValidateOtpDto {
        public String code;
    }
}
