package eu.interopehrate.fhir.provenance;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Resource;

/**
 *  Author: Engineering S.p.A. (www.eng.it)
 *  Project: InteropEHRate - www.interopehrate.eu
 *
 *  Description: Node implementation for a generic FHIR Resource without children
 *               (Observation, Condition, etc. etc.)
 */
public class ResourceNode {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	protected ResourceNode parent;
	protected Resource resource;
	protected List<ResourceNode> children = new ArrayList<ResourceNode>();
		
	ResourceNode (Resource resource) {
		this.resource = resource;
	}
	
	public Resource getResource() {
		return resource;
	}

	void setParent(ResourceNode parent) {
		if (this.parent != null)
			this.parent.removeChild(this);			
		
		this.parent = parent;
		parent.addChild(this);
	}
		
	public List<ResourceNode> getChildren() {
		return children;
	}
	
	void addChild (ResourceNode n) {
		children.add(n);
	}
	
	void removeChild(ResourceNode n) {
		children.remove(n);
	}
	
	/*
	 * 
	 */
	ResourceNode searchNodeByResourceId(String id) {
		if (resource != null && id.equals(resource.getId()))
			return this;
		else {
			ResourceNode n;
			for (ResourceNode c : children) {
				n = c.searchNodeByResourceId(id);
				if (n != null)
					return n;
			}	
		}
		
		return null;
	}
	
	/*
	 * Default implementation for node that does not have children
	 */
	public void loadChildren(ResourceNode root) {}
	
	/*
	 * 
	 */
	Provenance addProvenance(DomainResource provider) {
		return ProvenanceBuilder.build((DomainResource)this.getResource(), provider, provider);
	}
	
	/*
	 * 
	 */
	void addProvenanceExtension(Extension provExt) {
		((DomainResource)resource).addExtension(provExt);

		for (ResourceNode c : children) {
			c.addProvenanceExtension(provExt);
		}	
	}

	public void print4Debug() {
		print4Debug(1);
	}
	
	void print4Debug(int level) {
		StringBuilder spaces = new StringBuilder();
		for (int i = 1; i < level; i++)
			spaces.append("|---");
				
		System.out.println(spaces.toString() + resource.getId());
		
		level++;
		for (ResourceNode n : children)
			n.print4Debug(level);	
	}

}
