## GML 3.2 (ISO 19136:2007) Conformance Test Suite

### Scope

This test suite checks GML 3.2 application schemas and data sets for conformance to 
ISO 19136:2007, _Geographic information -- Geography Markup Language_ (also published 
as [OGC 07-036](http://portal.opengeospatial.org/files/?artifact_id=20509)).
The essential purpose of the test suite is to validate a GML application schema 
or an instance document; both kinds of resources are referenced by an 'http' URI. 
An application schema can be accessed from a variety of sources, such as: a plain 
web server, a Web Feature Service (WFS) by means of a DescribeFeatureType request, 
or a catalogue service.

The GML specification defines 10 conformance classes that pertain to application 
schemas. Five of these are currently covered by the test suite:

* A.1.1: All GML application schemas
* A.1.4: GML application schemas defining features and feature collections 
* A.1.5: GML application schemas defining spatial geometries
* A.1.6: GML application schemas defining spatial topologies
* A.1.7: GML application schemas defining time

While an instance document is checked for schema validity, the suite also includes 
tests that validate fundamental GML geometry elements against various constraints 
that cannot be expressed in an XML Schema grammar (e.g. surface boundary orientation); 
these tests also apply to any application-defined geometries that can substitute 
for the base GML geometry.

Visit the [project documentation website](http://opengeospatial.github.io/ets-gml32/) 
for more information about test suite coverage, including the API documentation.


### Eligible test subjects

Any spatial data set or application schema that purports to be based on GML 3.2 
can be subject to a conformity assessment. All WFS 2.0 implementation must produce 
and consume GML (v3.2) feature representations. The OGC maintains a list of 
[reference implementations](https://github.com/opengeospatial/cite/wiki/Reference-Implementations).
Several user communities have developed non-trivial application schemas based 
on GML 3.2:

* [GeoSciML](http://www.geosciml.org/) (v3.2)
* [AIXM](http://www.aixm.aero/) (v5.1)
* [INSPIRE](http://inspire.ec.europa.eu/index.cfm/pageid/2/list/xml-schemas) (v4.0)


### How to contribute

If you would like to get involved, you can:

* [Report an issue](https://github.com/opengeospatial/ets-gml32/issues) such as a defect or 
an enhancement request
* Help to resolve an [open issue](https://github.com/opengeospatial/ets-gml32/issues?q=is%3Aopen)
* Fix a bug: Fork the repository, apply the fix, and create a pull request
* Add new tests: Fork the repository, implement (and verify) the tests on a new topic branch, 
and create a pull request
