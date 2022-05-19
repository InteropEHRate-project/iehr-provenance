package eu.interopehrate.fhir.provenance;

import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CarePlan.CarePlanActivityComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Resource;

/**
 *  Author: Engineering S.p.A. (www.eng.it)
 *  Project: InteropEHRate - www.interopehrate.eu
 *
 *  Description: Node implementation for a FHIR Resource of type CarePlan
 */
public class CarePlanNode extends ResourceNode {
	
	CarePlanNode(CarePlan resource) {
		super(resource);
	}

	@Override
	public void loadChildren(ResourceNode root) {
		CarePlan carePlan = (CarePlan) resource;
		
		Resource currentResource;
		for (CarePlanActivityComponent act : carePlan.getActivity()) {
			currentResource = (Resource) act.getReference().getResource();
			if (currentResource == null) {
        		logger.warn("Found a reference pointing to a null resource: " + act.getReference());
				continue;
			}
			
        	if (!NodeFactory.isAllowed(currentResource.getResourceType()))
        		continue;
        	
        	if (currentResource.getId() == null) {
        		logger.warn("Found a resource without id, resource will not hava a provenance: " + 
        				currentResource.getResourceType());
        		continue;
        	}
        	
        	ResourceNode n = root.searchNodeByResourceId(currentResource.getId());
			if (n == null) {
				n = NodeFactory.createNode(currentResource);
				n.loadChildren(this);
			}
			n.setParent(this);
        }
	}

	Provenance addProvenance(DomainResource provider) {
		CarePlan carePlan = (CarePlan)resource;
		
		DomainResource author = provider;
		if (carePlan.getAuthor() != null)
			author = (DomainResource)carePlan.getAuthor().getResource();
		// creates the Provenance
		Provenance provenance = ProvenanceBuilder.build(carePlan, author, provider);
		// adds extension to children
		Extension provExt = carePlan.getExtensionByUrl(ProvenanceBuilder.PROV_EXT_NAME);
		for (ResourceNode child : children)
			child.addProvenanceExtension(provExt);
		
		return provenance;
	}
	
}
