package otp.api;

import com.sun.net.httpserver.HttpExchange;
import otp.model.User;
import otp.repository.OtpCodeRepositoryImpl;
import otp.repository.OtpConfigRepositoryImpl;
import otp.repository.UserRepositoryImpl;
import otp.service.AdminService;
import otp.utilities.HttpUtilities;
import otp.utilities.JsonUtilities;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class AdminApi {
    private final AdminService adminService = new AdminService(
            new OtpConfigRepositoryImpl(),
            new UserRepositoryImpl(),
            new OtpCodeRepositoryImpl()
    );

    public void updateOtpConfig(HttpExchange exchange) throws IOException {
        if (!"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtilities.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        String ct = exchange.getRequestHeaders().getFirst("Content-Type");
        if (ct == null || !ct.contains("application/json")) {
            HttpUtilities.sendError(exchange, 415, "Content-Type must be application/json");
            return;
        }

        try {
            ConfigRequest req = JsonUtilities.fromJson(exchange.getRequestBody(), ConfigRequest.class);
            adminService.updateOtpConfig(req.length, req.ttlSeconds);
            HttpUtilities.sendEmptyResponse(exchange, 204);
        } catch (IllegalArgumentException e) {
            HttpUtilities.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtilities.sendError(exchange, 500, "Internal server error");
        }
    }

    public void listUsers(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtilities.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        try {
            List<User> users = adminService.getAllUsersWithoutAdmins();
            String json = JsonUtilities.toJson(users);
            HttpUtilities.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            HttpUtilities.sendError(exchange, 500, "Internal server error");
        }
    }

    public void deleteUser(HttpExchange exchange) throws IOException {
        if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtilities.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        try {
            URI uri = exchange.getRequestURI();
            String[] segments = uri.getPath().split("/");
            Long id = Long.valueOf(segments[segments.length - 1]);
            adminService.deleteUserAndCodes(id);
            HttpUtilities.sendEmptyResponse(exchange, 204);
        } catch (NumberFormatException e) {
            HttpUtilities.sendError(exchange, 400, "Invalid user ID");
        } catch (IllegalArgumentException e) {
            HttpUtilities.sendError(exchange, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtilities.sendError(exchange, 500, "Internal server error");
        }
    }

    private static class ConfigRequest {
        public int length;
        public int ttlSeconds;
    }
}
