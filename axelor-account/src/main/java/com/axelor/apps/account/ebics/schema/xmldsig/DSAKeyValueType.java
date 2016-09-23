/*
 * XML Type:  DSAKeyValueType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: DSAKeyValueType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig;


/**
 * An XML DSAKeyValueType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public interface DSAKeyValueType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(DSAKeyValueType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("dsakeyvaluetypee913type");
    
    /**
     * Gets the "P" element
     */
    byte[] getP();
    
    /**
     * Gets (as xml) the "P" element
     */
    CryptoBinary xgetP();
    
    /**
     * True if has "P" element
     */
    boolean isSetP();
    
    /**
     * Sets the "P" element
     */
    void setP(byte[] p);
    
    /**
     * Sets (as xml) the "P" element
     */
    void xsetP(CryptoBinary p);
    
    /**
     * Unsets the "P" element
     */
    void unsetP();
    
    /**
     * Gets the "Q" element
     */
    byte[] getQ();
    
    /**
     * Gets (as xml) the "Q" element
     */
    CryptoBinary xgetQ();
    
    /**
     * True if has "Q" element
     */
    boolean isSetQ();
    
    /**
     * Sets the "Q" element
     */
    void setQ(byte[] q);
    
    /**
     * Sets (as xml) the "Q" element
     */
    void xsetQ(CryptoBinary q);
    
    /**
     * Unsets the "Q" element
     */
    void unsetQ();
    
    /**
     * Gets the "G" element
     */
    byte[] getG();
    
    /**
     * Gets (as xml) the "G" element
     */
    CryptoBinary xgetG();
    
    /**
     * True if has "G" element
     */
    boolean isSetG();
    
    /**
     * Sets the "G" element
     */
    void setG(byte[] g);
    
    /**
     * Sets (as xml) the "G" element
     */
    void xsetG(CryptoBinary g);
    
    /**
     * Unsets the "G" element
     */
    void unsetG();
    
    /**
     * Gets the "Y" element
     */
    byte[] getY();
    
    /**
     * Gets (as xml) the "Y" element
     */
    CryptoBinary xgetY();
    
    /**
     * Sets the "Y" element
     */
    void setY(byte[] y);
    
    /**
     * Sets (as xml) the "Y" element
     */
    void xsetY(CryptoBinary y);
    
    /**
     * Gets the "J" element
     */
    byte[] getJ();
    
    /**
     * Gets (as xml) the "J" element
     */
    CryptoBinary xgetJ();
    
    /**
     * True if has "J" element
     */
    boolean isSetJ();
    
    /**
     * Sets the "J" element
     */
    void setJ(byte[] j);
    
    /**
     * Sets (as xml) the "J" element
     */
    void xsetJ(CryptoBinary j);
    
    /**
     * Unsets the "J" element
     */
    void unsetJ();
    
    /**
     * Gets the "Seed" element
     */
    byte[] getSeed();
    
    /**
     * Gets (as xml) the "Seed" element
     */
    CryptoBinary xgetSeed();
    
    /**
     * True if has "Seed" element
     */
    boolean isSetSeed();
    
    /**
     * Sets the "Seed" element
     */
    void setSeed(byte[] seed);
    
    /**
     * Sets (as xml) the "Seed" element
     */
    void xsetSeed(CryptoBinary seed);
    
    /**
     * Unsets the "Seed" element
     */
    void unsetSeed();
    
    /**
     * Gets the "PgenCounter" element
     */
    byte[] getPgenCounter();
    
    /**
     * Gets (as xml) the "PgenCounter" element
     */
    CryptoBinary xgetPgenCounter();
    
    /**
     * True if has "PgenCounter" element
     */
    boolean isSetPgenCounter();
    
    /**
     * Sets the "PgenCounter" element
     */
    void setPgenCounter(byte[] pgenCounter);
    
    /**
     * Sets (as xml) the "PgenCounter" element
     */
    void xsetPgenCounter(CryptoBinary pgenCounter);
    
    /**
     * Unsets the "PgenCounter" element
     */
    void unsetPgenCounter();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static DSAKeyValueType newInstance() {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static DSAKeyValueType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static DSAKeyValueType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static DSAKeyValueType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static DSAKeyValueType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static DSAKeyValueType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static DSAKeyValueType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static DSAKeyValueType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static DSAKeyValueType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static DSAKeyValueType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static DSAKeyValueType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static DSAKeyValueType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static DSAKeyValueType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static DSAKeyValueType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static DSAKeyValueType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static DSAKeyValueType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static DSAKeyValueType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static DSAKeyValueType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (DSAKeyValueType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
