package eu.interopehrate.fhir.provenance.test;

import java.io.InputStream;

import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import eu.interopehrate.fhir.provenance.NodeFactory;
import eu.interopehrate.fhir.provenance.ProvenanceValidationRecord;
import eu.interopehrate.fhir.provenance.ProvenanceValidationResults;
import eu.interopehrate.fhir.provenance.ProvenanceValidator;
import eu.interopehrate.fhir.provenance.ResourceNode;

public class ValidateBundle {
	private static final String KEY_STORE = "keystore.p12";

	static {
		System.setProperty("org.slf4j.simpleLogger.log.eu.interopehrate.fhir.provenance", "DEBUG");
		System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
		System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");		
	}

	public static void main(String[] args) throws Exception {
		Logger logger = LoggerFactory.getLogger(ValidateBundle.class);
		IParser parser = FhirContext.forR4().newJsonParser();
		InputStream is = SuccessfullValidation.class.getClassLoader().getResourceAsStream("HCPAppComposition.json");
		Bundle bundle = (Bundle)parser.parseResource(is);
		
        // **** Only for DEBUG purposes!!
        ResourceNode bundleTree = NodeFactory.createNode(bundle);
        bundleTree.loadChildren(bundleTree);
        bundleTree.print4Debug();
        // **** Only for DEBUG purposes!!
        
		// #6 Validates the resources in the Bundle
		ProvenanceValidator validator = new ProvenanceValidator(KEY_STORE, parser);
		ProvenanceValidationResults res = validator.validateBundle(bundle);
		logger.info("Validation outcome: " + res.isSuccessful());

		for (ProvenanceValidationRecord r : res.getValidationResult()) {
			logger.info(r.toString());
		}
		
	}
}
