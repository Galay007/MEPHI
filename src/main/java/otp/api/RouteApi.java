package otp.api;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import otp.model.UserRole;
import otp.service.AuthFilterService;

public class RouteApi {
    private final AuthApi authApi = new AuthApi();
    private final OtpApi otpApi = new OtpApi();
    private final AdminApi adminApi = new AdminApi();

    public void registerRoutes(HttpServer server) {
        // Публичные маршруты
        server.createContext("/register", authApi::handleRegister);
        server.createContext("/login",    authApi::handleLogin);

        // Маршруты для пользователей (роль USER)
        HttpContext genCtx = server.createContext("/otp/generate", otpApi::generateOtp);
        genCtx.getFilters().add(new AuthFilterService(UserRole.USER));
        HttpContext valCtx = server.createContext("/otp/validate", otpApi::validateOtp);
        valCtx.getFilters().add(new AuthFilterService(UserRole.USER));

        // Маршруты для администратора (роль ADMIN)
        HttpContext configCtx = server.createContext("/admin/config", adminApi::updateOtpConfig);
        configCtx.getFilters().add(new AuthFilterService(UserRole.ADMIN));
        HttpContext usersCtx = server.createContext("/admin/users", exchange -> {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                adminApi.listUsers(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                adminApi.deleteUser(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });
        usersCtx.getFilters().add(new AuthFilterService(UserRole.ADMIN));
    }
}
