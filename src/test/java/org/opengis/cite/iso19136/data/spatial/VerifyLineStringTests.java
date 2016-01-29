package org.opengis.cite.iso19136.data.spatial;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.iso19136.BasicFixture;
import org.opengis.util.FactoryException;
import org.w3c.dom.DOMException;

/**
 * Verifies the behavior of the LineStringTests class.
 */
public class VerifyLineStringTests extends BasicFixture {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void findLines_expect2() throws URISyntaxException {
		URL url = this.getClass().getResource("/geom/MultiCurve-2.xml");
		File dataFile = new File(url.toURI());
		LineStringTests iut = new LineStringTests();
		iut.setDataFile(dataFile);
		iut.findLineStrings();
		assertEquals("Unexpected number of curves.", 2,
				iut.lineNodes.getLength());
	}

	@Test
	public void verifyLineHasValidCRS() throws URISyntaxException {
		URL url = this.getClass().getResource("/geom/MultiCurve-2.xml");
		File dataFile = new File(url.toURI());
		LineStringTests iut = new LineStringTests();
		iut.setDataFile(dataFile);
		iut.findLineStrings();
		iut.lineHasValidCRS();
	}

	@Test
	public void valid3DLineString() throws URISyntaxException, DOMException,
			FactoryException {
		URL url = this.getClass().getResource("/geom/LineString.xml");
		File dataFile = new File(url.toURI());
		LineStringTests iut = new LineStringTests();
		iut.setDataFile(dataFile);
		iut.findLineStrings();
		iut.validLineString();
	}

	@Test
	public void srsNameIsHttpId() throws URISyntaxException, DOMException,
			FactoryException {
		URL url = this.getClass().getResource(
				"/geom/LineString-srsName-http.xml");
		File dataFile = new File(url.toURI());
		LineStringTests iut = new LineStringTests();
		iut.setDataFile(dataFile);
		iut.findLineStrings();
		iut.validLineString();
	}

	@Test
	public void invalidLineStringCoords() throws URISyntaxException,
			DOMException, FactoryException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("not covered by valid area of CRS");
		URL url = this.getClass().getResource(
				"/geom/LineString-invalidCoords.xml");
		File dataFile = new File(url.toURI());
		LineStringTests iut = new LineStringTests();
		iut.setDataFile(dataFile);
		iut.findLineStrings();
		iut.validLineString();
	}
}
