package tn.esprit.userservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import tn.esprit.userservice.entity.User;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private static final String SECRET = "MySuperSecretKeyWith256BitsLength!!!!!!!";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

   // private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long expirationTimeMs = 1000 * 60 * 60 * 10; // 10h

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername()) // ✅ maintenant on utilise le username
                .claim("email", user.getEmail()) // 👉 si tu veux garder l'email
                .claim("role", user.getRole())

                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMs))
                .signWith(key)
                .compact();
    }


    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }



}
