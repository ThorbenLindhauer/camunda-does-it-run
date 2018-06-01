package sandbox.felix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.osgi.resource.Resource;

import sandbox.felix.engine.ProcessEngineResource;
import sandbox.felix.pa.ProcessApplicationResource;

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
	
	public static List<Resource> listProcessApplications(ProcessEngine engine)
	{
		
		ManagementService managementService = engine.getManagementService();
		List<Deployment> deployments = engine.getRepositoryService().createDeploymentQuery().list();
		
		Map<String, Set<String>> deploymentsByPa = new HashMap<>();
		
		for (Deployment deployment : deployments)
		{
			String paName = managementService.getProcessApplicationForDeployment(deployment.getId());
			
			CollectionUtil.addToMapOfSets(deploymentsByPa, paName, deployment.getId());
		}
		
		List<Resource> applicationResources = new ArrayList<>();
		
		for (String paName : deploymentsByPa.keySet())
		{
			applicationResources.add(new ProcessApplicationResource(paName, 
					deploymentsByPa.getOrDefault(paName, Collections.emptySet())));
		}
		
		return applicationResources;
	}
}
