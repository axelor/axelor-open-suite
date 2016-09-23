/*
 * XML Type:  HVDResponseOrderDataType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVDResponseOrderDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML HVDResponseOrderDataType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface HVDResponseOrderDataType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(HVDResponseOrderDataType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("hvdresponseorderdatatype7855type");
    
    /**
     * Gets the "DataDigest" element
     */
    DataDigestType getDataDigest();
    
    /**
     * Sets the "DataDigest" element
     */
    void setDataDigest(DataDigestType dataDigest);
    
    /**
     * Appends and returns a new empty "DataDigest" element
     */
    DataDigestType addNewDataDigest();
    
    /**
     * Gets the "DisplayFile" element
     */
    byte[] getDisplayFile();
    
    /**
     * Gets (as xml) the "DisplayFile" element
     */
    org.apache.xmlbeans.XmlBase64Binary xgetDisplayFile();
    
    /**
     * Sets the "DisplayFile" element
     */
    void setDisplayFile(byte[] displayFile);
    
    /**
     * Sets (as xml) the "DisplayFile" element
     */
    void xsetDisplayFile(org.apache.xmlbeans.XmlBase64Binary displayFile);
    
    /**
     * Gets the "OrderDataAvailable" element
     */
    boolean getOrderDataAvailable();
    
    /**
     * Gets (as xml) the "OrderDataAvailable" element
     */
    org.apache.xmlbeans.XmlBoolean xgetOrderDataAvailable();
    
    /**
     * Sets the "OrderDataAvailable" element
     */
    void setOrderDataAvailable(boolean orderDataAvailable);
    
    /**
     * Sets (as xml) the "OrderDataAvailable" element
     */
    void xsetOrderDataAvailable(org.apache.xmlbeans.XmlBoolean orderDataAvailable);
    
    /**
     * Gets the "OrderDataSize" element
     */
    java.math.BigInteger getOrderDataSize();
    
    /**
     * Gets (as xml) the "OrderDataSize" element
     */
    org.apache.xmlbeans.XmlPositiveInteger xgetOrderDataSize();
    
    /**
     * Sets the "OrderDataSize" element
     */
    void setOrderDataSize(java.math.BigInteger orderDataSize);
    
    /**
     * Sets (as xml) the "OrderDataSize" element
     */
    void xsetOrderDataSize(org.apache.xmlbeans.XmlPositiveInteger orderDataSize);
    
    /**
     * Gets the "OrderDetailsAvailable" element
     */
    boolean getOrderDetailsAvailable();
    
    /**
     * Gets (as xml) the "OrderDetailsAvailable" element
     */
    org.apache.xmlbeans.XmlBoolean xgetOrderDetailsAvailable();
    
    /**
     * Sets the "OrderDetailsAvailable" element
     */
    void setOrderDetailsAvailable(boolean orderDetailsAvailable);
    
    /**
     * Sets (as xml) the "OrderDetailsAvailable" element
     */
    void xsetOrderDetailsAvailable(org.apache.xmlbeans.XmlBoolean orderDetailsAvailable);
    
    /**
     * Gets array of all "SignerInfo" elements
     */
    SignerInfoType[] getSignerInfoArray();
    
    /**
     * Gets ith "SignerInfo" element
     */
    SignerInfoType getSignerInfoArray(int i);
    
    /**
     * Returns number of "SignerInfo" element
     */
    int sizeOfSignerInfoArray();
    
    /**
     * Sets array of all "SignerInfo" element
     */
    void setSignerInfoArray(SignerInfoType[] signerInfoArray);
    
    /**
     * Sets ith "SignerInfo" element
     */
    void setSignerInfoArray(int i, SignerInfoType signerInfo);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "SignerInfo" element
     */
    SignerInfoType insertNewSignerInfo(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "SignerInfo" element
     */
    SignerInfoType addNewSignerInfo();
    
    /**
     * Removes the ith "SignerInfo" element
     */
    void removeSignerInfo(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static HVDResponseOrderDataType newInstance() {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static HVDResponseOrderDataType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static HVDResponseOrderDataType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static HVDResponseOrderDataType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static HVDResponseOrderDataType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static HVDResponseOrderDataType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static HVDResponseOrderDataType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static HVDResponseOrderDataType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static HVDResponseOrderDataType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static HVDResponseOrderDataType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static HVDResponseOrderDataType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static HVDResponseOrderDataType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static HVDResponseOrderDataType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static HVDResponseOrderDataType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static HVDResponseOrderDataType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static HVDResponseOrderDataType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HVDResponseOrderDataType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HVDResponseOrderDataType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HVDResponseOrderDataType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
