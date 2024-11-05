package org.opengis.cite.iso19136.general;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.apache.xerces.xs.XSModel;
import org.junit.Test;
import org.opengis.cite.iso19136.BasicFixture;
import org.xml.sax.SAXException;

/**
 * Verifies methods in the ComplexPropertyTests class.
 */
public class VerifyComplexPropertyTests extends BasicFixture {

	private static final String TARGET_NS = "http://www.deegree.org/app";

	public VerifyComplexPropertyTests() {
	}

	@Test
	public void validateCurveMember() throws IOException, SAXException {
		URL url = this.getClass().getResource("/xsd/anon-types.xsd");
		XSModel model = createXSModel(url, URI.create(TARGET_NS));
		ComplexPropertyTests iut = new ComplexPropertyTests();
		iut.setSchemaModel(model);
		iut.validateMembersOfGmlObjectCollection();
	}

	@Test
	public void unspecifiedMetadataProperty() throws IOException, SAXException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Metadata property value is unspecified");
		URL url = this.getClass().getResource("/xsd/anon-types.xsd");
		XSModel model = createXSModel(url, URI.create(TARGET_NS));
		ComplexPropertyTests iut = new ComplexPropertyTests();
		iut.setSchemaModel(model);
		iut.validateMetadataProperties();
	}

}
