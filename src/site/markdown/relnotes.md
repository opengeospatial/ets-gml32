# Release Notes

## 1.31 (2023-05-17)

* [#75](https://github.com/opengeospatial/ets-gml32/issues/75): XML schema compilation fails when test suite is called from other ETS
* [#72](https://github.com/opengeospatial/ets-gml32/issues/72): Update TEAM Engine dependency to v5.6.1
* [#69](https://github.com/opengeospatial/ets-gml32/issues/69): Implement support for GML 3.2.2
* [#65](https://github.com/opengeospatial/ets-gml32/issues/65): validateMembersOfGmlObjectCollection test fails
* [#68](https://github.com/opengeospatial/ets-gml32/pull/68): Add credentials to SoapUI project

## 1.30 (2022-02-25)

* [#61](https://github.com/opengeospatial/ets-gml32/issues/61): Add template to get an XML/JSON response via rest endpoint
* [#67](https://github.com/opengeospatial/ets-gml32/pull/67): Bump xercesImpl from 2.12.0 to 2.12.2
* [#64](https://github.com/opengeospatial/ets-gml32/pull/64): Set Docker TEAM Engine version to 5.4.1
* [#63](https://github.com/opengeospatial/ets-gml32/pull/63): Added header for soapui test.

## 1.29 (2020-10-20)

* [#26](https://github.com/opengeospatial/ets-gml32/issues/26): Test method validSurfaceOrientation: Ring has fewer than 3 points
* [#56](https://github.com/opengeospatial/ets-gml32/issues/56): Update dependency of geomatics-geotk to version 1.15
* [#60](https://github.com/opengeospatial/ets-gml32/pull/60): Bump xercesImpl from 2.11.0 to 2.12.0
* [#57](https://github.com/opengeospatial/ets-gml32/issues/57): Cleanup dependencies

## 1.28 (2020-06-30)

* [#46](https://github.com/opengeospatial/ets-gml32/issues/46): application schema limitation
* [#45](https://github.com/opengeospatial/ets-gml32/issues/45): 2.5D points
* [#32](https://github.com/opengeospatial/ets-gml32/issues/32): ArrayIndexOutOfBoundsException thrown for MultiCurve with srsDimension=3

## 1.27 (2019-05-29)

* [#43](https://github.com/opengeospatial/ets-gml32/issues/43): Update tests to use TEAM Engine 5.4 enhancement
* [#40](https://github.com/opengeospatial/ets-gml32/issues/40): Enable Docker support and configure integration tests enhancement
* [#28](https://github.com/opengeospatial/ets-gml32/issues/28): feature collection of "gml:AbstractFeature"s does not validate bug
* [#17](https://github.com/opengeospatial/ets-gml32/issues/17): Colors in detailed test report are confusing or not correct enhancement question
* [#24](https://github.com/opengeospatial/ets-gml32/issues/24): Move release notes from changelog to relnotes enhancement
* [#36](https://github.com/opengeospatial/ets-gml32/issues/36): Introduce Dockerfile and Maven Docker plugin enhancement
* [#31](https://github.com/opengeospatial/ets-gml32/issues/31): Create SoapUI tests and integrate them into Maven and Jenkinsfile enhancement

## 1.26 (2018-05-16)

This maintenance release includes the following changes:

* Fix [#27](https://github.com/opengeospatial/ets-gml32/issues/27): Add conformance class configuration into the gml32 test

## 1.25 (2016-11-17)

This maintenance release includes the following changes:

*   Fix [#20](https://github.com/opengeospatial/ets-gml32/issues/20): Test method validSurfaceBoundary reports exterior boundary is not simple
*   Present Schematron validation results as plain text
*   For members of a geometry aggregate, look for srsName reference on ancestor geometry
*   Update dependency: geomatics-geotk-1.14

## 1.24 (2016-10-04)

This release includes the following updates:

*   Fix [issue #21](https://github.com/opengeospatial/ets-gml32/issues/21): Check child gml:posList, gml:pos elements for CRS reference
*   Updated dependencies: ets-common-8, geomatics-geotk-1.13

## 1.23 (2016-02-04)

This maintenance release includes the following fixes:

*   Fix [issue #20](https://github.com/opengeospatial/ets-gml32/issues/20): Check that surface geometry lies within valid area of CRS
*   Fix [issue #19](https://github.com/opengeospatial/ets-gml32/issues/19): LineString in which srsName value is 'http' URI throws NullPointerException
*   Fix [issue #18](https://github.com/opengeospatial/ets-gml32/issues/18): EPSG schema not created in Derby database
*   Use parent POM: ets-common-6

## 1.22 (2015-10-30)

This maintenance release includes the following fixes:

*   Update geomatics-geotk dependency to fix [issue #16](https://github.com/opengeospatial/ets-gml32/issues/16): validSurfaceBoundary test method reports 'Polygon not closed'
*   Fix [issue #13](https://github.com/opengeospatial/ets-gml32/issues/13): envelopeHasValidCRS fails if srsName is URL
*   Fix [issue #8](https://github.com/opengeospatial/ets-gml32/issues/8): Unit test failures on OS X
*   Fix Javadoc errors when building with JDK 8 (doclint).

## 1.21 (2015-07-08)

This maintenance release includes the following changes:

*   Fix [issues/11](https://github.com/opengeospatial/ets-gml32/issues/11): pointHasValidPosition - NullPointerException
*   In CTL output, report if all tests were skipped (because some precondition was not satisfied)

## 1.20 (2015-04-14)

This release includes the following fixes and enhancements:

*   Fix [issues/9](https://github.com/opengeospatial/ets-gml32/issues/9): Feature property type with choice compositor
*   Fix [issues/3](https://github.com/opengeospatial/ets-gml32/issues/3): LineString geometry is ignored
*   Set configfailurepolicy = "continue" (TestNG suite)
*   Build an all-in-one binary package (*-aio.jar)

## 1.19 (2015-03-05)

*   Update to new version scheme
*   [issue 8 - junit test failures](https://github.com/opengeospatial/ets-gml32/issues/8)

## 3.2.1-r18 (2014-10-16)

This maintenance release includes the following changes:

*   Resolve GitHub [issue 4](https://github.com/opengeospatial/ets-gml32/issues/4).
*   Update site documentation.
*   Add brief description annotations to test methods.

## 3.2.1-r17 (2014-08-20)

This final release is deemed suitable for conformance certification; it includes the following fixes:

*   Fix GitHub [issue 2](https://github.com/opengeospatial/ets-gml32/issues/2).
*   Fix GitHub [issue 1](https://github.com/opengeospatial/ets-gml32/issues/1).
*   Update dependencies (ets-commmon-2).

## 3.2.1-r16 (2014-06-05)

This release includes the following changes:

*   Add timeout (60 s) to compileXMLSchema test.
*   Remove assertion requiring metadata property value to be in application namespace.
*   Modify POM for GitHub.
*   Add site content.
*   Change license to Apache License, Version 2.0.

## 3.2.1-r15 (2014-04-15)

This maintenance release includes the following updates:

*   Add missing assertion regarding metadata property value (must be declared in application namespace).
*   Add support for dereferencing shorthand pointers in GML properties (@xlink:href attributes).
*   Update documentation.
*   Update dependencies (schema-utils, geomatics-geotk).

## 3.2.1-r14 (2014-03-04)

This release includes the following changes:

*   Modify unit tests to run without a network connection.

## 3.2.1-r13 (2014-01-30)

This release includes the following enhancements:

*   Added tests for gml:Polygon and gml:Surface geometries containing the following types of surface patch: PolygonPatch, Triangle, Rectangle (in org.opengis.cite.iso19136.data.spatial.SurfaceTests).
*   Updated dependency: geomatics-geotk-1.5.

## 3.2.1-r12 (2013-12-04)

This release includes the following enhancements:

*   Added tests for gml:Curve geometries containing the following types of curve segment: GeodesicString/Geodesic, ArcString/Arc/Circle, ArcByCenterPoint/CircleByCenterPoint.
*   Added tests for gml:CompositeCurve, gml:OrientableCurve.
*   Updated dependency: geomatics-geotk-1.4.

## 3.2.1-r11 (2013-11-14)

This release includes the following changes:

*   Updated dependency: geomatics-geotk.

## 3.2.1-r10 (2013-11-01)

This release includes the following enhancements:

*   Added CurveTests to validate essential aspects of gml:Curve elements (containing LineStringSegment only).
*   Added PointTests to validate gml:Point elements (including geometry elements in its substitution group).
*   Added EnvelopeTests to validate gml:Envelope elements.
*   Allow posting of GML document (form interface).

## 3.2.1-r9 (2013-09-19)

This is a maintenance release; it includes the following changes:

*   Write reporter output to test session directory when invoked via CTL script.

## 3.2.1-r8 (2013-08-23)

This is a maintenance release; it includes the following fixes:

*   CITE-839: validateMembersOfObjectCollection().
*   Updated documentation.

## 3.2.1-r7 (2013-07-24)

This is a maintenance release; it includes the following fixes:

*   CITE-840 (findGeometryComponents misses geometry properties).

## 3.2.1-r6 (2013-07-19)

This is a maintenance release; it includes the following fixes:

*   CITE-845 (Anonymous feature type definition not detected).

## 3.2.1-r5 (2013-07-15)

This is a maintenance release; it includes the following changes:

*   CITE-826: Disable tests for lexical conventions
*   CITE-825: Some schemas referenced in @xsi:schemaLocation are ignored
*   Use XLink 1.1 schema (internal catalog resolver)
*   Updated dependencies: schema-utils-1.2

## 3.2.1-r4 (2013-07-04)

This maintenance release includes the following changes:

*   Updated dependencies: teamengine-spi-4.0, jersey-client-1.17.1
*   Don't clear reporter output in SuiteFixtureListener#onFinish()
