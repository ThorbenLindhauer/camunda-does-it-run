package org.camunda.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.Rule;
import org.junit.Test;

import sandbox.felix.ProcessDefinitionResolver;
import sandbox.felix.UnresolvedProcessDefinition;

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
		Collection<UnresolvedProcessDefinition> unresolvedDefinitions = resolver.resolveInCurrentState();
		
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
		Collection<UnresolvedProcessDefinition> unresolvedDefinitions = resolver.resolveInCurrentState();
		
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
		Collection<UnresolvedProcessDefinition> unresolvedDefinitions = resolver.resolveInCurrentState();
		
		// then
		assertThat(unresolvedDefinitions).hasSize(1);
		System.out.println(unresolvedDefinitions);
	}
	
	@Test
	public void resolveUnsatisifedJobExecutorRegistration()
	{
		// given
		String deploymentId = deploy(ONE_TASK_PROCESS);
		rule.getManagementService().unregisterDeploymentForJobExecutor(deploymentId);
		
		ProcessDefinitionResolver resolver = new ProcessDefinitionResolver(rule.getProcessEngine());
		
		// when
		Collection<UnresolvedProcessDefinition> unresolvedDefinitions = resolver.resolveInCurrentState();
		
		// then
		assertThat(unresolvedDefinitions).hasSize(1);
		System.out.println(unresolvedDefinitions);
		
	}
	
	@Test
	public void resolveUnsatifiedPaRegistration()
	{
		// given
		BpmnModelInstance copy = ONE_TASK_PROCESS.clone();
		addProperty(copy, "oneTaskProcess", "process-application", TestProcessApplication.NAME);
		deploy(copy);
		
		ProcessDefinitionResolver resolver = new ProcessDefinitionResolver(rule.getProcessEngine());
		
		// when
		List<UnresolvedProcessDefinition> unresolvedDefinitions = resolver.resolveInCurrentState();
		
		// then
		assertThat(unresolvedDefinitions).hasSize(1);
		System.out.println(unresolvedDefinitions);
		
	}
	

	@Test
	public void resolveSatifiedPaRegistration()
	{
		// given
		BpmnModelInstance copy = ONE_TASK_PROCESS.clone();
		addProperty(copy, "oneTaskProcess", "process-application", TestProcessApplication.NAME);
		String deploymentId = deploy(copy);
		
		TestProcessApplication pa = new TestProcessApplication();
		pa.deploy();
		rule.getManagementService().registerProcessApplication(deploymentId, pa.getReference());
		
		ProcessDefinitionResolver resolver = new ProcessDefinitionResolver(rule.getProcessEngine());
		
		// when
		Collection<UnresolvedProcessDefinition> unresolvedDefinitions = resolver.resolveInCurrentState();
		
		// then
		assertThat(unresolvedDefinitions).hasSize(0);
		
	}
	
	private static void addProperty(BpmnModelInstance modelInstance, String elementId, String key, String value)
	{
		BaseElement elementInstance = modelInstance.getModelElementById(elementId);
		ExtensionElements extensionElements = getOrInitChildElement(
				elementInstance, 
				ExtensionElements.class, 
				e -> e.getExtensionElements());
		
		ModelElementInstance properties = getOrInitChildElement(
				extensionElements, 
				CamundaProperties.class, 
				e -> (CamundaProperties) e.getUniqueChildElementByType(CamundaProperties.class));
		
		CamundaProperty property = modelInstance.newInstance(CamundaProperty.class);
		property.setCamundaName(key);
		property.setCamundaValue(value);
		properties.addChildElement(property);
	}

	private static <P extends BpmnModelElementInstance, C extends BpmnModelElementInstance> C getOrInitChildElement(
			P element,
			Class<C> elementType,
			Function<P, C> accessor) {
		
		C child = accessor.apply(element);
		
		if (child == null)
		{
			ModelInstance modelInstance = element.getModelInstance();
			child = modelInstance.newInstance(elementType);
			element.addChildElement(child);
		}

		return child;
	}
}
