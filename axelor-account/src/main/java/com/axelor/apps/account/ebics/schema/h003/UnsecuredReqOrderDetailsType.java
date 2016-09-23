/*
 * XML Type:  UnsecuredReqOrderDetailsType
 * Namespace: http://www.ebics.org/H003
 * Java type: UnsecuredReqOrderDetailsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML UnsecuredReqOrderDetailsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface UnsecuredReqOrderDetailsType extends OrderDetailsType
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(UnsecuredReqOrderDetailsType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("unsecuredreqorderdetailstype61d2type");
    
    /**
     * Gets the "OrderID" element
     */
    java.lang.String getOrderID();
    
    /**
     * Gets (as xml) the "OrderID" element
     */
    OrderIDType xgetOrderID();
    
    /**
     * True if has "OrderID" element
     */
    boolean isSetOrderID();
    
    /**
     * Sets the "OrderID" element
     */
    void setOrderID(java.lang.String orderID);
    
    /**
     * Sets (as xml) the "OrderID" element
     */
    void xsetOrderID(OrderIDType orderID);
    
    /**
     * Unsets the "OrderID" element
     */
    void unsetOrderID();
    
    /**
     * Gets the "OrderAttribute" element
     */
    java.lang.String getOrderAttribute();
    
    /**
     * Gets (as xml) the "OrderAttribute" element
     */
    OrderAttributeBaseType xgetOrderAttribute();
    
    /**
     * Sets the "OrderAttribute" element
     */
    void setOrderAttribute(java.lang.String orderAttribute);
    
    /**
     * Sets (as xml) the "OrderAttribute" element
     */
    void xsetOrderAttribute(OrderAttributeBaseType orderAttribute);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static UnsecuredReqOrderDetailsType newInstance() {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static UnsecuredReqOrderDetailsType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static UnsecuredReqOrderDetailsType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static UnsecuredReqOrderDetailsType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static UnsecuredReqOrderDetailsType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static UnsecuredReqOrderDetailsType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static UnsecuredReqOrderDetailsType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static UnsecuredReqOrderDetailsType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static UnsecuredReqOrderDetailsType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static UnsecuredReqOrderDetailsType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static UnsecuredReqOrderDetailsType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static UnsecuredReqOrderDetailsType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static UnsecuredReqOrderDetailsType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static UnsecuredReqOrderDetailsType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static UnsecuredReqOrderDetailsType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static UnsecuredReqOrderDetailsType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static UnsecuredReqOrderDetailsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static UnsecuredReqOrderDetailsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (UnsecuredReqOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
