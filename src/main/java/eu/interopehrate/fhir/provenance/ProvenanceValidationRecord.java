package eu.interopehrate.fhir.provenance;

/**
 *  Author: Engineering S.p.A. (www.eng.it)
 *  Project: InteropEHRate - www.interopehrate.eu
 *
 *  Description: represent the outcome of a validation of a FHIR Resource
 */
public class ProvenanceValidationRecord {

    private String resourceId;
    private boolean validated;
    private String message;

    public ProvenanceValidationRecord(String resourceId, boolean validated, String message) {
        this.resourceId = resourceId;
        this.validated = validated;
        this.message = message;
    }

    public String getResourceId() {
        return resourceId;
    }

    public boolean isValidated() {
        return validated;
    }

    public String getMessage() {
        return message;
    }

	@Override
	public String toString() {
		return "ProvenanceValidationRecord [resourceId=" + resourceId + ", validated=" + validated + ", message="
				+ message + "]";
	}
    
}