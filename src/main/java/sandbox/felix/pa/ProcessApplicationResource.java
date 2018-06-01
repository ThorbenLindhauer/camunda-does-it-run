package sandbox.felix.pa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;

import sandbox.felix.engine.JobExecutorRegistration;

public class ProcessApplicationResource implements Resource {

	private final List<Capability> capabilities;
	
	public ProcessApplicationResource(String name, Collection<String> registeredDeployments)
	{
		this.capabilities = new ArrayList<>();
		for (String deployment : registeredDeployments)
		{
			this.capabilities.add(new PaDeploymentRegistration(this, deployment, name));
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
