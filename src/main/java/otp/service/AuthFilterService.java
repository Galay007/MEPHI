package otp.service;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import otp.model.User;
import otp.model.UserRole;
import otp.utilities.HttpUtilities;
import otp.utilities.JsonUtilities;
import otp.utilities.JwtUtilities;

import java.io.IOException;


public class AuthFilterService extends Filter {
    private final UserRole expectedRole;

    public AuthFilterService(UserRole expectedRole) {
        this.expectedRole = expectedRole;
    }

    @Override
    public String description() {
        return "Фильтр аутентификации и проверки роли (ROLE >= " + expectedRole + ")";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            HttpUtilities.sendError(exchange, 401, "Missing or invalid Authorization header");
            return;
        }
        String token = authHeader.replaceFirst("Bearer ", "");
        User user = JsonUtilities.fromJson(JwtUtilities.getClaim("user", token), User.class);

        if (user == null) {
            HttpUtilities.sendError(exchange, 401, "Invalid or expired token");
            return;
        }
        if (user.getRole().ordinal() < expectedRole.ordinal()) {
            HttpUtilities.sendError(exchange, 403, "No rights for this transaction");
            return;
        }
        exchange.setAttribute("user", user);
        chain.doFilter(exchange);
    }
}
