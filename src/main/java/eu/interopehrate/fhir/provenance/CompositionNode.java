package eu.interopehrate.fhir.provenance;

import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Composition.SectionComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

/**
 *  Author: Engineering S.p.A. (www.eng.it)
 *  Project: InteropEHRate - www.interopehrate.eu
 *
 *  Description: Node implementation for a FHIR Resource of type Composition
 */
public class CompositionNode extends ResourceNode {
	
	CompositionNode(Composition resource) {
		super(resource);
	}

	@Override
	public void loadChildren(ResourceNode root) {
		Composition composition = (Composition) resource;

		Resource currentResource;
		for (SectionComponent currentSection : composition.getSection()) {

			for (Reference currentRef : currentSection.getEntry()) {
				currentResource = (Resource) currentRef.getResource();
				if (currentResource == null) {
	        		logger.warn("Found a reference pointing to a null resource: " + currentRef.getReference());
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
	}

	Provenance addProvenance(DomainResource provider) {
		Composition composition = (Composition)resource;
		
		DomainResource author = provider;
		if (composition.getAuthor().size() > 0)
			author = (DomainResource)composition.getAuthorFirstRep().getResource();
		// creates the Provenance
		Provenance provenance = ProvenanceBuilder.build(composition, author, provider);
		// adds extension to children
		Extension provExt = composition.getExtensionByUrl(ProvenanceBuilder.PROV_EXT_NAME);
		for (ResourceNode child : children)
			child.addProvenanceExtension(provExt);
		
		return provenance;
	}
	
}
