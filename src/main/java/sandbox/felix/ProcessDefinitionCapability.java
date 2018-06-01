package sandbox.felix;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

public class ProcessDefinitionCapability implements Capability
{

    private final String key;
    private final String id;
    private final int version;
    private final boolean isLatestVersion;
    
    protected final ProcessDefinitionResource declaring;

    public ProcessDefinitionCapability(
    		ProcessDefinitionResource declaring, 
    		ProcessDefinition processDefinition)
    {
        this.key = processDefinition.getKey();
        this.id = processDefinition.getId();
        this.version = processDefinition.getVersion();
        this.declaring = declaring;
        
        // TODO: populate properly
        this.isLatestVersion = true;
    }

    @Override
    public String getNamespace()
    {
        return Namespaces.PROC_DEF_NS;
    }

    @Override
    public Map<String, String> getDirectives()
    {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getAttributes()
    {
    	Map<String, Object> attributes = new HashMap<>();

    	attributes.put("key", key);
    	attributes.put("id", id);
    	attributes.put("version", version);
    	
    	return attributes;
    }

    @Override
    public Resource getResource()
    {
        return declaring;
    }

    @Override
    public String toString()
    {
        return getAttributes().toString();
    }
    
    public String getKey() {
		return key;
	}
    
    public String getId() {
		return id;
	}
    
    public int getVersion() {
		return version;
	}
    
    public boolean isLatestVersion() {
		return isLatestVersion;
	}
}
