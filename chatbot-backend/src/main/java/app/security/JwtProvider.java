package app.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import app.model.User;

@Component
public class JwtProvider {
    public static final int ACCESS_EXP_SEC = 60 * 5;
    public static final int REFRESH_EXP_SEC = 60 * 60 * 24 * 30;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER = "Authorization";
    private static String secret;

    @Value("${chatbot.secret}")
    public void setKey(String value) {
        secret = value;
    }

    public static String createAccess(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expired = now.plusSeconds(ACCESS_EXP_SEC);
        String jwt = JWT.create()
                .withExpiresAt(Timestamp.valueOf(expired))
                .withClaim("id", user.getId())
                .withClaim("role", String.valueOf(user.getRole()))
                .sign(Algorithm.HMAC512(secret));
        return TOKEN_PREFIX + jwt;
    }


    public static String createRefresh(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expired = now.plusSeconds(REFRESH_EXP_SEC);
        return JWT.create()
                .withExpiresAt(Timestamp.valueOf(expired))
                .withClaim("id", user.getId())
                .sign(Algorithm.HMAC512(secret));
    }

    public static DecodedJWT verify(String jwt) {
        return JWT.require(Algorithm.HMAC512(secret)).build()
                .verify(jwt.replace(TOKEN_PREFIX, ""));
    }
}
