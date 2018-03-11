package ru.andreymarkelov.atlas.plugins.prombambooexporter.manager;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.hotspot.DefaultExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class MetricCollectorImpl extends Collector implements MetricCollector, InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(MetricCollectorImpl.class);

    private final CollectorRegistry registry;

    //--> Common

    private final Counter errorsCounter = Counter.build()
            .name("bamboo_error_count")
            .help("Errors Count")
            .labelNames("isNew")
            .create();

    //--> Builds

    private final Counter finishedBuildsCounter = Counter.build()
            .name("bamboo_finished_build_count")
            .help("Finished Builds Count")
            .labelNames("planKey", "state")
            .create();

    private final Counter canceledBuildsCounter = Counter.build()
            .name("bamboo_canceled_build_count")
            .help("Canceled Builds Count")
            .labelNames("planKey")
            .create();

    public MetricCollectorImpl() {
        this.registry = CollectorRegistry.defaultRegistry;
    }

    //--> Common

    @Override
    public void errorsCounter(boolean isNew) {
        errorsCounter.labels(String.valueOf(isNew)).inc();
    }

    //--> Builds

    @Override
    public void finishedBuildsCounter(String planKey, String state) {
        finishedBuildsCounter.labels(planKey, state).inc();
    }

    @Override
    public void canceledBuildsCounter(String planKey) {
        canceledBuildsCounter.labels(planKey).inc();
    }

    //--> Collect

    private List<MetricFamilySamples> collectInternal() {
        List<MetricFamilySamples> result = new ArrayList<>();
        result.addAll(errorsCounter.collect());
        result.addAll(finishedBuildsCounter.collect());
        result.addAll(canceledBuildsCounter.collect());
        return result;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        long start = System.currentTimeMillis();
        try {
            return collectInternal();
        } catch (Throwable throwable) {
            log.error("Error collect prometheus metrics", throwable);
            return emptyList();
        } finally {
            log.debug("Collect execution time is: {}ms", System.currentTimeMillis() - start);
        }
    }

    @Override
    public void destroy() {
        this.registry.unregister(this);
    }

    @Override
    public void afterPropertiesSet() {
        this.registry.register(this);
        DefaultExports.initialize();
    }

    @Override
    public CollectorRegistry getRegistry() {
        return registry;
    }
}
