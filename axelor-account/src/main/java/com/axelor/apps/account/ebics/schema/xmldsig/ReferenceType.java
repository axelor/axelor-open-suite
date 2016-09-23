/*
 * XML Type:  ReferenceType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: ReferenceType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig;


/**
 * An XML ReferenceType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public interface ReferenceType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(ReferenceType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("referencetypef44ctype");
    
    /**
     * Gets the "Transforms" element
     */
    TransformsType getTransforms();
    
    /**
     * True if has "Transforms" element
     */
    boolean isSetTransforms();
    
    /**
     * Sets the "Transforms" element
     */
    void setTransforms(TransformsType transforms);
    
    /**
     * Appends and returns a new empty "Transforms" element
     */
    TransformsType addNewTransforms();
    
    /**
     * Unsets the "Transforms" element
     */
    void unsetTransforms();
    
    /**
     * Gets the "DigestMethod" element
     */
    DigestMethodType getDigestMethod();
    
    /**
     * Sets the "DigestMethod" element
     */
    void setDigestMethod(DigestMethodType digestMethod);
    
    /**
     * Appends and returns a new empty "DigestMethod" element
     */
    DigestMethodType addNewDigestMethod();
    
    /**
     * Gets the "DigestValue" element
     */
    byte[] getDigestValue();
    
    /**
     * Gets (as xml) the "DigestValue" element
     */
    DigestValueType xgetDigestValue();
    
    /**
     * Sets the "DigestValue" element
     */
    void setDigestValue(byte[] digestValue);
    
    /**
     * Sets (as xml) the "DigestValue" element
     */
    void xsetDigestValue(DigestValueType digestValue);
    
    /**
     * Gets the "Id" attribute
     */
    java.lang.String getId();
    
    /**
     * Gets (as xml) the "Id" attribute
     */
    org.apache.xmlbeans.XmlID xgetId();
    
    /**
     * True if has "Id" attribute
     */
    boolean isSetId();
    
    /**
     * Sets the "Id" attribute
     */
    void setId(java.lang.String id);
    
    /**
     * Sets (as xml) the "Id" attribute
     */
    void xsetId(org.apache.xmlbeans.XmlID id);
    
    /**
     * Unsets the "Id" attribute
     */
    void unsetId();
    
    /**
     * Gets the "URI" attribute
     */
    java.lang.String getURI();
    
    /**
     * Gets (as xml) the "URI" attribute
     */
    org.apache.xmlbeans.XmlAnyURI xgetURI();
    
    /**
     * True if has "URI" attribute
     */
    boolean isSetURI();
    
    /**
     * Sets the "URI" attribute
     */
    void setURI(java.lang.String uri);
    
    /**
     * Sets (as xml) the "URI" attribute
     */
    void xsetURI(org.apache.xmlbeans.XmlAnyURI uri);
    
    /**
     * Unsets the "URI" attribute
     */
    void unsetURI();
    
    /**
     * Gets the "Type" attribute
     */
    java.lang.String getType();
    
    /**
     * Gets (as xml) the "Type" attribute
     */
    org.apache.xmlbeans.XmlAnyURI xgetType();
    
    /**
     * True if has "Type" attribute
     */
    boolean isSetType();
    
    /**
     * Sets the "Type" attribute
     */
    void setType(java.lang.String type);
    
    /**
     * Sets (as xml) the "Type" attribute
     */
    void xsetType(org.apache.xmlbeans.XmlAnyURI type);
    
    /**
     * Unsets the "Type" attribute
     */
    void unsetType();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static ReferenceType newInstance() {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static ReferenceType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static ReferenceType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static ReferenceType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static ReferenceType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static ReferenceType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static ReferenceType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static ReferenceType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static ReferenceType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static ReferenceType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static ReferenceType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static ReferenceType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static ReferenceType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static ReferenceType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static ReferenceType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static ReferenceType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static ReferenceType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static ReferenceType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (ReferenceType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
