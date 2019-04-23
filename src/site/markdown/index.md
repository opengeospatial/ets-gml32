# GML 3.2 (ISO 19136:2007) Conformance Test Suite 

## Scope 

This executable test suite (ETS) verifies the conformance of GML data and application schemas with respect to ISO 19136:2007 (GML 3.2.1). It can be used to check a GML **application schema** or an **instance document**; both types of resources are referenced by URI. The application schema could be accessed from a variety of sources, such as a WFS (DescribeFeatureType request using the GET method) or a catalogue service. As shown in Figure 1, a conforming GML data instance must refer to the relevant GML application schema, which in turn imports the complete GML schema.

![GML schemas](./images/gml-schemas.png)

**Figure 1** - GML schemas

A Schematron schema (ISO 19757-3) may be used to define supplementary data constraints that lay beyond the reach of an XML Schema grammar. A GML instance document may include a schema reference using the `xml-model` processing instruction (PI) as described in [ISO 19757-11](http://standards.iso.org/ittf/PubliclyAvailableStandards/c054793_ISO_IEC_19757-11_2011.zip). The PI must appear before the document element as shown in the following listing.

```
<?xml version="1.0" encoding="UTF-8"?>
<?xml-model href="http://example.org/data-constraints.sch" 
            schematypens="http://purl.oclc.org/dsdl/schematron" 
            phase="#ALL"?>
<CityModel xmlns="http://www.opengis.net/citygml/2.0">
  <!-- content omitted --> 
</CityModel>
```

The `uom` attribute indicates the unit of measure for some measured quantity. If the value is not an absolute URI, it is expected to be a unit symbol (possibly with a prefix symbol) appearing in the _Unified Code for Units of Measure_ ([UCUM](http://unitsofmeasure.org/ucum.html)). 

## Test coverage 

Table 1 in the GML specification defines the conformance classes related to GML application schemas. In this test suite all mandatory conformance requirements are checked, and every GML instance document is thoroughly validated against all referenced application schemas. However, the classes dealing with less commonly used types of objects are not implemented by the current test suite. Table 1 below indicates the implementation status of each conformance class.

| Name | ATS reference | Implemented in test suite |
| ---- | ------------ | -------------------------- |
|All GML application schemas | A.1.1 | Yes|
| GML application schemas defining features and feature collections | A.1.4 | Yes |
| GML application schemas defining spatial geometries | A.1.5 | Yes |
| GML application schemas defining spatial topologies | A.1.6 | Yes |
| GML application schemas defining time | A.1.7 | Yes |
| GML application schemas defining coordinate reference systems | A.1.8 | No |
| GML application schemas defining coverages | A.1.9 | No | 
| GML application schemas defining observations | A.1.10 | No |
| GML application schemas defining dictionaries and definitions | A.1.11 | No |
| GML application schemas defining values | A.1.12 | No |

No specific conformance classes are defined for GML instance documents. However, clause A.3 (_Abstract test suite for GML documents_) includes a set of abstract test cases for validating GML documents. Of these, A.3.1-A.3.4 are implemented by this test suite. Clause A.3.5 is a very broad, catch-all constraint that is partly implemented: "Verify that the GML document complies with all other constraints specified by this International Standard." 

Among the constraints implied by A.3.5 are those concerned with the validity of geometry representations. The suite includes tests that validate the geometry elements listed below; these tests also apply to any application-defined geometries that can substitute for the standard GML elements.

* gml:Point 
* gml:Curve having the following curve segments: gml:ArcByCenterPoint, gml:CircleByCenterPoint, gml:Arc, gml;Circle, gml:GeodesicString, gml:Geodesic, gml: LineStringSegment 
* gml:OrientableCurve 
* gml:CompositeCurve 
* gml:Polygon 
* gml:Surface having the following surface patches: gml:PolygonPatch, gml:Rectangle, gmlTriangle 

## Test suite structure 

The test suite definition file (testng.xml) is located in the root package, `org.opengis.cite.iso19136`. A conformance class corresponds to a <test />element; each test element includes a set of test classes that contain the actual test methods. The general structure of the test suite is shown in Table 2.

| Conformance class | Test Classes |
| ----------------- | ------------ |
| All GML application schemas | org.opengis.cite.iso19136.general.XMLSchemaTests<br />org.opengis.cite.iso19136.general.GeneralSchemaTests<br />org.opengis.cite.iso19136.general.ModelAndSyntaxTests<br />org.opengis.cite.iso19136.general.ComplexPropertyTests | 
| GML application schemas defining features and feature collections | org.opengis.cite.iso19136.components.FeatureComponentTests |
| GML application schemas defining spatial geometries| org.opengis.cite.iso19136.components.GeometryComponentTests |
| GML application schemas defining time | org.opengis.cite.iso19136.components.TemporalComponentTests |
| GML application schemas defining spatial topologies | org.opengis.cite.iso19136.components.TopologyComponentTests |
| GML Documents	| org.opengis.cite.iso19136.data.XMLSchemaValidationTests<br />org.opengis.cite.iso19136.data.SchematronTests<br />org.opengis.cite.iso19136.data.PropertyValueTests<br />org.opengis.cite.iso19136.data.spatial.EnvelopeTests<br />org.opengis.cite.iso19136.data.spatial.PointTests<br />org.opengis.cite.iso19136.data.spatial.SurfaceTests<br />org.opengis.cite.iso19136.data.spatial.CurveTests<br />org.opengis.cite.iso19136.data.spatial.CompositeCurveTests |

The Javadoc documentation provides more detailed information about the test methods that constitute the suite. 

## Test requirements 

The documents listed below stipulate requirements that must be satisfied by a conforming application schema.

* [ISO-19136 - Geographic information -- Geography Markup Language (GML)](http://www.iso.org/iso/iso_catalogue/catalogue_tc/catalogue_detail.htm?csnumber=32554). Also published as [OGC 07-036](http://portal.opengeospatial.org/files/?artifact_id=20509).
* [XML Schema Part 1: Structures](http://www.w3.org/TR/xmlschema-1/), Second Edition

A conforming GML application schema must satisfy all mandatory constraints _and_ define at least one type of GML object in accord with clauses 21.3 through 21.11. The applicable type-specific test cases are described in clauses A.1.4 through A.1.12. 

## How to run the tests 

The test suite may be run in any of the following environments: 

* Integrated development environment (IDE): The main Java class is `TestNGController`. 
* REST API: Submit a request that includes the necessary arguments to the test run controller (/rest/suites/${ets-code}/${project.version}/run). 
* TEAM-Engine: Run the CTL script located in the `/src/main/ctl/` directory. 

The test run arguments are summarized in Table 3\. The _Obligation_ descriptor can have the following values: M (mandatory), O (optional), or C (conditional). A GML application schema may be validated by itself without a referring document.

**Table 3 - Test run arguments**

| Name | Value domain | Obligation | Description |
| ---- | ------------ | ---------- | ----------- |
| gml | URI | M | An absolute URI that refers to either a representation of a GML data instance or an application schema1.|
| sch | URI | O | A URI referring to a Schematron schema that defines supplementary data constraints2.| 


**Notes:**

 1.  Ampersand ('&') characters appearing within query parameter values must be percent-encoded as %26.
 1.  See ISO 19757-3:2006





