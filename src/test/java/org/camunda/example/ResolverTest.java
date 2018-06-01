package org.camunda.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.junit.Rule;
import org.junit.Test;

import sandbox.felix.ProcessDefinitionResolver;

public class ResolverTest {

	private static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess("oneTaskProcess")
			.startEvent()
			.userTask()
			.endEvent()
			.done();
	
	private static final BpmnModelInstance CALL_ACTIVITY_PROCESS = Bpmn.createExecutableProcess("callActivityProcess")
			.startEvent()
			.callActivity()
				.calledElement("oneTaskProcess")
				.camundaCalledElementBinding("latest")
			.endEvent()
			.done();
	
	@Rule
	public ProcessEngineRule rule = new ProcessEngineRule();

	private String deploy(BpmnModelInstance... instances)
	{
		RepositoryService repositoryService = rule.getRepositoryService();
		DeploymentBuilder builder = repositoryService.createDeployment();
		
		for (BpmnModelInstance modelInstance : instances)
		{
			Process process = modelInstance.getModelElementsByType(Process.class).iterator().next();
			builder.addModelInstance(process.getId() + ".bpmn", modelInstance);
		}
		
		Deployment deployment = builder.deploy();
		rule.manageDeployment(deployment);
		
		rule.getManagementService().registerDeploymentForJobExecutor(deployment.getId());
		
		return deployment.getId();
	}
	
	@Test
	public void resolvePlainProcessDefinition()
	{
		// given
		deploy(ONE_TASK_PROCESS);
		ProcessDefinitionResolver resolver = new ProcessDefinitionResolver(rule.getProcessEngine());
		
		// when
		Collection<ProcessDefinition> unresolvedDefinitions = resolver.resolveInCurrentState();
		
		// then
		assertThat(unresolvedDefinitions).isEmpty();
	}
	
	
	@Test
	public void resolveSatisfiedCallActivity()
	{
		// given
		deploy(ONE_TASK_PROCESS, CALL_ACTIVITY_PROCESS);
		ProcessDefinitionResolver resolver = new ProcessDefinitionResolver(rule.getProcessEngine());
		
		// when
		Collection<ProcessDefinition> unresolvedDefinitions = resolver.resolveInCurrentState();
		
		// then
		assertThat(unresolvedDefinitions).isEmpty();
	}
	
	@Test
	public void resolveUnsatisfiedCallActivity()
	{
		// given
		deploy(CALL_ACTIVITY_PROCESS);
		ProcessDefinitionResolver resolver = new ProcessDefinitionResolver(rule.getProcessEngine());
		
		// when
		Collection<ProcessDefinition> unresolvedDefinitions = resolver.resolveInCurrentState();
		
		// then
		assertThat(unresolvedDefinitions).hasSize(1);
	}
	
	@Test
	public void resolveUnsatisifedJobExecutorRegistration()
	{
		// given
		String deploymentId = deploy(ONE_TASK_PROCESS);
		rule.getManagementService().unregisterDeploymentForJobExecutor(deploymentId);
		
		ProcessDefinitionResolver resolver = new ProcessDefinitionResolver(rule.getProcessEngine());
		
		// when
		Collection<ProcessDefinition> unresolvedDefinitions = resolver.resolveInCurrentState();
		
		// then
		assertThat(unresolvedDefinitions).hasSize(1);
		
	}
}
