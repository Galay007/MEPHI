package otp.utilities;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import otp.model.User;
import otp.model.UserRole;
import otp.utilities.JsonUtilities;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class JwtUtilities {

    private static final String SECRET_KEY = "secret_key";
    private static final long expirationTimeMinutes = 30; // Токен действует 30 минут

    public static String generateToken(User user) {
        long currentTimeMillis = System.currentTimeMillis();
        long expirationMillis = currentTimeMillis + (expirationTimeMinutes * 60 * 1000);
        String userDetailsJson = "";

        try {
            userDetailsJson = JsonUtilities.toJson(user);
        } catch (Exception e) {
            return null;
        }

        return JWT.create()
                .withClaim("user", userDetailsJson)
                .withIssuedAt(new Date(currentTimeMillis))
                .withExpiresAt(new Date(expirationMillis))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public static boolean validateToken(String token) {
        return verify(token) != null;
    }

    private static DecodedJWT verify(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build()
                    .verify(token);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getClaim(String field, String token) {
        if (token == null || !validateToken(token)) return null;
        return verify(token).getClaim(field).asString();
    }

    public static String extractUsername(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token);
        return decodedJWT.getSubject();
    }
}

