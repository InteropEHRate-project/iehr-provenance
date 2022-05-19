package eu.interopehrate.fhir.provenance;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Provenance;

/**
 *  Author: Engineering S.p.A. (www.eng.it)
 *  Project: InteropEHRate - www.interopehrate.eu
 *
 *  Description: Node implementation for a FHIR Resource of type MedicationStatement
 */
public class MedicationStatementNode extends ResourceNode {
	
	MedicationStatementNode (MedicationStatement resource) {
		super(resource);
		this.resource = resource;
	}

	@Override
	public void loadChildren(ResourceNode root) {
		MedicationStatement ms = (MedicationStatement)resource;
		
		if (ms.getMedicationReference() == null)
			return;
		
		Medication medication = (Medication)ms.getMedicationReference().getResource();
    	if (medication == null) {
    		logger.warn("Found a reference pointing to a null resource: " + ms.getMedicationReference());
    		return;
    	}
    	
    	if (!NodeFactory.isAllowed(medication.getResourceType()))
    		return;

    	if (medication.getId() == null) {
    		logger.warn("Found a resource without id, resource will not hava a provenance: " + 
    				medication.getResourceType());
    		return;
    	}
		
		ResourceNode n = root.searchNodeByResourceId(medication.getId());
		if (n == null) {
			n = NodeFactory.createNode(medication);
		    n.loadChildren(this);
		} 
	    n.setParent(this);
	}
	
	
	Provenance addProvenance(DomainResource provider) {
		MedicationStatement ms = (MedicationStatement)resource;
		
		// creates the Provenance
		Provenance provenance = ProvenanceBuilder.build(ms, provider, provider);
		// adds extension to children
		Extension provExt = ms.getExtensionByUrl(ProvenanceBuilder.PROV_EXT_NAME);
		for (ResourceNode child : children)
			child.addProvenanceExtension(provExt);
		
		return provenance;
	}
	
}
