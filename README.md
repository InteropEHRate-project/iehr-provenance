iehr-provenance

Library for Java Desktop for creating and validating resources with a linked Provenance.

This library has been developed in the context of the InteropEHRate European research 
project (www.interopehrate.eu).

Sample Usage

Run and inspect SuccesfullValidation.java and UnsuccesfullValidation.java for two complete
examples. Following a short sample:

	// #1 Initialize the Resource Signer with the Keystore (must be in the CLASSPATH)
	IParser parser = FhirContext.forR4().newJsonParser();
	ResourceSigner.INSTANCE.initialize(<KeyStoreFileName>, <CertificateAlias>, parser);
	
	// #2 Loads a JSON from the FS
	InputStream is = SuccessfullValidation.class.getClassLoader().getResourceAsStream("BundleToSign1.json");
	
	// #3 Converts the JSON stream to a Java Bundle Object
	Bundle bundle = (Bundle)parser.parseResource(is);
	
	// #4 Creates an instance of the BundleProvenanceBuilder
	BundleProvenanceBuilder builder = new BundleProvenanceBuilder(provider);
	
	// #5 Invokes method addProvenanceToBundleItems() to create signed provenances
	List<Provenance> provenances = builder.addProvenanceToBundleItems(bundle);
	
	// #6 Creates an instance of ProvenanceValidator
	ProvenanceValidator validator = new ProvenanceValidator(<KeyStoreFileName>, parser);
	ProvenanceValidationResults res = validator.validateBundle(bundle);
	logger.info("Validation outcome: " + res.isSuccessful());
	