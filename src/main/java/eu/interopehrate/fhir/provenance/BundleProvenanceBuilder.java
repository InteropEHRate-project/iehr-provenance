package eu.interopehrate.fhir.provenance;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Resource;

/**
 *  Author: Engineering S.p.A. (www.eng.it)
 *  Project: InteropEHRate - www.interopehrate.eu
 *
 *  Description: Creates signed Provenances to the items of a bundle that must be signed.
 *               Created Provenances are added to the Bundle.
 */
public class BundleProvenanceBuilder {
	
	protected final Logger logger = Logger.getLogger(getClass().getName());
	private Organization providerOrg;
	
	public BundleProvenanceBuilder(Organization providerOrg) {
		this.providerOrg = providerOrg;
	}
	
	/**
	 * Inspects a bundle and build the Provenance information for each 
	 * resource that must be signed.
	 * 
	 * @param bundle
	 * @param provider
	 * @throws Exception
	 */
	public List<Provenance> addProvenanceToBundleItems(Bundle bundle) throws Exception {
		// #1 creates a tree representation of the items in the bundle
		// recreating a hierarchical structure, simpler to navigate.
		ResourceNode root = createTree(bundle);
		
		// #2 Adds the provenance(s) with the signature to each item at level 1 
		List<Provenance> provenances = addProvenanceToTree(root);
		
		
		// #4 adds all the provenances created and the provider to the bundle
        for (Provenance p : provenances)
           	bundle.addEntry().setResource(p);
        
        // #5 checks if Organization needs to be added to the bundle
        boolean found = false;
		String tmpId; 
        for (BundleEntryComponent entry : bundle.getEntry()) {
        	if (entry.getResource() instanceof Organization) {
        		tmpId = entry.getResource().getIdElement().getIdPart();
        		if (tmpId == null)
        			continue;
        		
        		if (providerOrg.getIdElement().getIdPart() == null)
        			continue;
        		
        		if (providerOrg.getIdElement().getIdPart().equals(tmpId)) {
        			found = true;
        			break;
        		}
        	}
        }
        if (!found)
        	bundle.addEntry().setResource(providerOrg);
        
        return provenances;
	}
	
	
	private List<Provenance> addProvenanceToTree(ResourceNode root) throws Exception {
		final List<Provenance> provenances = new ArrayList<Provenance>();
		Provenance provenance;
		Resource resource = null;
		String jwsToken;
		
		for (ResourceNode child : root.getChildren()) {
			if (logger.isLoggable(Level.FINE))
				logger.fine("Creating provenance for resource: " + child.getResource().getId());		
			// Adds the provenance to the child
			provenance = child.addProvenance(providerOrg);
			try {
				// Gets the resource linked to the current node
				resource = child.getResource();
				// signs the resource and creates the jwsToken
				jwsToken = ResourceSigner.INSTANCE.createJWSToken(resource);
				// adds the jwsToken to the provenance
				provenance.getSignatureFirstRep().setData(jwsToken.getBytes());
				// adds the provenance to the list of created provenances
				provenances.add(provenance);
			} catch (Exception e) {
				logger.severe(String.format("Error %s while creating signature for %s.", 
						e.getMessage(), resource.getId()));
			}
		}
		
		return provenances;
	}
	
	
	private ResourceNode createTree(Bundle bundle) {
		ResourceNode root = NodeFactory.createNode(bundle);
		root.loadChildren(root);
		return root;
	}

}
