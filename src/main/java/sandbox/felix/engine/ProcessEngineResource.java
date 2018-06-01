package sandbox.felix.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;

public class ProcessEngineResource implements Resource {

	private final List<Capability> capabilities;
	
	public ProcessEngineResource(Collection<String> registeredDeployments)
	{
		this.capabilities = new ArrayList<>();
		for (String deployment : registeredDeployments)
		{
			this.capabilities.add(new DeploymentRegistration(this, deployment));
		}
	}
	
	@Override
	public List<Capability> getCapabilities(String namespace) {
		return capabilities;
	}

	@Override
	public List<Requirement> getRequirements(String namespace) {
		return Collections.emptyList();
	}

}
