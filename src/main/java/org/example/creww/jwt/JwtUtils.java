package org.example.creww.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
//import jakarta.annotation.PostConstruct;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {


    @Value("${JWT_SECRET}")
    private String jwtSecret;
    private static String Secret_Key;
    private static final long EXPIRATION_TIME = 86400000;

    @PostConstruct
    public void init() {
        Secret_Key = jwtSecret;
    }

    public String generateToken(Long userId) {
        return JWT.create()
            .withSubject(String.valueOf(userId))
            .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .sign(Algorithm.HMAC512(Secret_Key));
    }

    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC512(Secret_Key)).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        DecodedJWT decoded = JWT.decode((token));
        return decoded.getSubject();
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean isTokenValid(String token) {
        return token != null && validateToken(token);
    }
}
