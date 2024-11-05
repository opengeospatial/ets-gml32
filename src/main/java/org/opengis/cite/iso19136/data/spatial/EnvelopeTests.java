package org.opengis.cite.iso19136.data.spatial;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.referencing.CRS;
import org.apache.sis.xml.MarshallerPool;
import org.geotoolkit.gml.xml.v321.EnvelopeType;
import org.geotoolkit.gml.xml.GMLMarshallerPool;

import org.opengis.cite.geomatics.GeodesyUtils;
import org.opengis.cite.iso19136.GML32;
import org.opengis.cite.iso19136.data.DataFixture;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Validates the content of a gml:Envelope element, which implements the GM_Envelope class
 * from ISO 19107.
 *
 * <p>
 * The "lowerCorner" represents a coordinate position consisting of all the minimal
 * ordinates for each dimension for all points within the envelope. The "upperCorner" is a
 * coordinate position consisting of all the maximal ordinates for each dimension for all
 * points within the envelope.
 * </p>
 *
 * <p style="margin-bottom: 0.5em">
 * <strong>Sources</strong>
 * </p>
 * <ul>
 * <li>ISO 19136, cl. 10.1.4.6: EnvelopeType, Envelope</li>
 * <li>ISO 19136, cl. 9.10: Spatial reference system used in a feature or feature
 * collection</li>
 * <li>ISO 19107, cl. 6.4.3: GM_Envelope</li>
 * </ul>
 */
public class EnvelopeTests extends DataFixture {

	private static final QName GML_ENV = new QName(GML32.NS_NAME, "Envelope");

	List<Envelope> envelopes;

	/**
	 * A configuration method ({@code BeforeClass}) that finds gml:Envelope elements in
	 * the GML document under test. If none are found all test methods defined in the
	 * class will be skipped.
	 */
	@BeforeClass()
	public void findEnvelopes() {
		this.envelopes = new ArrayList<Envelope>();
		Unmarshaller unmarshaller;
		try {
			MarshallerPool pool = GMLMarshallerPool.getInstance();
			unmarshaller = pool.acquireUnmarshaller();
		}
		catch (JAXBException jxb) {
			throw new RuntimeException(jxb);
		}
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		try (FileInputStream gmlData = new FileInputStream(this.dataFile)) {
			XMLStreamReader reader = factory.createXMLStreamReader(gmlData, "UTF-8");
			while (reader.hasNext()) {
				int eventType = reader.next();
				if (eventType == XMLStreamConstants.START_ELEMENT && reader.getName().equals(GML_ENV)) {
					@SuppressWarnings("unchecked")
					JAXBElement<EnvelopeType> result = (JAXBElement<EnvelopeType>) unmarshaller.unmarshal(reader);
					EnvelopeType env = result.getValue();
					if (null != env.getSrsName() && env.getSrsName().startsWith("http")) {
						// Geotk 3.x does not support 'http' CRS naming scheme
						String crsRef = GeodesyUtils.getAbbreviatedCRSIdentifier(env.getSrsName());
						GeneralEnvelope genEnv = new GeneralEnvelope(CRS.forCode(crsRef));
						double[] lowerPos = env.getLowerCorner().getCoordinate();
						double[] upperPos = env.getUpperCorner().getCoordinate();
						double[] coords = new double[lowerPos.length + upperPos.length];
						System.arraycopy(lowerPos, 0, coords, 0, lowerPos.length);
						System.arraycopy(upperPos, 0, coords, lowerPos.length, upperPos.length);
						genEnv.setEnvelope(coords);
						this.envelopes.add(genEnv);
					}
					else {
						this.envelopes.add(env);
					}
				}
			}
			reader.close();
		}
		catch (Exception x) {
			// Remove non-ASCII chars from (Geotk) exception message
			String errMsg = x.getMessage().replaceAll("[^\\x00-\\x7F]", "");
			throw new RuntimeException(errMsg);
		}
		if (this.envelopes.isEmpty())
			throw new SkipException("No gml:Envelope elements found.");
	}

	/**
	 * [{@code Test}] An envelope must refer to a coordinate reference system using the
	 * {@code srsName} attribute. The alternative is to specify the CRS separately for
	 * each corner position but this is very uncommon.
	 *
	 * <p>
	 * The value of the srsName attribute must be an absolute URI, but it need not be
	 * dereferenceable for well-known CRS identifiers. The 'urn' or 'http' URI schemes may
	 * be used to identify a CRS as described in OGC 09-048r3, <em>Name type specification
	 * -- definitions -- part 1 -- basic name</em>. For example, the following URIs
	 * identify the WGS 84 (geographic 2D) coordinate reference system:
	 * </p>
	 * <ul>
	 * <li>urn:ogc:def:crs:EPSG::4326</li>
	 * <li>http://www.opengis.net/def/crs/EPSG/0/4326</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Note:</strong> Since gml:Envelope is not a geometry object, the CRS
	 * reference cannot be inherited from some larger spatial context according to ISO
	 * 19136, cl. 9.10.
	 * </p>
	 *
	 * @see <a href="http://portal.opengeospatial.org/files/?artifact_id=37802" target=
	 * "_blank">OGC 09-048r3</a>
	 */
	@Test(description = "See ISO 19136: 10.1.3.2")
	public void envelopeHasValidCRS() {
		for (int i = 0; i < this.envelopes.size(); i++) {
			Envelope env = this.envelopes.get(i);
			CoordinateReferenceSystem crs = env.getCoordinateReferenceSystem();
			String srsName = "unknown";
			if (EnvelopeType.class.isInstance(env)) {
				EnvelopeType gmlEnv = EnvelopeType.class.cast(env);
				srsName = gmlEnv.getSrsName();
			}
			Assert.assertNotNull(crs,
					String.format("//gml:Envelope[%d] has unknown CRS (srsName: %s)", i + 1, srsName));
		}
	}

	/**
	 * [{@code Test}] The coordinates of the lower corner must be less than the
	 * coordinates of the upper corner, where the coordinate tuples are compared item by
	 * item. Furthermore, the dimension of the corner positions must be identical and
	 * equal to that of the CRS.
	 *
	 * @see "ISO 19107: cl. 6.4.3.2, 6.4.3.3"
	 */
	@Test(description = "See ISO 19107: 6.4.3.2, 6.4.3.3")
	public void checkEnvelopePositions() {
		for (int i = 0; i < this.envelopes.size(); i++) {
			Envelope env = this.envelopes.get(i);
			DirectPosition lowerCorner = env.getLowerCorner();
			Assert.assertNotNull(lowerCorner, String.format("//gml:Envelope[%d] has no lowerCorner.", i + 1));
			DirectPosition upperCorner = env.getUpperCorner();
			Assert.assertEquals(lowerCorner.getDimension(), upperCorner.getDimension(),
					String.format("//gml:Envelope[%d]: dimension of corner positions must be equal.", i + 1));
			if (null != env.getCoordinateReferenceSystem()) {
				Assert.assertEquals(lowerCorner.getDimension(),
						env.getCoordinateReferenceSystem().getCoordinateSystem().getDimension(), String.format(
								"In //gml:Envelope[%d], dimension of lowerCorner does not match that of CRS.", i + 1));
			}
			for (int j = 0; j < lowerCorner.getDimension(); j++) {
				Assert.assertTrue(lowerCorner.getOrdinate(j) < upperCorner.getOrdinate(j),
						String.format(
								"//gml:Envelope[%d]: expected lowerCorner[%2$d] < upperCorner[%2$d] (%3$f < %4$f).",
								i + 1, j, lowerCorner.getOrdinate(j), upperCorner.getOrdinate(j)));
			}
		}
	}

}
