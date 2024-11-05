package org.opengis.cite.iso19136;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.opengis.cite.iso19136.util.NamespaceBindings;
import org.opengis.cite.iso19136.util.TestSuiteLogger;
import org.opengis.cite.iso19136.util.XMLSchemaModelUtils;
import org.opengis.cite.iso19136.util.XMLUtils;
import org.opengis.cite.validation.SchematronValidator;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.ws.rs.core.MediaType;

/**
 * Provides a set of custom assertion methods.
 */
public class ETSAssert {

	private ETSAssert() {
	}

	/**
	 * Asserts that the qualified name of a DOM Node matches the expected value.
	 * @param node The Node to check.
	 * @param qName A QName object containing a namespace name (URI) and a local part.
	 */
	public static void assertQualifiedName(Node node, QName qName) {
		Assert.assertEquals(node.getLocalName(), qName.getLocalPart(), ErrorMessage.get(ErrorMessageKeys.LOCAL_NAME));
		Assert.assertEquals(node.getNamespaceURI(), qName.getNamespaceURI(),
				ErrorMessage.get(ErrorMessageKeys.NAMESPACE_NAME));
	}

	/**
	 * Asserts that an XPath 1.0 expression holds true for the given evaluation context.
	 * The following standard namespace bindings do not need to be explicitly declared:
	 *
	 * <ul>
	 * <li>ows: {@value org.opengis.cite.iso19136.Namespaces#OWS}</li>
	 * <li>xlink: {@value org.opengis.cite.iso19136.Namespaces#XLINK}</li>
	 * <li>gml: {@value org.opengis.cite.iso19136.GML32#NS_NAME}</li>
	 * </ul>
	 * @param expr A valid XPath 1.0 expression.
	 * @param context The context node.
	 * @param namespaceBindings A collection of namespace bindings for the XPath
	 * expression, where each entry maps a namespace URI (key) to a prefix (value). It may
	 * be {@code null}.
	 */
	public static void assertXPath(String expr, Node context, Map<String, String> namespaceBindings) {
		if (null == context) {
			throw new NullPointerException("Context node is null.");
		}
		NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
		bindings.addAllBindings(namespaceBindings);
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(bindings);
		Boolean result;
		try {
			result = (Boolean) xpath.evaluate(expr, context, XPathConstants.BOOLEAN);
		}
		catch (XPathExpressionException xpe) {
			String msg = ErrorMessage.format(ErrorMessageKeys.XPATH_ERROR, expr);
			TestSuiteLogger.log(Level.WARNING, msg, xpe);
			throw new AssertionError(msg);
		}
		Assert.assertTrue(result, ErrorMessage.format(ErrorMessageKeys.XPATH_RESULT, context.getNodeName(), expr));
	}

	/**
	 * Asserts that an XML resource is schema-valid.
	 * @param validator The Validator to use.
	 * @param source The XML Source to be validated.
	 */
	public static void assertSchemaValid(Validator validator, Source source) {
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		validator.setErrorHandler(errHandler);
		try {
			validator.validate(source);
		}
		catch (Exception e) {
			throw new AssertionError(ErrorMessage.format(ErrorMessageKeys.XML_ERROR, e.getMessage()));
		}
		Assert.assertFalse(errHandler.errorsDetected(), ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
				errHandler.getErrorCount(), errHandler.toString()));
	}

	/**
	 * Asserts that an XML resource satisfies all applicable constraints specified in a
	 * Schematron (ISO 19757-3) schema. The "xslt2" query language binding is supported.
	 * All patterns are checked.
	 * @param schemaRef A URL that denotes the location of a Schematron schema.
	 * @param xmlSource The XML Source to be validated.
	 */
	public static void assertSchematronValid(URL schemaRef, Source xmlSource) {
		SchematronValidator validator;
		try {
			validator = new SchematronValidator(new StreamSource(schemaRef.toString()), "#ALL");
		}
		catch (Exception e) {
			StringBuilder msg = new StringBuilder("Failed to process Schematron schema at ");
			msg.append(schemaRef).append('\n');
			msg.append(e.getMessage());
			throw new AssertionError(msg);
		}
		Result result = validator.validate(xmlSource);
		Assert.assertFalse(validator.ruleViolationsDetected(), ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
				validator.getRuleViolationCount(), XMLUtils.resultToString(result)));
	}

	/**
	 * Asserts that the given URL is resolvable; that is, it can be dereferenced to obtain
	 * a resource representation that corresponds to an expected media type.
	 * @param url The URL to be dereferenced.
	 * @param expectedMediaType The expected media type of the representation; if not
	 * specified any type of content is acceptable.
	 */
	public static void assertURLIsResolvable(URL url, MediaType expectedMediaType) {
		MediaType contentType;
		int contentLength = 0;
		// WARNING: Ignores HTTP redirects 3xx
		try {
			URLConnection urlConnection = url.openConnection();
			urlConnection.connect();
			contentLength = urlConnection.getContentLength();
			contentType = MediaType.valueOf(urlConnection.getContentType());
		}
		catch (IOException iox) {
			throw new AssertionError(String.format("Failed to connect to URL %s \n %s", url.toString(), iox));
		}
		Assert.assertTrue(contentLength > 0, ErrorMessage.get(ErrorMessageKeys.MISSING_ENTITY));
		if (null != expectedMediaType) {
			Assert.assertEquals(contentType, expectedMediaType,
					ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_MEDIA_TYPE));
		}
	}

	/**
	 * Asserts that the content model of the given type definition mimics a GML property
	 * type. The type is presumably not derived (by restriction) from some base GML
	 * property type (gml:AssociationRoleType, gml:ReferenceType, gml:InlinePropertyType).
	 * The following constraints apply to the content model:
	 *
	 * <ul>
	 * <li>if the content model is not empty, one of the following cases must be true:
	 * <ol>
	 * <li>it contains only one element declaration (the property value), which may occur
	 * more than once in an array property type;</li>
	 * <li>it contains a choice compositor that allows at most one element (an allowable
	 * substitution) to occur.</li>
	 * </ol>
	 * </li>
	 * <li>if it is empty (or possibly empty), it includes the attribute group
	 * gml:AssociationAttributeGroup.</li>
	 * <li>includes the attribute group gml:OwnershipAttributeGroup.</li>
	 * <li>does not contain a wildcard schema component.</li>
	 * <li>the property value must substitute for the designated head element (if
	 * specified).</li>
	 * </ul>
	 *
	 * <p style="margin-bottom: 0.5em">
	 * <strong>Sources</strong>
	 * </p>
	 * <ul>
	 * <li>ISO 19136:2007, cl. 7.2.3.3: abstractAssociationRole, AssociationRoleType</li>
	 * <li>ISO 19136:2007, cl. 7.2.3.7: abstractReference, ReferenceType</li>
	 * <li>ISO 19136:2007, cl. 7.2.3.8: abstractInlineProperty, InlinePropertyType</li>
	 * </ul>
	 * @param model An XSModel object representing an XML Schema resource.
	 * @param propertyDecl An XSElementDeclaration object representing a property element
	 * declaration.
	 * @param head The head of the substitution group to which the property value belongs
	 * (may be {@code null}).
	 */
	public static void assertValidPropertyType(XSModel model, XSElementDeclaration propertyDecl,
			XSElementDeclaration head) {
		assertValidPropertyType(model, propertyDecl, head, false);
	}

	/**
	 * Asserts that the content model of the given type definition mimics a GML property
	 * type. The type is presumably not derived (by restriction) from some base GML
	 * property type (gml:AssociationRoleType, gml:ReferenceType, gml:InlinePropertyType).
	 * The following constraints apply to the content model:
	 *
	 * <ul>
	 * <li>if the content model is not empty, one of the following cases must be true:
	 * <ol>
	 * <li>it contains only one element declaration (the property value), which may occur
	 * more than once in an array property type;</li>
	 * <li>it contains a choice compositor that allows at most one element (an allowable
	 * substitution) to occur.</li>
	 * </ol>
	 * </li>
	 * <li>if it is empty (or possibly empty), it includes the attribute group
	 * gml:AssociationAttributeGroup.</li>
	 * <li>includes the attribute group gml:OwnershipAttributeGroup.</li>
	 * <li>does not contain a wildcard schema component.</li>
	 * <li>the property value must substitute for the designated head element (if
	 * specified).</li>
	 * </ul>
	 *
	 * <p style="margin-bottom: 0.5em">
	 * <strong>Sources</strong>
	 * </p>
	 * <ul>
	 * <li>ISO 19136:2007, cl. 7.2.3.3: abstractAssociationRole, AssociationRoleType</li>
	 * <li>ISO 19136:2007, cl. 7.2.3.7: abstractReference, ReferenceType</li>
	 * <li>ISO 19136:2007, cl. 7.2.3.8: abstractInlineProperty, InlinePropertyType</li>
	 * </ul>
	 * @param model An XSModel object representing an XML Schema resource.
	 * @param propertyDecl An XSElementDeclaration object representing a property element
	 * declaration.
	 * @param head The head of the substitution group to which the property value belongs
	 * (may be {@code null}).
	 * @param includeHeadInSubstition <code>true</code> if the property value may belong
	 * to the head, <code>false</code> otherwise
	 */
	public static void assertValidPropertyType(XSModel model, XSElementDeclaration propertyDecl,
			XSElementDeclaration head, boolean includeHeadInSubstition) {
		XSComplexTypeDefinition propTypeDef = (XSComplexTypeDefinition) propertyDecl.getTypeDefinition();
		String localName = (propTypeDef.getAnonymous()) ? propertyDecl.getName() : propTypeDef.getName();
		boolean isEmpty = (propTypeDef.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_EMPTY);
		XSObjectList attrUses = propTypeDef.getAttributeUses();
		if (isEmpty) {
			Assert.assertNotNull(getAttributeUseByName(attrUses, new QName(Namespaces.XLINK, "href")), ErrorMessage
				.format(ErrorMessageKeys.ATTRIB_REQUIRED, "xlink:href", propTypeDef.getNamespace(), localName));
		}
		else {
			XSParticle topParticle = propTypeDef.getParticle();
			if (topParticle.getMinOccurs() == 0) {
				// may be empty, so a reference is required
				Assert.assertNotNull(getAttributeUseByName(attrUses, new QName(Namespaces.XLINK, "href")),
						"Property value has minOccurs = 0. " + ErrorMessage.format(ErrorMessageKeys.ATTRIB_REQUIRED,
								"xlink:href", propTypeDef.getNamespace(), localName));
			}
			XSTerm term = topParticle.getTerm();
			Assert.assertTrue(XSModelGroup.class.isInstance(term),
					ErrorMessage.format(ErrorMessageKeys.MODEL_GRP_EXPECTED, propTypeDef.getNamespace(), localName));
			XSModelGroup group = (XSModelGroup) term;
			Assert.assertEquals(group.getCompositor(), XSModelGroup.COMPOSITOR_SEQUENCE,
					ErrorMessage.format(ErrorMessageKeys.SEQ_EXPECTED, propTypeDef.getNamespace(), localName));
			boolean isArrayProperty = (topParticle.getMaxOccursUnbounded() || (topParticle.getMaxOccurs() > 1));
			if (isArrayProperty) {
				Assert.assertNull(getAttributeUseByName(attrUses, new QName(Namespaces.XLINK, "href")), ErrorMessage
					.format(ErrorMessageKeys.ATTRIB_PROHIBITED, "xlink:href", propTypeDef.getNamespace(), localName));
			}
			Assert.assertTrue(group.getParticles().size() == 1,
					ErrorMessage.format(ErrorMessageKeys.TOO_MANY_PARTS, propTypeDef.getNamespace(), localName));
			XSParticle particle = (XSParticle) group.getParticles().item(0);
			switch (particle.getTerm().getType()) {
				case XSConstants.ELEMENT_DECLARATION:
					assertSubstitition(model, head, propTypeDef, particle, includeHeadInSubstition);
					break;
				case XSConstants.MODEL_GROUP:
					XSModelGroup modelGroup = XSModelGroup.class.cast(particle.getTerm());
					if (modelGroup.getCompositor() != XSModelGroup.COMPOSITOR_CHOICE) {
						throw new AssertionError("Only a choice compositor is allowed. Found "
								+ particle.getTerm().getClass().getName());
					}
					@SuppressWarnings("unchecked")
					ListIterator<XSParticle> itr = modelGroup.getParticles().listIterator();
					while (itr.hasNext()) {
						XSParticle xsParticle = itr.next();
						if (xsParticle.getTerm().getType() != XSConstants.ELEMENT_DECLARATION) {
							throw new AssertionError("Only element declarations are allowed in choice compositor.");
						}
						assertSubstitition(model, head, propTypeDef, xsParticle, includeHeadInSubstition);
					}
					break;
				default:
					throw new AssertionError("Wildcard component not permitted in property type: "
							+ XMLSchemaModelUtils.getQName(propTypeDef));
			}
		}
	}

	/**
	 * Asserts that the given XML entity contains the expected number of descendant
	 * elements having the specified name.
	 * @param xmlEntity A Document representing an XML entity.
	 * @param elementName The qualified name of the element.
	 * @param expectedCount The expected number of occurrences.
	 */
	public static void assertDescendantElementCount(Document xmlEntity, QName elementName, int expectedCount) {
		NodeList features = xmlEntity.getElementsByTagNameNS(elementName.getNamespaceURI(), elementName.getLocalPart());
		Assert.assertEquals(features.getLength(), expectedCount,
				String.format("Unexpected number of %s descendant elements.", elementName));
	}

	/**
	 * Returns an attribute use schema component identified by qualified name.
	 * @param attrUses An XSObjectList containing a collection of XSAttributeUse items.
	 * @param qName The qualified name of the attribute declaration to seek. The namespace
	 * name may be empty ("").
	 * @return The matching XSAttributeUse object, or {@code null} if one cannot be found.
	 */
	private static XSAttributeUse getAttributeUseByName(XSObjectList attrUses, QName qName) {
		XSAttributeUse attrUse = null;
		for (int i = 0; i < attrUses.getLength(); i++) {
			XSAttributeUse item = (XSAttributeUse) attrUses.item(i);
			XSAttributeDeclaration attrDecl = item.getAttrDeclaration();
			if (attrDecl.getName().equals(qName.getLocalPart())) {
				if (attrDecl.getNamespace() != null && !attrDecl.getNamespace().equals(qName.getNamespaceURI())) {
					// XSObject.getNamespace() may return null!!
					continue;
				}
				attrUse = item;
				break;
			}
		}
		return attrUse;
	}

	private static void assertSubstitition(XSModel model, XSElementDeclaration head,
			XSComplexTypeDefinition propTypeDef, XSParticle xsParticle, boolean includeHead) {
		if (null != head) {
			XSElementDeclaration elemDecl = (XSElementDeclaration) xsParticle.getTerm();
			List<XSElementDeclaration> elementsByAffiliation = XMLSchemaModelUtils.getElementsByAffiliation(model,
					head);
			if (includeHead)
				elementsByAffiliation.add(head);
			Assert.assertTrue(elementsByAffiliation.contains(elemDecl),
					ErrorMessage.format(ErrorMessageKeys.DISALLOWED_SUBSTITUTION, elemDecl, head,
							XMLSchemaModelUtils.getQName(propTypeDef)));
		}
	}

}
