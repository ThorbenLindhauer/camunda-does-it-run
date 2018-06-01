package sandbox.felix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;

import sandbox.felix.engine.DeploymentRegistration;

public class ProcessDefinitionResource implements Resource
{
	private final ProcessDefinition definition;

    private final List<Capability> capabilities = new ArrayList<>();
    private final List<Requirement> requirements = new ArrayList<>();

    public ProcessDefinitionResource(
    		ProcessDefinition definition, 
    		BpmnModelInstance modelInstance,
    		boolean deploymentAwareJobExecutor)
    {
    	this.definition = definition;
    	capabilities.add(new ProcessDefinitionCapability(this, definition));
    	
    	Collection<CallActivity> callActivities = modelInstance.getModelElementsByType(CallActivity.class);
    	for (CallActivity callActivity : callActivities)
    	{
    		Requirement requirement = createCallActivityRequirement(callActivity);
    		requirements.add(requirement);
    	}
    	
        if (deploymentAwareJobExecutor)
        {
        	requirements.add(new DeploymentRegistration(this, definition.getDeploymentId()));
        }
    }

	private Requirement createCallActivityRequirement(CallActivity callActivity) {
		String binding = callActivity.getCamundaCalledElementBinding();
		
		final String calledKey = callActivity.getCalledElement();
		// TODO: handle expressions
		
		// TODO: binding can be null
		switch (binding) 
		{
			case "latest":
				return ProcessDefinitionRequirement.byKey(this, calledKey);
				
			case "version":
				String versionString = callActivity.getCamundaCalledElementVersion();
				int staticVersion = Integer.parseInt(versionString);
				// TODO: handle expressions
				return ProcessDefinitionRequirement.byKeyAndVersion(this, calledKey, staticVersion);
			case "deployment":
				// TODO: implement
			default:
				throw new RuntimeException("unsupported call activity binding type " + binding);
		}
	}

    @Override
    public List<Capability> getCapabilities(String namespace)
    {
        return capabilities;
    }

    @Override
    public List<Requirement> getRequirements(String namespace)
    {
        return requirements;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("[Process definition ");
        sb.append(definition.getId());
        sb.append("; ");
        sb.append("Provides: ");
        sb.append(capabilities);
        sb.append("; Requires: ");
        sb.append(requirements);
        sb.append("]");

        return sb.toString();
    }

    public ProcessDefinition getDefinition() {
		return definition;
	}
}
