package org.opengis.cite.iso19136.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.opengis.cite.iso19136.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides various utility methods for accessing or manipulating XML representations.
 */
public class XMLUtils {

	private static final Logger LOGR = Logger.getLogger(XMLUtils.class.getPackage().getName());

	private static final XPathFactory XPATH_FACTORY = initXPathFactory();

	private static XPathFactory initXPathFactory() {
		XPathFactory factory = XPathFactory.newInstance();
		return factory;
	}

	/**
	 * Writes the content of a DOM Node to a string. The XML declaration is omitted and
	 * the character encoding is set to "US-ASCII" (any character outside of this set is
	 * serialized as a numeric character reference).
	 * @param node The DOM Node to be serialized.
	 * @return A String representing the content of the given node.
	 */
	public static String writeNodeToString(Node node) {
		if (null == node) {
			return "";
		}
		Writer writer = null;
		try {
			Transformer idTransformer = TransformerFactory.newInstance().newTransformer();
			Properties outProps = new Properties();
			outProps.setProperty(OutputKeys.ENCODING, "US-ASCII");
			outProps.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			outProps.setProperty(OutputKeys.INDENT, "yes");
			idTransformer.setOutputProperties(outProps);
			writer = new StringWriter();
			idTransformer.transform(new DOMSource(node), new StreamResult(writer));
		}
		catch (TransformerException ex) {
			TestSuiteLogger.log(Level.WARNING, "Failed to serialize node " + node.getNodeName(), ex);
		}
		return writer.toString();
	}

	/**
	 * Writes the result of a transformation to a String. An XML declaration is always
	 * omitted.
	 * @param result An object (DOMResult or StreamResult) that holds the result of a
	 * transformation, which may be XML or plain text.
	 * @return A String representing the content of the result; it may be empty if the
	 * content could not be read.
	 */
	public static String resultToString(Result result) {
		if (null == result) {
			throw new IllegalArgumentException("Result is null.");
		}
		StringWriter writer = new StringWriter();
		if (result instanceof DOMResult) {
			Node node = DOMResult.class.cast(result).getNode();
			Properties outProps = new Properties();
			outProps.setProperty(OutputKeys.ENCODING, "UTF-8");
			outProps.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			outProps.setProperty(OutputKeys.INDENT, "yes");
			Transformer idTransformer;
			try {
				idTransformer = TransformerFactory.newInstance().newTransformer();
				idTransformer.setOutputProperties(outProps);
				idTransformer.transform(new DOMSource(node), new StreamResult(writer));
			}
			catch (TransformerFactoryConfigurationError | TransformerException e) {
				LOGR.warning(e.getMessage());
			}
		}
		else if (result instanceof StreamResult) {
			StreamResult streamResult = StreamResult.class.cast(result);
			OutputStream os = streamResult.getOutputStream();
			if (null != os) {
				writer.write(os.toString()); // probably ByteArrayOutputStream
			}
			else { // try system id or writer
				Path path = Paths.get(URI.create(streamResult.getSystemId()));
				try {
					byte[] data = Files.readAllBytes(path);
					writer.write(new String(data));
				}
				catch (IOException e) {
					LOGR.warning(e.getMessage());
				}
			}
		}
		else {
			throw new IllegalArgumentException("Unsupported Result type:" + result.getClass());
		}
		return writer.toString();
	}

	/**
	 * Writes the content of a DOM Node to a byte stream. An XML declaration is always
	 * omitted.
	 * @param node The DOM Node to be serialized.
	 * @param outputStream The destination OutputStream reference.
	 */
	public static void writeNode(Node node, OutputStream outputStream) {
		try {
			Transformer idTransformer = TransformerFactory.newInstance().newTransformer();
			Properties outProps = new Properties();
			outProps.setProperty(OutputKeys.METHOD, "xml");
			outProps.setProperty(OutputKeys.ENCODING, "UTF-8");
			outProps.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			outProps.setProperty(OutputKeys.INDENT, "yes");
			idTransformer.setOutputProperties(outProps);
			idTransformer.transform(new DOMSource(node), new StreamResult(outputStream));
		}
		catch (TransformerException ex) {
			String nodeName = (node.getNodeType() == Node.DOCUMENT_NODE)
					? Document.class.cast(node).getDocumentElement().getNodeName() : node.getNodeName();
			TestSuiteLogger.log(Level.WARNING, "Failed to serialize DOM node: " + nodeName, ex);
		}
	}

	/**
	 * Evaluates an XPath 1.0 expression using the given context and returns the result as
	 * a node set.
	 * @param context The context node.
	 * @param expr An XPath expression.
	 * @param namespaceBindings A collection of namespace bindings for the XPath
	 * expression, where each entry maps a namespace URI (key) to a prefix (value).
	 * Standard bindings do not need to be declared (see
	 * {@link NamespaceBindings#withStandardBindings()}.
	 * @return A NodeList containing nodes that satisfy the expression (it may be empty).
	 * @throws XPathExpressionException If the expression cannot be evaluated for any
	 * reason.
	 */
	public static NodeList evaluateXPath(Node context, String expr, Map<String, String> namespaceBindings)
			throws XPathExpressionException {
		Object result = evaluateXPath(context, expr, namespaceBindings, XPathConstants.NODESET);
		if (!NodeList.class.isInstance(result)) {
			throw new XPathExpressionException("Expression does not evaluate to a NodeList: " + expr);
		}
		return (NodeList) result;
	}

	/**
	 * Evaluates an XPath expression using the given context and returns the result as the
	 * specified type.
	 *
	 * <p>
	 * <strong>Note:</strong> The Saxon implementation supports XPath 2.0 expressions when
	 * using the JAXP XPath APIs (the default implementation will throw an exception).
	 * </p>
	 * @param context The context node.
	 * @param expr An XPath expression.
	 * @param namespaceBindings A collection of namespace bindings for the XPath
	 * expression, where each entry maps a namespace URI (key) to a prefix (value).
	 * Standard bindings do not need to be declared (see
	 * {@link NamespaceBindings#withStandardBindings()}.
	 * @param returnType The desired return type (as declared in {@link XPathConstants} ).
	 * @return The result converted to the desired returnType.
	 * @throws XPathExpressionException If the expression cannot be evaluated for any
	 * reason.
	 */
	public static Object evaluateXPath(Node context, String expr, Map<String, String> namespaceBindings,
			QName returnType) throws XPathExpressionException {
		NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
		bindings.addAllBindings(namespaceBindings);
		XPathFactory factory = XPATH_FACTORY;
		// WARNING: If context node is Saxon NodeOverNodeInfo, the factory must
		// use the same Configuration object to avoid IllegalArgumentException
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(bindings);
		Object result = xpath.evaluate(expr, context, returnType);
		return result;
	}

	/**
	 * Evaluates an XPath 1.0 expression using the given Source object and returns the
	 * result as the specified type.
	 * @param source A Source object (not StAXSource) that supplies an XML entity.
	 * @param expr An XPath expression.
	 * @param namespaceBindings A collection of namespace bindings for the XPath
	 * expression, where each entry maps a namespace URI (key) to a prefix (value).
	 * Standard bindings do not need to be declared (see
	 * {@link NamespaceBindings#withStandardBindings()}.
	 * @param returnType The desired return type (as declared in {@link XPathConstants} ).
	 * @return The result converted to the desired returnType.
	 * @throws XPathExpressionException If the expression cannot be evaluated for any
	 * reason.
	 */
	public static Object evaluateXPath(Source source, String expr, Map<String, String> namespaceBindings,
			QName returnType) throws XPathExpressionException {
		if (StAXSource.class.isInstance(source)) {
			throw new IllegalArgumentException("StAXSource not supported.");
		}
		if (DOMSource.class.isInstance(source)) {
			DOMSource domSource = (DOMSource) source;
			return evaluateXPath(domSource.getNode(), expr, namespaceBindings, returnType);
		}
		InputSource xmlSource = null;
		if (StreamSource.class.isInstance(source)) {
			StreamSource streamSource = (StreamSource) source;
			xmlSource = new InputSource(streamSource.getInputStream());
			xmlSource.setSystemId(source.getSystemId());
		}
		if (SAXSource.class.isInstance(source)) {
			SAXSource saxSource = (SAXSource) source;
			xmlSource = saxSource.getInputSource();
			xmlSource.setSystemId(source.getSystemId());
		}
		NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
		bindings.addAllBindings(namespaceBindings);
		XPath xpath = XPATH_FACTORY.newXPath();
		xpath.setNamespaceContext(bindings);
		return xpath.evaluate(expr, xmlSource, returnType);
	}

	/**
	 * Evaluates an XPath 2.0 expression using the Saxon s9api interfaces.
	 * @param xmlSource The XML Source.
	 * @param expr The XPath expression to be evaluated.
	 * @param nsBindings A collection of namespace bindings required to evaluate the XPath
	 * expression, where each entry maps a namespace URI (key) to a prefix (value); this
	 * may be {@code null} if not needed.
	 * @return An XdmValue object representing a value in the XDM data model; this is a
	 * sequence of zero or more items, where each item is either an atomic value or a
	 * node.
	 * @throws SaxonApiException If an error occurs while evaluating the expression; this
	 * always wraps some other underlying exception.
	 */
	public static XdmValue evaluateXPath2(Source xmlSource, String expr, Map<String, String> nsBindings)
			throws SaxonApiException {
		Processor proc = new Processor(false);
		XPathCompiler compiler = proc.newXPathCompiler();
		if (null != nsBindings) {
			for (String nsURI : nsBindings.keySet()) {
				compiler.declareNamespace(nsBindings.get(nsURI), nsURI);
			}
		}
		XPathSelector xpath = compiler.compile(expr).load();
		DocumentBuilder builder = proc.newDocumentBuilder();
		XdmNode node = null;
		if (DOMSource.class.isInstance(xmlSource)) {
			DOMSource domSource = (DOMSource) xmlSource;
			node = builder.wrap(domSource.getNode());
		}
		else {
			node = builder.build(xmlSource);
		}
		xpath.setContextItem(node);
		return xpath.evaluate();
	}

	/**
	 * Creates a new Element having the specified qualified name. The element must be
	 * {@link Document#adoptNode(Node) adopted} when inserted into another Document.
	 * @param qName A QName object.
	 * @return An Element node (with a Document owner but no parent).
	 */
	public static Element createElement(QName qName) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		Element elem = doc.createElementNS(qName.getNamespaceURI(), qName.getLocalPart());
		return elem;
	}

	/**
	 * Returns a List of all descendant Element nodes having the specified [namespace
	 * name] property. The elements are listed in document order.
	 * @param node The node to search from.
	 * @param namespaceURI An absolute URI denoting a namespace name.
	 * @return A List containing elements in the specified namespace; the list is empty if
	 * there are no elements in the namespace.
	 */
	public static List<Element> getElementsByNamespaceURI(Node node, String namespaceURI) {
		List<Element> list = new ArrayList<Element>();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if (child.getNamespaceURI().equals(namespaceURI))
				list.add((Element) child);
		}
		return list;
	}

	/**
	 * Transforms the content of a DOM Node using a specified XSLT stylesheet.
	 * @param xslt A Source object representing a stylesheet (XSLT 1.0 or 2.0).
	 * @param source A Node representing the XML source. If it is an Element node it will
	 * be imported into a new DOM Document.
	 * @return A DOM Document containing the result of the transformation.
	 */
	public static Document transform(Source xslt, Node source) {
		Document sourceDoc = null;
		Document resultDoc = null;
		try {
			resultDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			if (source.getNodeType() == Node.DOCUMENT_NODE) {
				sourceDoc = (Document) source;
			}
			else {
				sourceDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				sourceDoc.appendChild(sourceDoc.importNode(source, true));
			}
		}
		catch (ParserConfigurationException pce) {
			throw new RuntimeException(pce);
		}
		Processor processor = new Processor(false);
		XsltCompiler compiler = processor.newXsltCompiler();
		try {
			XsltExecutable exec = compiler.compile(xslt);
			XsltTransformer transformer = exec.load();
			transformer.setSource(new DOMSource(sourceDoc));
			transformer.setDestination(new DOMDestination(resultDoc));
			transformer.transform();
		}
		catch (SaxonApiException e) {
			throw new RuntimeException(e);
		}
		return resultDoc;
	}

	/**
	 * Reads the given property element and returns either (a) the child element, or (b)
	 * the XLink referent. If the xlink:href attribute is present an attempt will be made
	 * to dereference the URI value, which may contain a fragment identifier (a string
	 * that adheres to the XPointer syntax).
	 * @param propertyNode A property node; the value is supplied in-line or by reference.
	 * @return A DOM Node representing the property value, or {@code null} if it cannot be
	 * accessed or parsed.
	 */
	public static Node getPropertyValue(final Node propertyNode) {
		Element value = null;
		Element propElem = (Element) propertyNode;
		String href = propElem.getAttributeNS(Namespaces.XLINK, "href");
		if (href.isEmpty()) {
			value = (Element) propElem.getElementsByTagName("*").item(0);
		}
		else {
			URI uriRef = null;
			try {
				Document referent = null;
				uriRef = URI.create(href);
				if (!uriRef.isAbsolute()) {
					String baseURI = propertyNode.getOwnerDocument().getBaseURI();
					uriRef = URIUtils.resolveRelativeURI(baseURI, uriRef.toString());
				}
				referent = URIUtils.parseURI(uriRef);
				if (null == uriRef.getFragment()) {
					value = referent.getDocumentElement();
				}
				else {
					value = getFragment(referent, uriRef.getFragment());
				}
			}
			catch (SAXException | IOException e) {
				TestSuiteLogger.log(Level.WARNING, String.format("Failed to read value of property %s from %s",
						propertyNode.getNodeName(), uriRef));
			}
		}
		return value;
	}

	/**
	 * Extracts the specified fragment from the given XML source document. The fragment
	 * identifier is expected to conform to the W3C XPointer framework. However, only
	 * shorthand pointers are currently supported.
	 *
	 * <p>
	 * In GML documents such pointers refer to the element that has a matching gml:id
	 * attribute value; this attribute is a <a target="_blank" href=
	 * "http://www.w3.org/TR/xptr-framework/#term-sdi">schema-determined ID</a> as defined
	 * in the XPointer specification.
	 * </p>
	 * @param doc A DOM Document.
	 * @param fragmentId A fragment identifier that adheres to the XPointer syntax.
	 * @return A copy of the matching Element, or {@code null} if no matching element was
	 * found.
	 * @see <a target="_blank" href= "http://www.w3.org/TR/xptr-framework/">XPointer
	 * Framework</a>
	 * @see <a target="_blank" href=
	 * "http://www.w3.org/2005/04/xpointer-schemes/">XPointer Registry</a>
	 */
	public static Element getFragment(Document doc, String fragmentId) {
		if (fragmentId.indexOf('(') > 0) {
			throw new UnsupportedOperationException("Scheme-based pointers are not currently supported.");
		}
		Element fragment = null;
		String expr = String.format("//*[@gml:id='%s']", fragmentId);
		try {
			NodeList nodeList = evaluateXPath(doc, expr, null);
			if (nodeList.getLength() > 0) {
				fragment = (Element) nodeList.item(0).cloneNode(true);
			}
		}
		catch (XPathExpressionException xpe) {
			throw new RuntimeException(xpe);
		}
		return fragment;
	}

}
