package com.taskflow.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

/**
 * Loads configuration using environment variables over profile properties over defaults.
 */
public final class ConfigService {
    private final Properties defaults = new Properties();
    private final Properties profile = new Properties();

    public ConfigService() {
        load(defaults, "application.properties");
        String activeProfile = env("TASKFLOW_PROFILE").orElse(defaults.getProperty("taskflow.profile", "dev"));
        load(profile, "application-" + activeProfile + ".properties");
    }

    public String get(String key) {
        return getOptional(key).orElseThrow(() -> new IllegalArgumentException("missing config key " + key));
    }

    public int getInt(String key, int fallback) {
        return getOptional(key).map(Integer::parseInt).orElse(fallback);
    }

    public Optional<String> getOptional(String key) {
        String envKey = toEnvKey(key);
        return env(envKey)
                .or(() -> Optional.ofNullable(profile.getProperty(key)).map(this::resolvePlaceholder))
                .or(() -> Optional.ofNullable(defaults.getProperty(key)).map(this::resolvePlaceholder));
    }

    private String toEnvKey(String key) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char current = key.charAt(i);
            if (current == '.') {
                builder.append('_');
            } else {
                if (Character.isUpperCase(current) && i > 0 && key.charAt(i - 1) != '.') {
                    builder.append('_');
                }
                builder.append(Character.toUpperCase(current));
            }
        }
        return builder.toString();
    }

    private Optional<String> env(String key) {
        return Optional.ofNullable(System.getenv(key)).filter(value -> !value.isBlank());
    }

    private String resolvePlaceholder(String value) {
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
            return env(value.substring(2, value.length() - 1)).orElse("");
        }
        return value;
    }

    private void load(Properties target, String resource) {
        try (InputStream stream = ConfigService.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream != null) {
                target.load(stream);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("failed to load " + resource, ex);
        }
    }
}
