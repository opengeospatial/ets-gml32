package org.opengis.cite.iso19136;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.transform.stream.StreamSource;

import org.opengis.cite.iso19136.util.TestSuiteLogger;
import org.opengis.cite.iso19136.util.URIUtils;
import org.opengis.cite.iso19136.util.ValidationUtils;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * A listener that performs various tasks before and after a test suite is run,
 * usually concerned with maintaining a shared test suite fixture. Since this
 * listener is loaded using the ServiceLoader mechanism, its methods will be
 * called before those of other suite listeners listed in the test suite
 * definition and before any annotated configuration methods.
 * 
 * Attributes set on an ISuite instance are not inherited by constituent test
 * group contexts (ITestContext). However, they are still accessible from lower
 * contexts.
 * 
 * @see org.testng.ISuite ISuite interface
 */
public class SuiteFixtureListener implements ISuiteListener {

	@Override
	public void onStart(ISuite suite) {
		TestSuiteLogger.log(Level.CONFIG, String.format(
				"[SuiteFixtureListener] All parameters:\n%s", suite
						.getXmlSuite().getAllParameters()));
		processIUTParameter(suite);
		processGmlReference(suite);
		processSchematronSchema(suite);
		processVersionParameter(suite);
	}

	@Override
	public void onFinish(ISuite suite) {
		String reportDir = suite.getOutputDirectory();
		TestSuiteLogger.log(
				Level.CONFIG,
				String.format(
						"Test run directory: %s",
						reportDir.substring(0,
								reportDir.lastIndexOf(File.separatorChar))));
	}

	/**
	 * Processes the {@link org.opengis.cite.iso19136.TestRunArg#IUT} test suite
	 * parameter that refers to a POSTed message entity. Its value is a URI
	 * referring to either an application schema or a GML document. If the
	 * resource is an XML Schema the
	 * {@link org.opengis.cite.iso19136.TestRunArg#XSD} parameter is set;
	 * otherwise it is assumed to be a GML data resource.
	 * 
	 * @param suite
	 *            An ISuite object representing a TestNG test suite.
	 */
	void processIUTParameter(ISuite suite) {
		Map<String, String> params = suite.getXmlSuite().getParameters();
		String iutRef = params.get(TestRunArg.IUT.toString());
		if (null == iutRef || iutRef.isEmpty()) {
			return;
		}
		try {
			File iutFile = URIUtils.resolveURIAsFile(URI.create(iutRef));
			if (isXMLSchema(iutFile)) {
				params.put(TestRunArg.XSD.toString(), iutRef);
			} else {
				params.put(TestRunArg.GML.toString(), iutRef);
			}
		} catch (Exception x) {
			throw new RuntimeException(
					"Failed to read resource from " + iutRef, x);
		}
		params.remove(TestRunArg.IUT.toString());
	}

	/**
	 * Processes the {@link org.opengis.cite.iso19136.TestRunArg#VERSION} test suite
	 * parameter that refers to a POSTed message entity. Its value is a String
	 * representing the version of the GML to be tested.
	 * 
	 * @param suite
	 *            An ISuite object representing a TestNG test suite.
	 */
	void processVersionParameter(ISuite suite) {
		Map<String, String> params = suite.getXmlSuite().getParameters();
		String versionRef = params.get(TestRunArg.VERSION.toString());
		if (null == versionRef || versionRef.isEmpty()) {
			versionRef = "3.2.2";
		}
		suite.setAttribute(SuiteAttribute.VERSION.getName(), versionRef);
	}

	/**
	 * Extracts schema references from the GML resource identified by the
	 * supplied test run argument. If this is a GML instance document, the value
	 * of the standard xsi:schemaLocation attribute is used to locate the
	 * application schema(s). The schema references are added as the suite
	 * attribute {@link SuiteAttribute#SCHEMA_LOC_SET SCHEMA_LOC_SET} (of type
	 * Set&lt;URI&gt;).
	 * 
	 * @param suite
	 *            An ISuite object representing a TestNG test suite.
	 */
	void processGmlReference(ISuite suite) {
		Map<String, String> params = suite.getXmlSuite().getParameters();
		TestSuiteLogger.log(Level.CONFIG,
				String.format("Suite parameters:\n %s", params));
		Set<URI> schemaURIs = new HashSet<URI>();
		String xsdURI = params.get(TestRunArg.XSD.toString());
		if (null != xsdURI && !xsdURI.isEmpty()) {
			// was submitted as iut argument value via POST
			schemaURIs.add(URI.create(xsdURI));
			suite.setAttribute(SuiteAttribute.SCHEMA_LOC_SET.getName(),
					schemaURIs);
			return;
		}
		String gmlURI = params.get(TestRunArg.GML.toString());
		if (null == gmlURI || gmlURI.isEmpty()) {
			throw new IllegalArgumentException(
					"Missing GML resource (document or application schema).");
		}
		File gmlFile = null;
		try {
			gmlFile = URIUtils.resolveURIAsFile(URI.create(gmlURI));
			if (null == gmlFile || !gmlFile.exists()) {
				throw new IllegalArgumentException(
						"Failed to dereference URI: " + gmlURI);
			}
			if (isXMLSchema(gmlFile)) {
				params.put(TestRunArg.XSD.toString(), gmlURI);
				schemaURIs.add(URI.create(gmlURI));
			} else {
				schemaURIs.addAll(ValidationUtils.extractSchemaReferences(
						new StreamSource(gmlFile), gmlURI));
				suite.setAttribute(SuiteAttribute.GML.getName(), gmlFile);
				TestSuiteLogger.log(Level.FINE, "Wrote GML document to "
						+ gmlFile.getAbsolutePath());
			}
		} catch (IOException iox) {
			throw new RuntimeException("Failed to read resource obtained from "
					+ gmlURI, iox);
		} catch (XMLStreamException xse) {
			throw new RuntimeException(
					"Failed to find schema reference in source: "
							+ gmlFile.getAbsolutePath(), xse);
		}
		suite.setAttribute(SuiteAttribute.SCHEMA_LOC_SET.getName(), schemaURIs);
		TestSuiteLogger.log(Level.FINE,
				String.format("Schema references: %s", schemaURIs));
	}

	/**
	 * Adds a URI reference specifying the location of a Schematron schema.
	 * 
	 * @param suite
	 *            An ISuite object representing a TestNG test suite.
	 */
	void processSchematronSchema(ISuite suite) {
		Map<String, String> params = suite.getXmlSuite().getParameters();
		String schRef = params.get(TestRunArg.SCH.toString());
		if ((schRef != null) && !schRef.isEmpty()) {
			URI schURI = URI.create(params.get(TestRunArg.SCH.toString()));
			suite.setAttribute(SuiteAttribute.SCHEMATRON.getName(), schURI);
		}
	}

	/**
	 * Determines if the content of the given file represents an XML Schema. The
	 * document element must be {"http://www.w3.org/2001/XMLSchema"}schema.
	 * 
	 * @param file
	 *            A File object.
	 * @return {@code true} if the file contains an XML Schema; {@code false}
	 *         otherwise.
	 */
	boolean isXMLSchema(File file) {
		if (!file.exists() || (file.length() == 0)) {
			return false;
		}
		QName docElemName = QName.valueOf("");
		InputStream inStream = null;
		XMLEventReader reader = null;
		try {
			inStream = new FileInputStream(file);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			reader = factory.createXMLEventReader(inStream);
			StartElement docElem = reader.nextTag().asStartElement();
			docElemName = docElem.getName();
		} catch (Exception e1) {
			return false;
		} finally {
			try {
				reader.close();
				inStream.close();
			} catch (Exception e2) {
				TestSuiteLogger.log(Level.INFO, "Error closing resource.", e2);
			}
		}
		return docElemName.getNamespaceURI().equals(
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}

}
