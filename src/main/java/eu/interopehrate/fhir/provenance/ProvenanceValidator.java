package eu.interopehrate.fhir.provenance;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Provenance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.parser.IParser;
import iehr.security.CryptoManagementFactory;
import iehr.security.api.CryptoManagement;

/**
 *  Author: Engineering S.p.A. (www.eng.it)
 *  Project: InteropEHRate - www.interopehrate.eu
 *
 *  Description: executes the validation of the received bundle.
 */
public class ProvenanceValidator {
    public static final String NOT_VALIDATED_MSG = "Resource does not match with Provenance.";
    public static final String NOT_VALIDABLE_MSG = "Resource does not have a related Provenance.";
    public static final Logger LOGGER = LoggerFactory.getLogger(ProvenanceValidator.class.getName());
    
    private CryptoManagement cryptoManagement;
    private IParser parser;

    public ProvenanceValidator(String keyStoreName, IParser parser) {
        cryptoManagement = CryptoManagementFactory.create(Constants.IEHR_CA_URL, keyStoreName);

        this.parser = parser;
        this.parser.setPrettyPrint(false);
    }

    /**
     * Validates the provenance of the resources
     *
     * @param theBundle
     * @return
     * @throws Exception
     */
    public ProvenanceValidationResults validateBundle(Bundle theBundle) throws Exception {
        ProvenanceValidationResults res = new ProvenanceValidationResults();
    	parser.setPrettyPrint(false);

        // Retrieves the signed provenances from the bundle
        List<Provenance> provenances = extractSignedProvenances(theBundle);

        // Creates the resource tree. The first level of this tree contains the
        // resources that MUST have a Provenance
        ResourceNode root = NodeFactory.createNode(theBundle);
        root.loadChildren(root);

        // Starts checking the provenance for each resource
        DomainResource resourceToValidate;
        String jwsToken;
        String encodedResource;
        boolean foundProvenance = false;
        for (ResourceNode child : root.getChildren()) {
            resourceToValidate = (DomainResource)child.getResource();
            // Looks for provenance of the current resource
            foundProvenance = false;
            for (Provenance provenance : provenances) {
                if (matches(provenance, resourceToValidate)) {
                    foundProvenance = true;
                    jwsToken = new String(provenance.getSignatureFirstRep().getData());
                    encodedResource = parser.encodeResourceToString(resourceToValidate);
                    if (cryptoManagement.verifyDetachedJws(jwsToken, encodedResource))
                        res.addValidationResult(resourceToValidate.getId(), true, "");
                    else
                        res.addValidationResult(resourceToValidate.getId(), false, NOT_VALIDATED_MSG);

                    break;
                }
            }

            if (!foundProvenance) {
                LOGGER.error("Provenance for {} NOT found in bundle. Resource is not valid.", resourceToValidate.getId());
                res.addValidationResult(resourceToValidate.getId(), false, NOT_VALIDABLE_MSG);
            }
        }

        return res;
    }

    /**
     * Retrieves signed Provenance in the bundle. Each signed Provenance represent
     * an health data to be validated.
     *
     * @param theBundle
     * @return
     */
    private List<Provenance> extractSignedProvenances(Bundle theBundle) {
        List<Provenance> provenances = new ArrayList<Provenance>();

        // Extracts the list of Provenance
        Provenance currentProvenance;
        for (Bundle.BundleEntryComponent entry : theBundle.getEntry()) {
            if (entry.getResource()instanceof Provenance) {
                currentProvenance = (Provenance) entry.getResource();
                if (currentProvenance.getSignatureFirstRep().getData() != null)
                    provenances.add((Provenance) entry.getResource());
            }
        }

        return provenances;
    }

    private boolean matches (Provenance provenance, DomainResource resource) {
    	if (provenance.getTarget().size() == 0)
    		return false;
    	
    	DomainResource target = (DomainResource) provenance.getTargetFirstRep().getResource();
    	final String targetId = target.getResourceType() + "/" + target.getIdElement().getIdPart();
    	final String resourceId = resource.getResourceType() + "/" + resource.getIdElement().getIdPart();
        // LOGGER.debug("resourceId: {}, targetId: {}", resourceId, targetId);
        return resourceId.equals(targetId);
    }
    
}