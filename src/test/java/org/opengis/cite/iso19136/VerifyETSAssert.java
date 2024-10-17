package org.opengis.cite.iso19136;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import jakarta.ws.rs.core.MediaType;

public class VerifyETSAssert {

	private static final String WADL_NS = "http://wadl.dev.java.net/2009/02";

	private static DocumentBuilder docBuilder;

	private static SchemaFactory factory;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyETSAssert() {
	}

	@BeforeClass
	public static void setUpClass() throws ParserConfigurationException {
		factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	public void validateUsingSchemaHints_expect2Errors() throws SAXException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("2 schema validation error(s) detected");
		URL url = this.getClass().getResource("/Gamma.xml");
		Schema schema = factory.newSchema();
		Validator validator = schema.newValidator();
		ETSAssert.assertSchemaValid(validator, new StreamSource(url.toString()));
	}

	@Test
	public void assertXPathWithNamespaceBindings() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream("/capabilities-simple.xml"));
		Map<String, String> nsBindings = new HashMap<String, String>();
		nsBindings.put(WADL_NS, "ns1");
		String xpath = "//ns1:resources";
		ETSAssert.assertXPath(xpath, doc, nsBindings);
	}

	@Test
	public void assertXPath_expectError() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Unexpected result evaluating XPath expression");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream("/capabilities-simple.xml"));
		// using built-in namespace binding
		String xpath = "//ows:OperationsMetadata/ows:Constraint[@name='XMLEncoding']/ows:DefaultValue = 'TRUE'";
		ETSAssert.assertXPath(xpath, doc, null);
	}

	@Test
	public void assertTextResourceIsResolvable() {
		URL url = getClass().getResource("/Jabberwocky.txt");
		ETSAssert.assertURLIsResolvable(url, MediaType.TEXT_PLAIN_TYPE);
	}

	@Test
	public void assertTextResourceIsResolvableAsXML() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Response entity has unexpected media type");
		URL url = getClass().getResource("/Jabberwocky.txt");
		ETSAssert.assertURLIsResolvable(url, MediaType.APPLICATION_XML_TYPE);
	}

	@Test
	public void assertURLIsResolvable_cannotConnect() throws MalformedURLException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Failed to connect to URL");
		Random random = new Random();
		int portNum = random.nextInt(4000) + 60000;
		URL url = new URL("http", "localhost", portNum, "/nothing/here");
		ETSAssert.assertURLIsResolvable(url, null);
	}

	@Test
	@Ignore("Passes, but avoid establishing network connection")
	public void assertXMLResourceIsResolvable() throws MalformedURLException {
		URL url = new URL("http://www.w3schools.com/xml/note.xml");
		ETSAssert.assertURLIsResolvable(url, MediaType.TEXT_XML_TYPE);
	}

}
