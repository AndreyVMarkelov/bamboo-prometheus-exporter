package ru.andreymarkelov.atlas.plugins.prombambooexporter.manager;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.bamboo.buildqueue.manager.AgentManager;
import com.atlassian.bamboo.license.BambooLicenseManager;
import com.atlassian.extras.api.bamboo.BambooLicense;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.hotspot.DefaultExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MetricCollectorImpl extends Collector implements MetricCollector, InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(MetricCollectorImpl.class);

    private final CollectorRegistry registry;
    private final BambooLicenseManager bambooLicenseManager;
    private final AgentManager agentManager;

    //--> Common

    private final Counter errorsCounter = Counter.build()
            .name("bamboo_error_count")
            .help("Errors Count")
            .labelNames("isNew")
            .create();

    //--> Agents

    private final Gauge allAgentsGauge = Gauge.build()
            .name("bamboo_all_agents_gauge")
            .help("All Agents Gauge")
            .create();

    private final Gauge activeAgentsGauge = Gauge.build()
            .name("bamboo_active_agents_gauge")
            .help("Active Agents Gauge")
            .create();

    private final Gauge busyAgentsGauge = Gauge.build()
            .name("bamboo_busy_agents_gauge")
            .help("Busy Agents Gauge")
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

    private final Counter buildQueueTimeoutCounter = Counter.build()
            .name("bamboo_build_queue_timeout_count")
            .help("Build Queue Timeout Count")
            .labelNames("planKey")
            .create();

    //--> Deploys

    private final Counter finishedDeploysCounter = Counter.build()
            .name("bamboo_finished_deploys_count")
            .help("Finished Deploys Count")
            .labelNames("planKey", "state")
            .create();

    //--> License
    private final Gauge maintenanceExpiryDaysGauge = Gauge.build()
            .name("bamboo_maintenance_expiry_days_gauge")
            .help("Maintenance Expiry Days Gauge")
            .create();

    private final Gauge licenseExpiryDaysGauge = Gauge.build()
            .name("bamboo_license_expiry_days_gauge")
            .help("License Expiry Days Gauge")
            .create();

    private final Gauge allowedUsersGauge = Gauge.build()
            .name("bamboo_allowed_users_gauge")
            .help("Allowed Users Gauge")
            .create();

    public MetricCollectorImpl(
            BambooLicenseManager bambooLicenseManager,
            AgentManager agentManager) {
        this.bambooLicenseManager = bambooLicenseManager;
        this.agentManager = agentManager;
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

    @Override
    public void buildQueueTimeoutCounter(String planKey) {
        buildQueueTimeoutCounter.labels(planKey).inc();
    }

    //--> Deploys

    @Override
    public void finishedDeploysCounter(String planKey, String state) {
        finishedDeploysCounter.labels(planKey, state).inc();
    }

    //--> Collect

    private List<MetricFamilySamples> collectInternal() {
        // license
        BambooLicense bambooLicense = bambooLicenseManager.getLicense();
        if (bambooLicense != null) {
            // because nullable
            if (bambooLicense.getMaintenanceExpiryDate() != null) {
                maintenanceExpiryDaysGauge.set(DAYS.convert(bambooLicense.getMaintenanceExpiryDate().getTime() - System.currentTimeMillis(), MILLISECONDS));
            }
            // because nullable
            if (bambooLicense.getExpiryDate() != null) {
                licenseExpiryDaysGauge.set(DAYS.convert(bambooLicense.getExpiryDate().getTime() - System.currentTimeMillis(), MILLISECONDS));
            }
            allowedUsersGauge.set(bambooLicense.getMaximumNumberOfUsers());
        }

        // agents
        allAgentsGauge.set(agentManager.getAllAgents().size());
        activeAgentsGauge.set(agentManager.getActiveAndEnabledAgents().size());
        busyAgentsGauge.set(agentManager.getBusyBuildAgents().size());

        List<MetricFamilySamples> result = new ArrayList<>();
        result.addAll(errorsCounter.collect());
        result.addAll(finishedBuildsCounter.collect());
        result.addAll(canceledBuildsCounter.collect());
        result.addAll(finishedDeploysCounter.collect());
        result.addAll(buildQueueTimeoutCounter.collect());
        result.addAll(maintenanceExpiryDaysGauge.collect());
        result.addAll(licenseExpiryDaysGauge.collect());
        result.addAll(allowedUsersGauge.collect());
        result.addAll(allAgentsGauge.collect());
        result.addAll(activeAgentsGauge.collect());
        result.addAll(busyAgentsGauge.collect());

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
