package ru.andreymarkelov.atlas.plugins.prombambooexporter.manager;

public interface SecureTokenManager {
    String getToken();
    void setToken(String token);
}
