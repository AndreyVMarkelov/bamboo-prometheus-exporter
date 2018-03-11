package ru.andreymarkelov.atlas.plugins.prombambooexporter.listener;

import com.atlassian.bamboo.event.BambooErrorEvent;
import com.atlassian.bamboo.event.BuildCanceledEvent;
import com.atlassian.bamboo.event.BuildFinishedEvent;
import com.atlassian.event.api.EventListener;
import ru.andreymarkelov.atlas.plugins.prombambooexporter.manager.MetricCollector;

public class MetricListener {
    private final MetricCollector metricCollector;

    public MetricListener(MetricCollector metricCollector) {
        this.metricCollector = metricCollector;
    }

    @EventListener
    public void buildFinishedEvent(BuildFinishedEvent buildFinishedEvent) {
        metricCollector.finishedBuildsCounter(buildFinishedEvent.getBuildPlanKey(), buildFinishedEvent.getBuildState().name());
    }

    @EventListener
    public void buildCanceledEvent(BuildCanceledEvent buildCanceledEvent) {
        metricCollector.canceledBuildsCounter(buildCanceledEvent.getBuildPlanKey());
    }

    @EventListener
    public void bambooErrorEvent(BambooErrorEvent bambooErrorEvent) {
        metricCollector.errorsCounter(bambooErrorEvent.isNewError());
    }
}
