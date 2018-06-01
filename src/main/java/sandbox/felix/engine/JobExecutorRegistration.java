package sandbox.felix.engine;

import java.util.Collections;
import java.util.Map;

import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

import sandbox.felix.MatchingRequirement;
import sandbox.felix.Namespaces;

public class JobExecutorRegistration implements MatchingRequirement<JobExecutorRegistration>, Capability {

	private final Resource resource;
	private final String deploymentId;
	
	public JobExecutorRegistration(Resource resource, String deploymentId)
	{
		this.resource = resource;
		this.deploymentId = deploymentId;
	}
	
	@Override
	public String getNamespace() {
		return Namespaces.ENGINE_NS;
	}

	@Override
	public Map<String, String> getDirectives() {
        return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return Collections.singletonMap("deployment", deploymentId);
	}

	@Override
	public Resource getResource() {
		return resource;
	}

	@Override
	public boolean isSatisfiedBy(JobExecutorRegistration requirement) {
		return deploymentId.equals(requirement.deploymentId);
	}

	@Override
	public Class<JobExecutorRegistration> getCapabilityType() {
		return JobExecutorRegistration.class;
	}
	
	@Override
	public String toString() {
		return "Job Executor registration for deployment " + deploymentId;
	}

}
