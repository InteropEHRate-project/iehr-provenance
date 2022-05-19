package eu.interopehrate.fhir.provenance;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Media;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

/**
 *  Author: Engineering S.p.A. (www.eng.it)
 *  Project: InteropEHRate - www.interopehrate.eu
 *
 *  Description: creates instances of nodes given a FHIR Resource
 */
public class NodeFactory {
	
	static ResourceType[] allowedTypes = {
			(new Bundle()).getResourceType(),
			(new Observation()).getResourceType(),
			(new DocumentReference()).getResourceType(),
			(new Media()).getResourceType(),
			(new Condition()).getResourceType(),
			(new Medication()).getResourceType(),
			(new MedicationStatement()).getResourceType(),
			(new MedicationRequest()).getResourceType(),
			(new Encounter()).getResourceType(),
			(new DiagnosticReport()).getResourceType(),
			(new Composition()).getResourceType(),
			(new CarePlan()).getResourceType(),
	};
	
	/**
	 * 
	 * @param resource
	 * @return
	 */
	public static ResourceNode createNode(Resource resource) {
		if (!isAllowed(resource.getResourceType()))
			throw new IllegalStateException(resource.getResourceType() + " cannot be represente as a node!");
		
		if (resource instanceof DiagnosticReport)
			return new DiagnosticReportNode((DiagnosticReport)resource);
		else if (resource instanceof Composition)
			return new CompositionNode((Composition)resource);
		else if (resource instanceof Bundle)
			return new BundleNode((Bundle)resource);
		else if (resource instanceof CarePlan)
			return new CarePlanNode((CarePlan)resource);
		else if (resource instanceof MedicationStatement)
			return new MedicationStatementNode((MedicationStatement)resource);
		else 
			return new ResourceNode(resource);
	}

	/**
	 * 
	 * @param resourceType
	 * @return
	 */
	public static boolean isAllowed(ResourceType resourceType) {
		for (ResourceType c : allowedTypes) {
			if (c.equals(resourceType))
				return true;
		}
		
		return false;
	}
}
