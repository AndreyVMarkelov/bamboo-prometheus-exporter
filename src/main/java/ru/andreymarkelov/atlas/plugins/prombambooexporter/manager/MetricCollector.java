package ru.andreymarkelov.atlas.plugins.prombambooexporter.manager;

import io.prometheus.client.CollectorRegistry;

public interface MetricCollector {
    CollectorRegistry getRegistry();
    void errorsCounter(boolean isNew);
    void finishedBuildsCounter(String planKey, String state);
    void canceledBuildsCounter(String planKey);
}
