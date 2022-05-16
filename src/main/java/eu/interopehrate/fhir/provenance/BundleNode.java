package eu.interopehrate.fhir.provenance;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;

/**
 *  Author: Engineering S.p.A. (www.eng.it)
 *  Project: InteropEHRate - www.interopehrate.eu
 *
 *  Description: Node implementation for a FHIR Resource of type Bundle
 */
public class BundleNode extends ResourceNode {
	
	
	BundleNode(Bundle resource) {
		super(resource);
	}

	@Override
	public void loadChildren(ResourceNode root) {
		ResourceType resourceType;
		Bundle bundle = (Bundle)resource;
		ResourceNode n;
		
		for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {  
        	resourceType = entry.getResource().getResourceType();
        	if (!NodeFactory.isAllowed(resourceType))
        		continue;
        	
        	if (entry.getResource().getId() == null) {
        		logger.warn("Found a resource without id, resource will not hava a provenance: " + 
        				entry.getResource().getResourceType());
        		continue;
        	}
        		
			n = root.searchNodeByResourceId(entry.getResource().getId());
			if (n == null) {
				n = NodeFactory.createNode(entry.getResource());
				n.loadChildren(this);
				n.setParent(this);
			}
        }	
	}
	
	
	public void print4Debug(int level) {
		StringBuilder spaces = new StringBuilder();
		for (int i = 1; i < level; i++)
			spaces.append("   ");
				
		System.out.println(spaces.toString() + resource.getResourceType().toString());
		
		level++;
		for (ResourceNode n : children)
			n.print4Debug(level);	
	}
}
