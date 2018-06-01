package sandbox.felix;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.osgi.resource.Resource;

public abstract class ProcessDefinitionRequirement implements MatchingRequirement<ProcessDefinitionCapability>
{
    protected final ProcessDefinitionResource declaring;

    public ProcessDefinitionRequirement(ProcessDefinitionResource declaring)
    {
        this.declaring = declaring;
    }

    @Override
    public String getNamespace()
    {
        return Namespaces.PROC_DEF_NS;
    }

    // TODO: what is this good for?
    @Override
    public Map<String, String> getDirectives()
    {
        return Collections.emptyMap();
    }

    @Override
    public Resource getResource()
    {
        return declaring;
    }

	@Override
	public Class<ProcessDefinitionCapability> getCapabilityType() {
		return ProcessDefinitionCapability.class;
	}
	
	public static ProcessDefinitionRequirement byKey(ProcessDefinitionResource resource, String key)
	{
		return new DefinitionByKey(resource, key);
	}

	public static ProcessDefinitionRequirement byKeyAndVersion(ProcessDefinitionResource resource, String key, int version)
	{
		return new DefinitionByKeyAndVersion(resource, key, version);
	}
	
	@Override
	public String toString() {
		return "[Process definition: " + getAttributes() + "]";
	}
	
	// TODO: key and deployment
	
	
	public static final class DefinitionByKey extends ProcessDefinitionRequirement
	{
		private String key;
		
		public DefinitionByKey(ProcessDefinitionResource resource, String key)
		{
			super(resource);
			this.key = key;
		}

		@Override
		public boolean isSatisfiedBy(ProcessDefinitionCapability requirement) {
			return key.equals(requirement.getKey()) && requirement.isLatestVersion();
		}

		@Override
		public Map<String, Object> getAttributes() {
			Map<String, Object> attributes = new HashMap<>();
			
			attributes.put("key", key);
			attributes.put("latestVersion", true);
			
			return attributes;
		}
	}
	

	public static final class DefinitionByKeyAndVersion extends ProcessDefinitionRequirement
	{
		private String key;
		private int version;
		
		public DefinitionByKeyAndVersion(ProcessDefinitionResource resource, String key, int version)
		{
			super(resource);
			this.key = key;
			this.version = version;
		}

		@Override
		public boolean isSatisfiedBy(ProcessDefinitionCapability requirement) {
			return key.equals(requirement.getKey()) && requirement.getVersion() == version;
		}

		@Override
		public Map<String, Object> getAttributes() {
			Map<String, Object> attributes = new HashMap<>();
			
			attributes.put("key", key);
			attributes.put("version", version);
			
			return attributes;
		}
		
	}

}
