/*
 * XML Type:  SPKIDataType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: SPKIDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig;


/**
 * An XML SPKIDataType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public interface SPKIDataType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(SPKIDataType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("spkidatatypea180type");
    
    /**
     * Gets array of all "SPKISexp" elements
     */
    byte[][] getSPKISexpArray();
    
    /**
     * Gets ith "SPKISexp" element
     */
    byte[] getSPKISexpArray(int i);
    
    /**
     * Gets (as xml) array of all "SPKISexp" elements
     */
    org.apache.xmlbeans.XmlBase64Binary[] xgetSPKISexpArray();
    
    /**
     * Gets (as xml) ith "SPKISexp" element
     */
    org.apache.xmlbeans.XmlBase64Binary xgetSPKISexpArray(int i);
    
    /**
     * Returns number of "SPKISexp" element
     */
    int sizeOfSPKISexpArray();
    
    /**
     * Sets array of all "SPKISexp" element
     */
    void setSPKISexpArray(byte[][] spkiSexpArray);
    
    /**
     * Sets ith "SPKISexp" element
     */
    void setSPKISexpArray(int i, byte[] spkiSexp);
    
    /**
     * Sets (as xml) array of all "SPKISexp" element
     */
    void xsetSPKISexpArray(org.apache.xmlbeans.XmlBase64Binary[] spkiSexpArray);
    
    /**
     * Sets (as xml) ith "SPKISexp" element
     */
    void xsetSPKISexpArray(int i, org.apache.xmlbeans.XmlBase64Binary spkiSexp);
    
    /**
     * Inserts the value as the ith "SPKISexp" element
     */
    void insertSPKISexp(int i, byte[] spkiSexp);
    
    /**
     * Appends the value as the last "SPKISexp" element
     */
    void addSPKISexp(byte[] spkiSexp);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "SPKISexp" element
     */
    org.apache.xmlbeans.XmlBase64Binary insertNewSPKISexp(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "SPKISexp" element
     */
    org.apache.xmlbeans.XmlBase64Binary addNewSPKISexp();
    
    /**
     * Removes the ith "SPKISexp" element
     */
    void removeSPKISexp(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static SPKIDataType newInstance() {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static SPKIDataType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static SPKIDataType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static SPKIDataType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static SPKIDataType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static SPKIDataType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static SPKIDataType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static SPKIDataType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static SPKIDataType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static SPKIDataType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static SPKIDataType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static SPKIDataType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static SPKIDataType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static SPKIDataType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static SPKIDataType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static SPKIDataType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static SPKIDataType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static SPKIDataType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (SPKIDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
