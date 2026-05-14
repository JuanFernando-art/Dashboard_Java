package com.meuprojeto.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class AppConfig {
    private static final Map<String, String> LOCAL_ENV = loadLocalEnv();
    private static final String APP_ENV = readEnv("APP_ENV");
    private static final boolean DEV = "dev".equalsIgnoreCase(APP_ENV);

    private AppConfig() {}

    public static boolean isDev() {
        return DEV;
    }

    public static String requireEnv(String name) {
        String value = readEnv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " deve ser definido fora do ambiente local.");
        }
        return value;
    }

    public static String envOrDevFallback(String name, String devFallback) {
        String value = readEnv(name);
        if (value != null && !value.isBlank()) {
            return value;
        }

        if (!DEV) {
            throw new IllegalStateException(name + " deve ser definido fora do ambiente local.");
        }

        System.err.println("Aviso: " + name + " nao definido. Usando fallback local de desenvolvimento.");
        return devFallback;
    }

    private static String readEnv(String name) {
        String value = System.getenv(name);
        if (value != null && !value.isBlank()) {
            return value;
        }

        return LOCAL_ENV.get(name);
    }

    private static Map<String, String> loadLocalEnv() {
        Path envFile = findLocalEnvFile();
        if (!Files.isRegularFile(envFile)) {
            return Map.of();
        }

        Map<String, String> values = new HashMap<>();
        try {
            for (String line : Files.readAllLines(envFile)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int separator = trimmed.indexOf('=');
                if (separator <= 0) {
                    continue;
                }

                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();
                values.put(key, stripQuotes(value));
            }
        } catch (IOException e) {
            System.err.println("Aviso: nao foi possivel ler .env.local: " + e.getMessage());
        }

        return values;
    }

    private static Path findLocalEnvFile() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            Path candidate = current.resolve(".env.local");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }

            current = current.getParent();
        }

        return Path.of(".env.local");
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }
}
