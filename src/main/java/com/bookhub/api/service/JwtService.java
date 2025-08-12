package com.bookhub.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.PublicKey;

@Service
public class JwtService {

//
//    private final String SECRET_KEY;
//
//    public JwtService(@Value("${jwt.secret}") String secretKey) {
//        this.SECRET_KEY = secretKey;
//    }
//
//    public String extractUsername(String token) {
//        return null;
//    }
//
//    private Claims extractAllClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(getSignInKey()) // Not verifyWith – that's for asymmetric keys like RSA
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//        return null;
//
//    }
//
//    private Key getSignInKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }


}
