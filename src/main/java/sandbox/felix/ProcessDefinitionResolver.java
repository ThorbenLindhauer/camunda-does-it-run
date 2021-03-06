package sandbox.felix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.felix.resolver.Logger;
import org.apache.felix.resolver.ResolverImpl;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wiring;
import org.osgi.service.resolver.HostedCapability;
import org.osgi.service.resolver.ResolutionException;
import org.osgi.service.resolver.ResolveContext;

public class ProcessDefinitionResolver {

	private ProcessEngine engine;
	
	public ProcessDefinitionResolver(ProcessEngine engine)
	{
		this.engine = engine;
	}
	
	/**
	 * @return unresolved process definitions
	 */
	public List<UnresolvedProcessDefinition> resolveInCurrentState()
	{
		ResolverImpl resolver = new ResolverImpl(new Logger(0), 1);

        Collection<Resource> resources = new ArrayList<>();
        resources.add(ResourceFactory.forEngine(engine));
        resources.addAll(ResourceFactory.listDefinitions(engine));
        resources.addAll(ResourceFactory.listProcessApplications(engine));

        List<UnresolvedProcessDefinition> unresolvedProcessDefinitions = new ArrayList<>();
        
        ResolveContextImpl context = new ResolveContextImpl(resources);
        try {
			resolver.resolve(context);
		} catch (ResolutionException e) {
			Collection<Requirement> requirements = e.getUnresolvedRequirements();
			
			Map<ProcessDefinition, List<Requirement>> requirementsByProcdef = new HashMap<>();
			
			requirements.stream()
			.filter(r -> r.getResource() instanceof ProcessDefinitionResource)
			.forEach(r -> {
				ProcessDefinition definition = ((ProcessDefinitionResource) r.getResource()).getDefinition();
				CollectionUtil.addToMapOfLists(requirementsByProcdef, definition, r);
			});

			requirementsByProcdef.forEach((p, r) -> unresolvedProcessDefinitions.add(new UnresolvedProcessDefinition(p, r)));
		}
        
        return unresolvedProcessDefinitions;
	}
	
	private class ResolveContextImpl extends ResolveContext
	{
		
		private Collection<Resource> resources;
		
		public ResolveContextImpl(Collection<Resource> resources)
		{
			this.resources = resources;
		}

        @Override
        public Collection<Resource> getMandatoryResources()
        {
            return resources;
        }

        @Override
        public boolean isEffective(Requirement requirement)
        {
            return true;
        }

        @Override
        public int insertHostedCapability(List<Capability> capabilities, HostedCapability hostedCapability)
        {
            return 0;
        }

        @Override
        public Map<Resource, Wiring> getWirings()
        {
        	// TODO: should return wiring after resolution
            return Collections.emptyMap();
//            throw new UnsupportedOperationException();
        }

        @Override
        public List<Capability> findProviders(Requirement requirement)
        {
            return resources.stream()
                .flatMap(r -> r.getCapabilities(null).stream())
                .filter(c ->
                {
                    Map<String, Object> capabilityAttributes = c.getAttributes();
                    
                    if (requirement instanceof MatchingRequirement)
                    {
                    	MatchingRequirement matchingRequirement = ((MatchingRequirement) requirement);
                    	Class type = matchingRequirement.getCapabilityType();
                    	
                    	if (c.getClass().isAssignableFrom(type))
                    	{
                    		return matchingRequirement.isSatisfiedBy(c);
                    	}
                    	else
                    	{
                    		return false;
                    	}
                    }
                    else
                    {
                    	// attribute-based matching
                    	return 
                    		c.getNamespace().equals(requirement.getNamespace()) &&
                			requirement
		            			.getAttributes()
		            			.entrySet()
		            			.stream()
		            			.allMatch(e -> e.getValue().equals(capabilityAttributes.get(e.getKey())));
                    }
                    
                })
                .collect(Collectors.toList());
        }
    }
}
