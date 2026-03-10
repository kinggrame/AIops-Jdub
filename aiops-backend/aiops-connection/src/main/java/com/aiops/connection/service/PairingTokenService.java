package com.aiops.connection.service;

import com.aiops.common.exception.BusinessException;
import com.aiops.connection.model.PairingTokenEntity;
import com.aiops.connection.repository.PairingTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class PairingTokenService {

    private final PairingTokenRepository pairingTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public PairingTokenService(PairingTokenRepository pairingTokenRepository) {
        this.pairingTokenRepository = pairingTokenRepository;
    }

    public String generatePairingToken(String hostname, String ip, Duration ttl) {
        String token = generateSecureToken();
        Instant expiresAt = Instant.now().plus(ttl != null ? ttl : Duration.ofMinutes(10));
        
        PairingTokenEntity entity = new PairingTokenEntity(token, hostname, ip, expiresAt);
        pairingTokenRepository.save(entity);
        
        return token;
    }

    public boolean validateAndConsumeToken(String token, String expectedHostname, String expectedIp) {
        if (token == null || token.isBlank()) {
            return false;
        }
        
        Optional<PairingTokenEntity> opt = pairingTokenRepository.findByTokenAndUsedFalse(token);
        if (opt.isEmpty()) {
            return false;
        }
        
        PairingTokenEntity entity = opt.get();
        
        if (Instant.now().isAfter(entity.getExpiresAt())) {
            return false;
        }
        
        entity.setUsed(true);
        pairingTokenRepository.save(entity);
        
        return true;
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
