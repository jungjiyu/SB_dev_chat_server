package com.chat.kit.myutil_token.system.service;

import com.chat.kit.myutil_token.system.util.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
@Slf4j
public class TokenService {

    private PrivateKey privateKey;

    @PostConstruct
    private void initializePrivateKey() {
        try {
            log.info("Initializing Private Key...");
            String privateKeyPath = "private_key.pem";
            InputStream privateKeyStream = getClass().getClassLoader().getResourceAsStream(privateKeyPath);
            if (privateKeyStream == null) {
                throw new IllegalArgumentException("Private key file not found: " + privateKeyPath);
            }

            String privateKeyContent = new String(privateKeyStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = keyFactory.generatePrivate(privateKeySpec);

            log.info("PrivateKey successfully initialized.");
        } catch (Exception e) {
            log.error("Error initializing TokenService: ", e);
            throw new RuntimeException(e);
        }
    }

    public String createSystemToken() {
        Instant instant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiredDate = instant.plus(JwtProperties.ACCESS_TOKEN_EXPIRE_TIME, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setSubject("SYSTEM")
                .claim(JwtProperties.ROLE, "SYSTEM")
                .setIssuedAt(Date.from(instant))
                .setExpiration(Date.from(expiredDate))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

}
