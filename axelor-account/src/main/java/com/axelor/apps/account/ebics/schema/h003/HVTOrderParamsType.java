/*
 * XML Type:  HVTOrderParamsType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVTOrderParamsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML HVTOrderParamsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface HVTOrderParamsType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(HVTOrderParamsType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("hvtorderparamstype3562type");
    
    /**
     * Gets the "PartnerID" element
     */
    java.lang.String getPartnerID();
    
    /**
     * Gets (as xml) the "PartnerID" element
     */
    PartnerIDType xgetPartnerID();
    
    /**
     * Sets the "PartnerID" element
     */
    void setPartnerID(java.lang.String partnerID);
    
    /**
     * Sets (as xml) the "PartnerID" element
     */
    void xsetPartnerID(PartnerIDType partnerID);
    
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
     * Gets the "OrderFlags" element
     */
    HVTOrderParamsType.OrderFlags getOrderFlags();
    
    /**
     * Sets the "OrderFlags" element
     */
    void setOrderFlags(HVTOrderParamsType.OrderFlags orderFlags);
    
    /**
     * Appends and returns a new empty "OrderFlags" element
     */
    HVTOrderParamsType.OrderFlags addNewOrderFlags();
    
    /**
     * Gets array of all "Parameter" elements
     */
    ParameterDocument.Parameter[] getParameterArray();
    
    /**
     * Gets ith "Parameter" element
     */
    ParameterDocument.Parameter getParameterArray(int i);
    
    /**
     * Returns number of "Parameter" element
     */
    int sizeOfParameterArray();
    
    /**
     * Sets array of all "Parameter" element
     */
    void setParameterArray(ParameterDocument.Parameter[] parameterArray);
    
    /**
     * Sets ith "Parameter" element
     */
    void setParameterArray(int i, ParameterDocument.Parameter parameter);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "Parameter" element
     */
    ParameterDocument.Parameter insertNewParameter(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "Parameter" element
     */
    ParameterDocument.Parameter addNewParameter();
    
    /**
     * Removes the ith "Parameter" element
     */
    void removeParameter(int i);
    
    /**
     * An XML OrderFlags(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface OrderFlags extends HVTOrderFlagsType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(OrderFlags.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("orderflagsce2delemtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HVTOrderParamsType.OrderFlags newInstance() {
              return (HVTOrderParamsType.OrderFlags) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HVTOrderParamsType.OrderFlags newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HVTOrderParamsType.OrderFlags) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static HVTOrderParamsType newInstance() {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static HVTOrderParamsType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static HVTOrderParamsType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static HVTOrderParamsType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static HVTOrderParamsType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static HVTOrderParamsType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static HVTOrderParamsType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static HVTOrderParamsType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static HVTOrderParamsType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static HVTOrderParamsType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static HVTOrderParamsType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static HVTOrderParamsType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static HVTOrderParamsType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static HVTOrderParamsType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static HVTOrderParamsType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static HVTOrderParamsType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HVTOrderParamsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HVTOrderParamsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HVTOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
