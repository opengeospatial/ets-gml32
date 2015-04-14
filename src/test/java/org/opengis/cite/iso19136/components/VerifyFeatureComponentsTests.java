package org.opengis.cite.iso19136.components;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.validation.Schema;

import org.apache.xerces.xs.XSModel;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.validation.XSModelBuilder;
import org.opengis.cite.validation.XmlSchemaCompiler;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the FeatureComponentTests class.
 */
public class VerifyFeatureComponentsTests {

	private static final String TARGET_NS = "http://example.org/ns1";
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyFeatureComponentsTests() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		testContext = mock(ITestContext.class);
		suite = mock(ISuite.class);
		when(testContext.getSuite()).thenReturn(suite);
	}

	@Test
	public void invalidFeaturePropertyAllowsMD_Metadata()
			throws URISyntaxException, SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("MD_Metadata cannot substitute");
		URL url = this.getClass().getResource("/xsd/simple2.xsd");
		XSModel model = createXSModel(url, URI.create(TARGET_NS));
		FeatureComponentTests iut = new FeatureComponentTests();
		iut.setSchemaModel(model);
		iut.verifyFeatureMemberProperties();
	}

	@Test
	public void invalidFleetMemberTypeAllowsEnvelope()
			throws URISyntaxException, SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Envelope cannot substitute");
		URL url = this.getClass().getResource("/xsd/autos.xsd");
		XSModel model = createXSModel(url,
				URI.create("http://www.deegree.org/app"));
		FeatureComponentTests iut = new FeatureComponentTests();
		iut.setSchemaModel(model);
		iut.verifyFeatureMemberProperties();
	}

	XSModel createXSModel(URL schemaUrl, URI targetNamespace)
			throws URISyntaxException, SAXException, IOException {
		Set<URI> uriSet = new HashSet<URI>();
		uriSet.add(schemaUrl.toURI());
		URL entityCatalog = getClass().getResource(
				"/org/opengis/cite/iso19136/schema-catalog.xml");
		XmlSchemaCompiler xsdCompiler = new XmlSchemaCompiler(entityCatalog);
		Schema xsd = xsdCompiler.compileXmlSchema(uriSet.toArray(new URI[uriSet
				.size()]));
		XSModel model = XSModelBuilder.buildXMLSchemaModel(xsd,
				targetNamespace.toString());
		return model;
	}
}
