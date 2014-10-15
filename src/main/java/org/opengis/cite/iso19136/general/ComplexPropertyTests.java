package org.opengis.cite.iso19136.general;

import java.util.List;

import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSTypeDefinition;
import org.opengis.cite.iso19136.ETSAssert;
import org.opengis.cite.iso19136.ErrorMessage;
import org.opengis.cite.iso19136.ErrorMessageKeys;
import org.opengis.cite.iso19136.util.GMLObjectTypeFilter;
import org.opengis.cite.iso19136.util.XMLSchemaModelUtils;
import org.testng.annotations.Test;

/**
 * Checks constraints that apply to various complex properties of GML objects.
 * The values of such properties (that typically define binary associations)
 * include metadata descriptions, spatial geometry objects, topology objects,
 * temporal objects, and location descriptions.
 * 
 * Implicit property type definitions are examined to ensure they have valid
 * content models, since invalid type derivations (i.e. restrictions of existing
 * GML property types) should be detected when the application schema is
 * initially assessed for XML Schema compliance.
 * 
 * <h6 style="margin-bottom: 0.5em">Sources</h6>
 * <ul>
 * <li>ISO 19136:2007, cl. A.1.1.8: Content model of property elements</li>
 * <li>ISO 19136:2007, cl. 7.2.3: GML properties</li>
 * </ul>
 */
public class ComplexPropertyTests extends SchemaModelFixture {

    /**
     * [{@code Test}] Validates the content of metadata properties. A metadata
     * property type must be derived by extension from
     * gml:AbstractMetadataPropertyType; it shall follow one of the patterns
     * specified for GML property types in clause 7.2.3, and cannot contain a
     * wildcard particle. A property value must be specified--an empty content
     * model is not permitted even if a reference is allowed.
     * 
     * <h6 style="margin-bottom: 0.5em">Sources</h6>
     * <ul>
     * <li>ISO 19136:2007, cl. A.1.1.9: Metadata and data quality properties</li>
     * <li>ISO 19136:2007, cl. 7.2.6: Metadata</li>
     * </ul>
     */
    @Test(description = "See ISO 19136: 7.2.6, A.1.1.9")
    public void validateMetadataProperties() {
        XSTypeDefinition metadataPropType = this.model.getTypeDefinition(
                GML32.MD_PROP_TYPE, GML32.NS_NAME);
        List<XSElementDeclaration> metadataProps = XMLSchemaModelUtils
                .getGlobalElementsByType(this.model, metadataPropType);
        metadataProps.addAll(XMLSchemaModelUtils.getLocalElementsByType(
                this.model, metadataPropType, new GMLObjectTypeFilter()));
        for (XSElementDeclaration metaDataProp : metadataProps) {
            ETSAssert.assertValidPropertyType(model, metaDataProp, null);
            XSComplexTypeDefinition propTypeDef = (XSComplexTypeDefinition) metaDataProp
                    .getTypeDefinition();
            if (propTypeDef.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_EMPTY) {
                throw new AssertionError(ErrorMessage.format(
                        ErrorMessageKeys.MD_PROP_UNSPECIFIED, metaDataProp));
            }
        }
    }

    /**
     * [{@code Test}] A GML object collection is any gml:AbstractObject having a
     * property element whose content model is derived by extension from
     * gml:AbstractMemberType. A member property type shall follow one of the
     * patterns specified for GML property types in clause 7.2.3.
     * 
     * Furthermore, the collection type may also include a reference to the
     * attribute group gml:AggregationAttributeGroup in order to provide
     * additional information about the semantics of the object collection.
     * 
     * <h6 style="margin-bottom: 0.5em">Sources</h6>
     * <ul>
     * <li>ISO 19136:2007, cl. A.1.1.14: GML Object Collections</li>
     * <li>ISO 19136:2007, cl. 7.2.5: Collections of GML Objects</li>
     * </ul>
     */
    @Test(description = "See ISO 19136: 7.2.5, A.1.1.14")
    public void validateMembersOfGmlObjectCollection() {
        XSTypeDefinition memberPropType = this.model.getTypeDefinition(
                GML32.MEMBER_PROP_TYPE, GML32.NS_NAME);
        List<XSElementDeclaration> memberProps = XMLSchemaModelUtils
                .getGlobalElementsByType(this.model, memberPropType);
        memberProps.addAll(XMLSchemaModelUtils.getLocalElementsByType(
                this.model, memberPropType, null));
        XSElementDeclaration gmlObj = this.model.getElementDeclaration(
                GML32.ABSTRACT_GML, GML32.NS_NAME);
        for (XSElementDeclaration memberProp : memberProps) {
            ETSAssert.assertValidPropertyType(model, memberProp, gmlObj);
        }
    }
}
