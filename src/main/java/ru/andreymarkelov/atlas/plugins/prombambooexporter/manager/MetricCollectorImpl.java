package ru.andreymarkelov.atlas.plugins.prombambooexporter.manager;

import com.atlassian.bamboo.ServerLifecycleProvider;
import com.atlassian.bamboo.buildqueue.manager.AgentManager;
import com.atlassian.bamboo.event.spi.ExecutorStats;
import com.atlassian.bamboo.license.BambooLicenseManager;
import com.atlassian.bamboo.plan.NonBlockingPlanExecutionService;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plan.TopLevelPlan;
import com.atlassian.bamboo.plan.branch.ChainBranch;
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

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MetricCollectorImpl extends Collector implements MetricCollector, InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(MetricCollectorImpl.class);

    private final CollectorRegistry registry;
    private final BambooLicenseManager bambooLicenseManager;
    private final AgentManager agentManager;
    private final NonBlockingPlanExecutionService nonBlockingPlanExecutionService;
    private final PlanManager planManager;
    private final ServerLifecycleProvider serverLifecycleProvider;

    //--> Common

    private final Counter errorsCounter = Counter.build()
            .name("bamboo_error_count")
            .help("Errors Count")
            .labelNames("isNew")
            .create();

    private final Gauge lifecycleStateGauge = Gauge.build()
            .name("bamboo_lifecycle_state_gauge")
            .help("Lifecycle State")
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

    //--> Plans

    private final Gauge plansGauge = Gauge.build()
            .name("bamboo_plans_gauge")
            .help("Plans By Type (top, branch) Gauge")
            .labelNames("type")
            .create();

    //--> Workers

    private final Gauge planWorkerIdleGauge = Gauge.build()
            .name("bamboo_plans_workers_idle_gauge")
            .help("Plan Workers Idle Gauge")
            .create();

    private final Gauge planWorkerBusyGauge = Gauge.build()
            .name("bamboo_plans_workers_busy_gauge")
            .help("Plan Workers Busy Gauge")
            .create();

    private final Gauge planWorkerQueueGauge = Gauge.build()
            .name("bamboo_plans_workers_queue_gauge")
            .help("Plan Workers Queue Size Gauge")
            .create();

    public MetricCollectorImpl(
            BambooLicenseManager bambooLicenseManager,
            AgentManager agentManager,
            NonBlockingPlanExecutionService nonBlockingPlanExecutionService,
            PlanManager planManager,
            ServerLifecycleProvider serverLifecycleProvider) {
        this.bambooLicenseManager = bambooLicenseManager;
        this.agentManager = agentManager;
        this.nonBlockingPlanExecutionService = nonBlockingPlanExecutionService;
        this.planManager = planManager;
        this.serverLifecycleProvider = serverLifecycleProvider;
        this.registry = CollectorRegistry.defaultRegistry;
    }

    // Implementations

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
        // server states
        lifecycleStateGauge.set(serverLifecycleProvider.getServerLifecycleState().ordinal());

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

        // plans
        plansGauge.labels("top").set(planManager.getPlanCount(TopLevelPlan.class));
        plansGauge.labels("branch").set(planManager.getPlanCount(ChainBranch.class));

        // workers
        final ExecutorStats planExecutorStats = this.nonBlockingPlanExecutionService.getExecutorStats();
        final int planExecutorStatsActiveCount = planExecutorStats.getActiveCount();
        planWorkerIdleGauge.set(planExecutorStats.getPoolSize() - planExecutorStatsActiveCount);
        planWorkerBusyGauge.set(planExecutorStatsActiveCount);
        planWorkerQueueGauge.set(planExecutorStats.getEventsQueue().size());

        List<MetricFamilySamples> result = new ArrayList<>();
        result.addAll(lifecycleStateGauge.collect());
        result.addAll(errorsCounter.collect());
        result.addAll(finishedBuildsCounter.collect());
        result.addAll(canceledBuildsCounter.collect());
        result.addAll(finishedDeploysCounter.collect());
        result.addAll(buildQueueTimeoutCounter.collect());
        // license
        result.addAll(maintenanceExpiryDaysGauge.collect());
        result.addAll(licenseExpiryDaysGauge.collect());
        result.addAll(allowedUsersGauge.collect());
        // agents
        result.addAll(allAgentsGauge.collect());
        result.addAll(activeAgentsGauge.collect());
        result.addAll(busyAgentsGauge.collect());
        // plans
        result.addAll(plansGauge.collect());
        // workers
        result.addAll(planWorkerIdleGauge.collect());
        result.addAll(planWorkerBusyGauge.collect());
        result.addAll(planWorkerQueueGauge.collect());

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
