package sandbox.felix.engine;

import java.util.Collections;
import java.util.Map;

import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

import sandbox.felix.MatchingRequirement;
import sandbox.felix.Namespaces;

public class DeploymentRegistration implements MatchingRequirement<DeploymentRegistration>, Capability {

	private final Resource resource;
	private final String deploymentId;
	
	public DeploymentRegistration(Resource resource, String deploymentId)
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
	public boolean isSatisfiedBy(DeploymentRegistration requirement) {
		return deploymentId.equals(requirement.deploymentId);
	}

	@Override
	public Class<DeploymentRegistration> getCapabilityType() {
		return DeploymentRegistration.class;
	}
	
	@Override
	public String toString() {
		return "Deployment registration for " + deploymentId;
	}

}
