package ru.andreymarkelov.atlas.plugins.prombambooexporter.listener;

import com.atlassian.bamboo.deployments.execution.events.DeploymentFinishedEvent;
import com.atlassian.bamboo.deployments.projects.DeploymentProject;
import com.atlassian.bamboo.deployments.projects.service.DeploymentProjectService;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.event.BambooErrorEvent;
import com.atlassian.bamboo.event.BuildCanceledEvent;
import com.atlassian.bamboo.event.BuildFinishedEvent;
import com.atlassian.bamboo.event.BuildQueueTimeoutEvent;
import com.atlassian.event.api.EventListener;
import ru.andreymarkelov.atlas.plugins.prombambooexporter.manager.MetricCollector;

public class MetricListener {
    private final MetricCollector metricCollector;
    private final DeploymentResultService deploymentResultService;
    private final DeploymentProjectService deploymentProjectService;

    public MetricListener(
            MetricCollector metricCollector,
            DeploymentResultService deploymentResultService,
            DeploymentProjectService deploymentProjectService) {
        this.metricCollector = metricCollector;
        this.deploymentResultService = deploymentResultService;
        this.deploymentProjectService = deploymentProjectService;
    }

    @EventListener
    public void buildFinishedEvent(BuildFinishedEvent buildFinishedEvent) {
        long duration = 0;
        if (buildFinishedEvent.getBuildContext() != null) {
            duration = buildFinishedEvent.getTimestamp() - buildFinishedEvent.getBuildContext().getCurrentResult().getTasksStartDate().getTime();
        }
        metricCollector.finishedBuildsCounter(buildFinishedEvent.getBuildPlanKey(), buildFinishedEvent.getBuildState().name());
        metricCollector.finishedBuildsDuration(buildFinishedEvent.getBuildPlanKey(), duration);
    }

    @EventListener
    public void buildCanceledEvent(BuildCanceledEvent buildCanceledEvent) {
        metricCollector.canceledBuildsCounter(buildCanceledEvent.getBuildPlanKey());
    }

    @EventListener
    public void bambooErrorEvent(BambooErrorEvent bambooErrorEvent) {
        metricCollector.errorsCounter(bambooErrorEvent.isNewError());
    }

    @EventListener
    public void buildQueueTimeoutEvent(BuildQueueTimeoutEvent buildQueueTimeoutEvent) {
        metricCollector.buildQueueTimeoutCounter(buildQueueTimeoutEvent.getBuildPlanKey());
    }

    @EventListener
    public void deploymentFinishedEvent(DeploymentFinishedEvent deploymentFinishedEvent) {
        DeploymentResult deploymentResult = deploymentResultService.getDeploymentResult(deploymentFinishedEvent.getDeploymentResultId());
        DeploymentProject deploymentProject = deploymentProjectService.getDeploymentProject(deploymentResult.getEnvironment().getDeploymentProjectId());
        if (deploymentResult != null && deploymentProject != null) {
            metricCollector.finishedDeploysCounter(deploymentProject.getPlanKey().getKey(), deploymentResult.getDeploymentState().name());
        }
    }
}
