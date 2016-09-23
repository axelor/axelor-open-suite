/*
 * XML Type:  HVUOrderDetailsType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVUOrderDetailsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML HVUOrderDetailsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface HVUOrderDetailsType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(HVUOrderDetailsType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("hvuorderdetailstypec41dtype");
    
    /**
     * Gets the "OrderType" element
     */
    java.lang.String getOrderType();
    
    /**
     * Gets (as xml) the "OrderType" element
     */
    OrderTBaseType xgetOrderType();
    
    /**
     * Sets the "OrderType" element
     */
    void setOrderType(java.lang.String orderType);
    
    /**
     * Sets (as xml) the "OrderType" element
     */
    void xsetOrderType(OrderTBaseType orderType);
    
    /**
     * Gets the "OrderID" element
     */
    java.lang.String getOrderID();
    
    /**
     * Gets (as xml) the "OrderID" element
     */
    OrderIDType xgetOrderID();
    
    /**
     * Sets the "OrderID" element
     */
    void setOrderID(java.lang.String orderID);
    
    /**
     * Sets (as xml) the "OrderID" element
     */
    void xsetOrderID(OrderIDType orderID);
    
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
     * Gets the "SigningInfo" element
     */
    HVUSigningInfoType getSigningInfo();
    
    /**
     * Sets the "SigningInfo" element
     */
    void setSigningInfo(HVUSigningInfoType signingInfo);
    
    /**
     * Appends and returns a new empty "SigningInfo" element
     */
    HVUSigningInfoType addNewSigningInfo();
    
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
     * Gets the "OriginatorInfo" element
     */
    HVUOriginatorInfoType getOriginatorInfo();
    
    /**
     * Sets the "OriginatorInfo" element
     */
    void setOriginatorInfo(HVUOriginatorInfoType originatorInfo);
    
    /**
     * Appends and returns a new empty "OriginatorInfo" element
     */
    HVUOriginatorInfoType addNewOriginatorInfo();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static HVUOrderDetailsType newInstance() {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static HVUOrderDetailsType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static HVUOrderDetailsType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static HVUOrderDetailsType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static HVUOrderDetailsType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static HVUOrderDetailsType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static HVUOrderDetailsType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static HVUOrderDetailsType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static HVUOrderDetailsType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static HVUOrderDetailsType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static HVUOrderDetailsType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static HVUOrderDetailsType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static HVUOrderDetailsType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static HVUOrderDetailsType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static HVUOrderDetailsType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static HVUOrderDetailsType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HVUOrderDetailsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HVUOrderDetailsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HVUOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
