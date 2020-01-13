package ru.andreymarkelov.atlas.plugins.prombambooexporter.manager;

import io.prometheus.client.CollectorRegistry;

public interface MetricCollector {
    CollectorRegistry getRegistry();
    void errorsCounter(boolean isNew);
    void finishedBuildsCounter(String planKey, String state);

    /**
     * Duration of plan in milliseconds.
     *
     * @param planKey - plan key.
     * @param duration - plan duration to complete (any state) in milliseconds.
     */
    void finishedBuildsDuration(String planKey, long duration);

    void canceledBuildsCounter(String planKey);
    void finishedDeploysCounter(String planKey, String state);
    void buildQueueTimeoutCounter(String planKey);
}
