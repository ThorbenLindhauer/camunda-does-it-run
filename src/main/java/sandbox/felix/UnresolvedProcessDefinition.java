package sandbox.felix;

import java.util.List;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.osgi.resource.Requirement;

public class UnresolvedProcessDefinition {
	
	
	private final ProcessDefinition processDefinition;
	private final List<Requirement> unresolvedRequirements;
	
	public UnresolvedProcessDefinition(ProcessDefinition processDefinition, List<Requirement> unresolvedRequirements)
	{
		this.processDefinition = processDefinition;
		this.unresolvedRequirements = unresolvedRequirements;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Process definition " + processDefinition.getId() + " has unsatisfied requirements:");
		for (Requirement requirement : unresolvedRequirements)
		{
			sb.append("\n");
			sb.append(requirement);
		}
		
		return sb.toString();
	}
}
