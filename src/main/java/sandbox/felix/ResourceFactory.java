package sandbox.felix;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import sandbox.felix.engine.ProcessEngineResource;

public class ResourceFactory {

	public static List<ProcessDefinitionResource> listDefinitions(ProcessEngine engine)
	{
		RepositoryService repositoryService = engine.getRepositoryService();
		ProcessEngineConfiguration engineConfiguration = engine.getProcessEngineConfiguration();
		boolean jobExecutorDeploymentAware = engineConfiguration.isJobExecutorDeploymentAware();

		List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
		List<ProcessDefinitionResource> resources = new ArrayList<>();
		
		for (ProcessDefinition definition : processDefinitions)
		{
			BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(definition.getId());
			
			ProcessDefinitionResource resource = 
					new ProcessDefinitionResource(
						definition,
						modelInstance,
						jobExecutorDeploymentAware);
			resources.add(resource);
		}
		
		return resources;
	}
	
	public static ProcessEngineResource forEngine(ProcessEngine engine)
	{
		Set<String> registeredDeployments = engine.getManagementService().getRegisteredDeployments();
		return new ProcessEngineResource(registeredDeployments);
	}
}
