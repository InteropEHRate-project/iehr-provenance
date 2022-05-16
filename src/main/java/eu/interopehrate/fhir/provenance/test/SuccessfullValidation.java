package eu.interopehrate.fhir.provenance.test;

import java.io.InputStream;
import java.util.List;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Provenance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import eu.interopehrate.fhir.provenance.BundleProvenanceBuilder;
import eu.interopehrate.fhir.provenance.NodeFactory;
import eu.interopehrate.fhir.provenance.ProvenanceValidationRecord;
import eu.interopehrate.fhir.provenance.ProvenanceValidationResults;
import eu.interopehrate.fhir.provenance.ProvenanceValidator;
import eu.interopehrate.fhir.provenance.ResourceNode;
import eu.interopehrate.fhir.provenance.ResourceSigner;

public class SuccessfullValidation {

	static {
		System.setProperty("org.slf4j.simpleLogger.log.eu.interopehrate.fhir.provenance", "DEBUG");
		System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
		System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");		
	}

	public static void main(String[] args) throws Exception {
		Logger logger = LoggerFactory.getLogger(SuccessfullValidation.class);
		// #1 Initalize ResourceSigner, MUST BE DONE ONCE, and MUST BE DONE 
		//    ONLY IF THERE IS THE NEED TO CREATE SIGNATURES
		IParser parser = FhirContext.forR4().newJsonParser();
		// FTGM_iehr.p12 is in src/main/resources
		ResourceSigner.INSTANCE.initialize("FTGM_iehr.p12", "FTGM_iehr", parser);
		
		// #2 Load file to be signed and creates the instance of Bundle
		/*
		 * This Bundle has the following structure: 
		 * Bundle
		 * |--- DiagnosticReport
		 * |--- |--- Observation
		 * |--- |--- Observation
		 * |--- Observation
		 * 
		 * At the end of the building of the provenances only 2 objects must be signed.
		 */
		InputStream is = SuccessfullValidation.class.getClassLoader().getResourceAsStream("BundleToSign1.json");
		Bundle bundle = (Bundle)parser.parseResource(is);

		// #3 Creates the provider Organization
		Organization provider = new Organization();
		provider.setId("1");
        provider.addIdentifier(new Identifier().setValue("FTGM"));
        provider.setActive(true);
        provider.setName("Fondazione Gabriele Monasterio");
        provider.addAddress().addLine("Via Rossi, 45")
        .setCity("Pisa")
        .setState("Italy")
        .setPostalCode("00675")
        .setUse(Address.AddressUse.WORK);
		
        // **** Only for DEBUG purposes!!
        ResourceNode bundleTree = NodeFactory.createNode(bundle);
        bundleTree.loadChildren(bundleTree);
        bundleTree.print4Debug();
        // **** Only for DEBUG purposes!!
        
		// #4 Signs the resources in the Bundle
		BundleProvenanceBuilder builder = new BundleProvenanceBuilder(provider);		
		List<Provenance> provenances = builder.addProvenanceToBundleItems(bundle);
		logger.info("Provenances size equals to 2: " + (provenances.size() == 2));		
		
		// #6 Validates the resources in the Bundle
		ProvenanceValidator validator = new ProvenanceValidator("FTGM_iehr.p12", parser);
		ProvenanceValidationResults res = validator.validateBundle(bundle);
		logger.info("Validation outcome: " + res.isSuccessful());

		for (ProvenanceValidationRecord r : res.getValidationResult()) {
			logger.info(r.toString());
		}
	}

}
