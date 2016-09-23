/*
 * XML Type:  X509IssuerSerialType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: X509IssuerSerialType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig;


/**
 * An XML X509IssuerSerialType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public interface X509IssuerSerialType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(X509IssuerSerialType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("x509issuerserialtype7eb2type");
    
    /**
     * Gets the "X509IssuerName" element
     */
    java.lang.String getX509IssuerName();
    
    /**
     * Gets (as xml) the "X509IssuerName" element
     */
    org.apache.xmlbeans.XmlString xgetX509IssuerName();
    
    /**
     * Sets the "X509IssuerName" element
     */
    void setX509IssuerName(java.lang.String x509IssuerName);
    
    /**
     * Sets (as xml) the "X509IssuerName" element
     */
    void xsetX509IssuerName(org.apache.xmlbeans.XmlString x509IssuerName);
    
    /**
     * Gets the "X509SerialNumber" element
     */
    java.math.BigInteger getX509SerialNumber();
    
    /**
     * Gets (as xml) the "X509SerialNumber" element
     */
    org.apache.xmlbeans.XmlInteger xgetX509SerialNumber();
    
    /**
     * Sets the "X509SerialNumber" element
     */
    void setX509SerialNumber(java.math.BigInteger x509SerialNumber);
    
    /**
     * Sets (as xml) the "X509SerialNumber" element
     */
    void xsetX509SerialNumber(org.apache.xmlbeans.XmlInteger x509SerialNumber);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static X509IssuerSerialType newInstance() {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static X509IssuerSerialType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static X509IssuerSerialType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static X509IssuerSerialType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static X509IssuerSerialType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static X509IssuerSerialType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static X509IssuerSerialType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static X509IssuerSerialType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static X509IssuerSerialType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static X509IssuerSerialType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static X509IssuerSerialType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static X509IssuerSerialType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static X509IssuerSerialType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static X509IssuerSerialType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static X509IssuerSerialType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static X509IssuerSerialType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static X509IssuerSerialType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static X509IssuerSerialType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (X509IssuerSerialType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
