/*
 * XML Type:  TransformType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: TransformType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig;


/**
 * An XML TransformType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public interface TransformType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(TransformType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("transformtype550btype");
    
    /**
     * Gets array of all "XPath" elements
     */
    java.lang.String[] getXPathArray();
    
    /**
     * Gets ith "XPath" element
     */
    java.lang.String getXPathArray(int i);
    
    /**
     * Gets (as xml) array of all "XPath" elements
     */
    org.apache.xmlbeans.XmlString[] xgetXPathArray();
    
    /**
     * Gets (as xml) ith "XPath" element
     */
    org.apache.xmlbeans.XmlString xgetXPathArray(int i);
    
    /**
     * Returns number of "XPath" element
     */
    int sizeOfXPathArray();
    
    /**
     * Sets array of all "XPath" element
     */
    void setXPathArray(java.lang.String[] xPathArray);
    
    /**
     * Sets ith "XPath" element
     */
    void setXPathArray(int i, java.lang.String xPath);
    
    /**
     * Sets (as xml) array of all "XPath" element
     */
    void xsetXPathArray(org.apache.xmlbeans.XmlString[] xPathArray);
    
    /**
     * Sets (as xml) ith "XPath" element
     */
    void xsetXPathArray(int i, org.apache.xmlbeans.XmlString xPath);
    
    /**
     * Inserts the value as the ith "XPath" element
     */
    void insertXPath(int i, java.lang.String xPath);
    
    /**
     * Appends the value as the last "XPath" element
     */
    void addXPath(java.lang.String xPath);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "XPath" element
     */
    org.apache.xmlbeans.XmlString insertNewXPath(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "XPath" element
     */
    org.apache.xmlbeans.XmlString addNewXPath();
    
    /**
     * Removes the ith "XPath" element
     */
    void removeXPath(int i);
    
    /**
     * Gets the "Algorithm" attribute
     */
    java.lang.String getAlgorithm();
    
    /**
     * Gets (as xml) the "Algorithm" attribute
     */
    org.apache.xmlbeans.XmlAnyURI xgetAlgorithm();
    
    /**
     * Sets the "Algorithm" attribute
     */
    void setAlgorithm(java.lang.String algorithm);
    
    /**
     * Sets (as xml) the "Algorithm" attribute
     */
    void xsetAlgorithm(org.apache.xmlbeans.XmlAnyURI algorithm);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static TransformType newInstance() {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static TransformType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static TransformType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static TransformType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static TransformType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static TransformType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static TransformType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static TransformType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static TransformType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static TransformType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static TransformType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static TransformType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static TransformType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static TransformType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static TransformType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static TransformType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static TransformType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static TransformType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (TransformType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
