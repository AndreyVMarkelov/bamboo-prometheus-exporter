package ru.andreymarkelov.atlas.plugins.prombambooexporter.manager;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class SecureTokenManagerImpl implements SecureTokenManager {
    private final PluginSettings pluginSettings;

    public SecureTokenManagerImpl(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
    }

    @Override
    public String getToken() {
        Object storedValue = getPluginSettings().get("PLUGIN_PROMETHEUS_FOR_BAMBOO_SECURITY_TOKEN");
        return storedValue != null ? storedValue.toString() : "";
    }

    @Override
    public void setToken(String token) {
        getPluginSettings().put("PLUGIN_PROMETHEUS_FOR_BAMBOO_SECURITY_TOKEN", token);
    }

    private synchronized PluginSettings getPluginSettings() {
        return pluginSettings;
    }
}
