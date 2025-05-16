package otp.api;

import com.sun.net.httpserver.HttpExchange;
import otp.model.UserRole;
import otp.repository.UserRepositoryImpl;
import otp.service.UserService;
import otp.utilities.HttpUtilities;
import otp.utilities.JsonUtilities;

import java.io.IOException;
import java.util.Map;

public class AuthApi {
    private final UserService userService = new UserService(new UserRepositoryImpl());

    public void handleRegister(HttpExchange exchange) throws IOException {
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
            RegisterRequest req = JsonUtilities.fromJson(exchange.getRequestBody(), RegisterRequest.class);

            if ("ADMIN".equals(req.role) && userService.adminExists()) {
                HttpUtilities.sendError(exchange, 409, "Admin already exists");
                return;
            }

            userService.register(req.username, req.password, UserRole.valueOf(req.role));
            HttpUtilities.sendEmptyResponse(exchange, 201);
        } catch (IllegalArgumentException | IllegalStateException e) {
            HttpUtilities.sendError(exchange, 409, e.getMessage());
        } catch (Exception e) {
            HttpUtilities.sendError(exchange, 500, "Internal server error");
        }
    }

    public void handleLogin(HttpExchange exchange) throws IOException {
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
            LoginRequest req = JsonUtilities.fromJson(exchange.getRequestBody(), LoginRequest.class);
            String token = userService.login(req.username, req.password);
            if (token == null) {
                HttpUtilities.sendError(exchange, 401, "Unauthorized");
                return;
            }
            String json = JsonUtilities.toJson(Map.of("token", token));
            HttpUtilities.sendJsonResponse(exchange, 200, json);
        } catch (IllegalArgumentException e) {
            HttpUtilities.sendError(exchange, 401, e.getMessage());
        } catch (Exception e) {
            HttpUtilities.sendError(exchange, 500, "Internal server error");
        }
    }

    private static class RegisterRequest {
        public String username;
        public String password;
        public String role;
    }

    private static class LoginRequest {
        public String username;
        public String password;
    }
}
