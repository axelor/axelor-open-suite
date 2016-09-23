/*
 * XML Type:  StaticHeaderOrderDetailsType
 * Namespace: http://www.ebics.org/H003
 * Java type: StaticHeaderOrderDetailsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML StaticHeaderOrderDetailsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface StaticHeaderOrderDetailsType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(StaticHeaderOrderDetailsType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("staticheaderorderdetailstype6643type");
    
    /**
     * Gets the "OrderType" element
     */
    StaticHeaderOrderDetailsType.OrderType getOrderType();
    
    /**
     * Sets the "OrderType" element
     */
    void setOrderType(StaticHeaderOrderDetailsType.OrderType orderType);
    
    /**
     * Appends and returns a new empty "OrderType" element
     */
    StaticHeaderOrderDetailsType.OrderType addNewOrderType();
    
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
    OrderAttributeType.Enum getOrderAttribute();
    
    /**
     * Gets (as xml) the "OrderAttribute" element
     */
    OrderAttributeType xgetOrderAttribute();
    
    /**
     * Sets the "OrderAttribute" element
     */
    void setOrderAttribute(OrderAttributeType.Enum orderAttribute);
    
    /**
     * Sets (as xml) the "OrderAttribute" element
     */
    void xsetOrderAttribute(OrderAttributeType orderAttribute);
    
    /**
     * Gets the "OrderParams" element
     */
    org.apache.xmlbeans.XmlObject getOrderParams();
    
    /**
     * Sets the "OrderParams" element
     */
    void setOrderParams(org.apache.xmlbeans.XmlObject orderParams);
    
    /**
     * Appends and returns a new empty "OrderParams" element
     */
    org.apache.xmlbeans.XmlObject addNewOrderParams();
    
    /**
     * An XML OrderType(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of StaticHeaderOrderDetailsType$OrderType.
     */
    public interface OrderType extends OrderTBaseType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(OrderType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("ordertype9acfelemtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static StaticHeaderOrderDetailsType.OrderType newInstance() {
              return (StaticHeaderOrderDetailsType.OrderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static StaticHeaderOrderDetailsType.OrderType newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (StaticHeaderOrderDetailsType.OrderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static StaticHeaderOrderDetailsType newInstance() {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static StaticHeaderOrderDetailsType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static StaticHeaderOrderDetailsType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static StaticHeaderOrderDetailsType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static StaticHeaderOrderDetailsType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static StaticHeaderOrderDetailsType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static StaticHeaderOrderDetailsType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static StaticHeaderOrderDetailsType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static StaticHeaderOrderDetailsType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static StaticHeaderOrderDetailsType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static StaticHeaderOrderDetailsType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static StaticHeaderOrderDetailsType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static StaticHeaderOrderDetailsType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static StaticHeaderOrderDetailsType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static StaticHeaderOrderDetailsType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static StaticHeaderOrderDetailsType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static StaticHeaderOrderDetailsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static StaticHeaderOrderDetailsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (StaticHeaderOrderDetailsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
