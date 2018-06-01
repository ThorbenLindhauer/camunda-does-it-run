package sandbox.felix.pa;

import java.util.Collections;
import java.util.Map;

import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

import sandbox.felix.MatchingRequirement;
import sandbox.felix.Namespaces;

public class PaDeploymentRegistration implements MatchingRequirement<PaDeploymentRegistration>, Capability {

	private final Resource resource;
	
	private final String deploymentId;
	private final String paName;
	
	public PaDeploymentRegistration(Resource resource, String deploymentId, String paName)
	{
		this.resource = resource;
		this.deploymentId = deploymentId;
		this.paName = paName;
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
	public boolean isSatisfiedBy(PaDeploymentRegistration requirement) {
		return deploymentId.equals(requirement.deploymentId)
				&& paName.equals(requirement.paName);
	}

	@Override
	public Class<PaDeploymentRegistration> getCapabilityType() {
		return PaDeploymentRegistration.class;
	}
	
	@Override
	public String toString() {
		return "Registration of process application " + paName + " for deployment " + deploymentId;
	}

}
