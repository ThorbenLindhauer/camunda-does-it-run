package sandbox.felix;

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

public interface MatchingRequirement<T extends Capability> extends Requirement {

	boolean isSatisfiedBy(T requirement);
	
	Class<T> getCapabilityType();
}
