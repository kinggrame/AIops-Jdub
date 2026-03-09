package com.aiops.web.tunnel;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@EnableConfigurationProperties(TunnelProperties.class)
public class TunnelLifecycleManager {

    private static final Logger log = LoggerFactory.getLogger(TunnelLifecycleManager.class);
    private static final List<String> ALLOWED_COMMANDS = List.of("cloudflared", "frpc", "ngrok");
    private static final Pattern PUBLIC_URL_PATTERN = Pattern.compile("https://[-a-zA-Z0-9._~:/?#\\[\\]@!$&'()*+,;=%]+|tcp://[-a-zA-Z0-9._~:/?#\\[\\]@!$&'()*+,;=%]+");

    private final TunnelProperties properties;
    private Process process;
    private final AtomicReference<String> publicUrl = new AtomicReference<>();
    private final AtomicReference<String> lastMessage = new AtomicReference<>("Tunnel auto-start disabled");

    public TunnelLifecycleManager(TunnelProperties properties) {
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startTunnel() {
        if (!properties.isEnabled()) {
            log.info("Tunnel auto-start disabled");
            lastMessage.set("Tunnel auto-start disabled");
            return;
        }
        try {
            validateConfiguration();
            List<String> commandLine = buildCommandLine();
            ProcessBuilder builder = new ProcessBuilder(commandLine);
            builder.redirectErrorStream(true);
            process = builder.start();
            startLogPump(process);
            log.info("Tunnel process started with command {}", commandLine.get(0));
            lastMessage.set("Tunnel process started");
        } catch (Exception exception) {
            log.error("Tunnel start blocked: {}", exception.getMessage());
            lastMessage.set("Tunnel start blocked: " + exception.getMessage());
            stopTunnel();
        }
    }

    @PreDestroy
    public void stopTunnel() {
        if (process == null) {
            return;
        }
        if (process.isAlive()) {
            process.destroy();
            try {
                if (!process.waitFor(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }
        process = null;
        publicUrl.set(null);
        lastMessage.set("Tunnel stopped");
    }

    public TunnelStatus status() {
        return new TunnelStatus(
                properties.isEnabled(),
                process != null && process.isAlive(),
                properties.getCommand(),
                extractUrl(properties.getArguments()),
                publicUrl.get(),
                lastMessage.get()
        );
    }

    private void validateConfiguration() {
        String command = properties.getCommand() == null ? "" : properties.getCommand().trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_COMMANDS.contains(command)) {
            throw new IllegalStateException("Tunnel command not allowed: " + properties.getCommand());
        }
        String url = extractUrl(properties.getArguments());
        if (url == null) {
            throw new IllegalStateException("Tunnel arguments must include --url http://127.0.0.1:<port>");
        }
        URI uri = URI.create(url);
        String host = uri.getHost();
        boolean local = "127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host);
        if (!local && !properties.isAllowNonLocalUrl()) {
            throw new IllegalStateException("Tunnel target must stay on localhost unless aiops.tunnel.allow-non-local-url=true");
        }
    }

    private List<String> buildCommandLine() {
        List<String> parts = new ArrayList<>();
        parts.add(properties.getCommand());
        String arguments = properties.getArguments();
        if (arguments != null && !arguments.isBlank()) {
            for (String part : arguments.trim().split("\\s+")) {
                if (!part.isBlank()) {
                    parts.add(part);
                }
            }
        }
        return parts;
    }

    private String extractUrl(String arguments) {
        if (arguments == null || arguments.isBlank()) {
            return null;
        }
        String[] parts = arguments.trim().split("\\s+");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("--url".equals(parts[i])) {
                return parts[i + 1];
            }
        }
        return null;
    }

    private void startLogPump(Process currentProcess) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    capturePublicUrl(line);
                    log.info("[tunnel] {}", line);
                }
            } catch (IOException exception) {
                log.warn("Tunnel log stream closed: {}", exception.getMessage());
            }
        }, "aiops-tunnel-log");
        thread.setDaemon(true);
        thread.start();
    }

    private void capturePublicUrl(String line) {
        Matcher matcher = PUBLIC_URL_PATTERN.matcher(line);
        if (matcher.find()) {
            String detected = matcher.group();
            publicUrl.compareAndSet(null, detected);
            lastMessage.set("Public tunnel URL detected");
        }
    }
}
