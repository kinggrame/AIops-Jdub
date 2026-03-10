package com.aiops.connection.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "pairing_tokens")
public class PairingTokenEntity {

    @Id
    @Column(name = "token", length = 64)
    private String token;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "ip")
    private String ip;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public PairingTokenEntity() {}

    public PairingTokenEntity(String token, String hostname, String ip, Instant expiresAt) {
        this.token = token;
        this.hostname = hostname;
        this.ip = ip;
        this.expiresAt = expiresAt;
        this.used = false;
        this.createdAt = Instant.now();
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
