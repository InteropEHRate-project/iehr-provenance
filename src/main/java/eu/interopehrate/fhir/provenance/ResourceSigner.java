package eu.interopehrate.fhir.provenance;

import java.security.PrivateKey;

import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.parser.IParser;
import iehr.security.CryptoManagementFactory;
import iehr.security.api.CryptoManagement;

/**
 *  Author: Engineering S.p.A. (www.eng.it)
 *  Project: InteropEHRate - www.interopehrate.eu
 *
 *  Description: cretes a JWS signature for a FHIR Resource
 */
public class ResourceSigner {
	
	public static final ResourceSigner INSTANCE = new ResourceSigner();
	private CryptoManagement cryptoManagement;
	private PrivateKey privateKey;
	private byte[] certificateData;
	private IParser parser;

	private ResourceSigner() {}
	
	/**
	 * Used to initialize the ResourceSigner and the security library.
	 * 
	 * @param keyStoreName: name of the keystore (it is searched over the classpath)
	 * @param certAlias: alias of the certificate/private key
	 * @param parser: FHIR parser to covnert resources to String
	 * 
	 * @throws Exception
	 */
	public void initialize(String keyStoreName, String certAlias, IParser parser) throws Exception {
		// Creates the instance of CryptoManagement
		cryptoManagement = CryptoManagementFactory.create(Constants.IEHR_CA_URL, keyStoreName);
		// retrieves the health org private key using the alias
        privateKey = cryptoManagement.getPrivateKey(certAlias);
        if (privateKey == null)
        	throw new IllegalStateException("Error: Provided Keystore " + keyStoreName + " NOT FOUND in CLASSPATH!");
        
		// retrieves the health org certificate: MUST BE DONE ONCE
		certificateData = cryptoManagement.getUserCertificate(certAlias);
		// Instantiate the parser
		this.parser = parser;
		this.parser.setPrettyPrint(false);
	}
	
	/**
	 * Creates the detached JWSToken for a resource.
	 * 
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	public String createJWSToken(Resource resource) throws Exception {
		if (parser == null)
			throw new IllegalArgumentException("ResourceSigner has not been correctly initialized!");

		// signs the resource
		parser.setPrettyPrint(false);
		final String encodedResource = parser.encodeResourceToString(resource);
		final String signedResource = cryptoManagement.signPayload(encodedResource, privateKey);
		
		// Creates the JWS token
		return cryptoManagement.createDetachedJws(certificateData, signedResource);
	}

}
