package org.opengis.cite.iso19136.data.spatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.xerces.xs.XSElementDeclaration;
import org.geotoolkit.gml.xml.v321.LineStringType;
import org.geotoolkit.xml.MarshallerPool;
import org.opengis.cite.geomatics.gml.GmlUtils;
import org.opengis.cite.iso19136.GML32;
import org.opengis.cite.iso19136.data.DataFixture;
import org.opengis.cite.iso19136.util.TestSuiteLogger;
import org.opengis.cite.iso19136.util.XMLSchemaModelUtils;
import org.opengis.cite.iso19136.util.XMLUtils;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Validates the content of a gml:LineString element (or any element in its
 * substitution group). In GML a LineString is a regarded as a special curve
 * that consists of a single (implicit) segment with linear interpolation; it
 * must have two or more coordinate tuples. Note that in ISO 19107 a
 * GM_LineString is treated as a curve segment, not as a geometry type.
 * 
 * <p style="margin-bottom: 0.5em">
 * <strong>Sources</strong>
 * </p>
 * <ul>
 * <li>ISO 19136:2007, cl. 10.4.4: LineStringType, LineString</li>
 * <li>ISO 19107:2003, cl. 6.4.10: GM_LineString</li>
 * </ul>
 */
public class LineStringTests extends DataFixture {

	NodeList lineNodes;
	List<QName> lineElems = new ArrayList<QName>();

	/**
	 * A configuration method ({@code BeforeClass}) that looks for
	 * gml:LineString elements in the GML document under test. If none are found
	 * all test methods defined in the class will be skipped.
	 */
	@BeforeClass(alwaysRun = true)
	public void findLineStrings() {
		Source data = new StreamSource(this.dataFile);
		this.lineElems.add(new QName(GML32.NS_NAME, GML32.LINE_STRING));
		if (null != this.model) {
			XSElementDeclaration gmlCurve = this.model.getElementDeclaration(
					GML32.LINE_STRING, GML32.NS_NAME);
			List<XSElementDeclaration> lineDecls = XMLSchemaModelUtils
					.getElementsByAffiliation(this.model, gmlCurve);
			for (XSElementDeclaration decl : lineDecls) {
				this.lineElems.add(new QName(decl.getNamespace(), decl
						.getName()));
			}
		}
		Map<String, String> namespaceBindings = new HashMap<String, String>();
		String xpath = generateXPathExpression(this.lineElems,
				namespaceBindings);
		try {
			this.lineNodes = (NodeList) XMLUtils.evaluateXPath(data, xpath,
					namespaceBindings, XPathConstants.NODESET);
		} catch (XPathExpressionException xpe) { // won't happen
			throw new RuntimeException(xpe);
		}
		if (this.lineNodes.getLength() == 0) {
			throw new SkipException("No gml:Curve elements were found.");
		}
	}

	/**
	 * [{@code Test}] Verifies that a gml:LineString element has a valid CRS
	 * reference.
	 * 
	 * <p style="margin-bottom: 0.5em">
	 * <strong>Sources</strong>
	 * </p>
	 * <ul>
	 * <li>ISO 19136, cl. 9.10, 10.1.3.2</li>
	 * <li>ISO 19107, cl. 6.2.2.17 (Coordinate Reference System association)</li>
	 * </ul>
	 */
	@Test(description = "See ISO 19136: 9.10, 10.1.3.2; ISO 19107: 6.2.2.17")
	public void lineHasValidCRS() {
		for (int i = 0; i < this.lineNodes.getLength(); i++) {
			Element geom = (Element) this.lineNodes.item(i);
			GeometryAssert.assertValidCRS(geom);
		}
	}

	/**
	 * [{@code Test}] Verifies that a gml:LineString element contains at least
	 * two coordinate tuples and that it lies within the valid area of the CRS.
	 * 
	 * <p style="margin-bottom: 0.5em">
	 * <strong>Sources</strong>
	 * </p>
	 * <ul>
	 * <li>ISO 19136, 10.4.4: LineStringType, LineString</li>
	 * </ul>
	 */
	@Test(description = "See ISO 19136: 10.4.4")
	public void validLineString() {
		Unmarshaller gmlUnmarshaller;
		try {
			MarshallerPool pool = new MarshallerPool(
					"org.geotoolkit.gml.xml.v321");
			gmlUnmarshaller = pool.acquireUnmarshaller();
		} catch (JAXBException jxe) {
			throw new RuntimeException(jxe);
		}
		for (int i = 0; i < this.lineNodes.getLength(); i++) {
			Element lineElem = (Element) this.lineNodes.item(i);
			GmlUtils.findCRSReference(lineElem);
			GeometryAssert.assertAllCurveSegmentsHaveRequiredLength(lineElem);
			LineStringType line;
			try {
				JAXBElement<LineStringType> result = gmlUnmarshaller.unmarshal(
						lineElem, LineStringType.class);
				line = result.getValue();
			} catch (JAXBException e) {
				TestSuiteLogger.log(Level.WARNING,
						"Failed to unmarshal LineString geometry.", e);
				continue;
			}
			GeometryAssert.assertGeometryCoveredByValidArea(line);
		}
	}
}
