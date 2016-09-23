/*
 * XML Type:  HVTOrderFlagsType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVTOrderFlagsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML HVTOrderFlagsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface HVTOrderFlagsType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(HVTOrderFlagsType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("hvtorderflagstype4aa1type");
    
    /**
     * Gets the "completeOrderData" attribute
     */
    boolean getCompleteOrderData();
    
    /**
     * Gets (as xml) the "completeOrderData" attribute
     */
    org.apache.xmlbeans.XmlBoolean xgetCompleteOrderData();
    
    /**
     * True if has "completeOrderData" attribute
     */
    boolean isSetCompleteOrderData();
    
    /**
     * Sets the "completeOrderData" attribute
     */
    void setCompleteOrderData(boolean completeOrderData);
    
    /**
     * Sets (as xml) the "completeOrderData" attribute
     */
    void xsetCompleteOrderData(org.apache.xmlbeans.XmlBoolean completeOrderData);
    
    /**
     * Unsets the "completeOrderData" attribute
     */
    void unsetCompleteOrderData();
    
    /**
     * Gets the "fetchLimit" attribute
     */
    long getFetchLimit();
    
    /**
     * Gets (as xml) the "fetchLimit" attribute
     */
    HVTOrderFlagsType.FetchLimit xgetFetchLimit();
    
    /**
     * True if has "fetchLimit" attribute
     */
    boolean isSetFetchLimit();
    
    /**
     * Sets the "fetchLimit" attribute
     */
    void setFetchLimit(long fetchLimit);
    
    /**
     * Sets (as xml) the "fetchLimit" attribute
     */
    void xsetFetchLimit(HVTOrderFlagsType.FetchLimit fetchLimit);
    
    /**
     * Unsets the "fetchLimit" attribute
     */
    void unsetFetchLimit();
    
    /**
     * Gets the "fetchOffset" attribute
     */
    long getFetchOffset();
    
    /**
     * Gets (as xml) the "fetchOffset" attribute
     */
    HVTOrderFlagsType.FetchOffset xgetFetchOffset();
    
    /**
     * True if has "fetchOffset" attribute
     */
    boolean isSetFetchOffset();
    
    /**
     * Sets the "fetchOffset" attribute
     */
    void setFetchOffset(long fetchOffset);
    
    /**
     * Sets (as xml) the "fetchOffset" attribute
     */
    void xsetFetchOffset(HVTOrderFlagsType.FetchOffset fetchOffset);
    
    /**
     * Unsets the "fetchOffset" attribute
     */
    void unsetFetchOffset();
    
    /**
     * An XML fetchLimit(@).
     *
     * This is an atomic type that is a restriction of HVTOrderFlagsType$FetchLimit.
     */
    public interface FetchLimit extends org.apache.xmlbeans.XmlNonNegativeInteger
    {
        long getLongValue();
        void setLongValue(long l);
        /** @deprecated */
        long longValue();
        /** @deprecated */
        void set(long l);
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(FetchLimit.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("fetchlimit8f22attrtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HVTOrderFlagsType.FetchLimit newValue(java.lang.Object obj) {
              return (HVTOrderFlagsType.FetchLimit) type.newValue( obj ); }
            
            public static HVTOrderFlagsType.FetchLimit newInstance() {
              return (HVTOrderFlagsType.FetchLimit) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HVTOrderFlagsType.FetchLimit newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HVTOrderFlagsType.FetchLimit) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML fetchOffset(@).
     *
     * This is an atomic type that is a restriction of HVTOrderFlagsType$FetchOffset.
     */
    public interface FetchOffset extends org.apache.xmlbeans.XmlNonNegativeInteger
    {
        long getLongValue();
        void setLongValue(long l);
        /** @deprecated */
        long longValue();
        /** @deprecated */
        void set(long l);
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(FetchOffset.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("fetchoffset7036attrtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HVTOrderFlagsType.FetchOffset newValue(java.lang.Object obj) {
              return (HVTOrderFlagsType.FetchOffset) type.newValue( obj ); }
            
            public static HVTOrderFlagsType.FetchOffset newInstance() {
              return (HVTOrderFlagsType.FetchOffset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HVTOrderFlagsType.FetchOffset newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HVTOrderFlagsType.FetchOffset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static HVTOrderFlagsType newInstance() {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static HVTOrderFlagsType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static HVTOrderFlagsType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static HVTOrderFlagsType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static HVTOrderFlagsType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static HVTOrderFlagsType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static HVTOrderFlagsType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static HVTOrderFlagsType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static HVTOrderFlagsType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static HVTOrderFlagsType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static HVTOrderFlagsType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static HVTOrderFlagsType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static HVTOrderFlagsType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static HVTOrderFlagsType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static HVTOrderFlagsType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static HVTOrderFlagsType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HVTOrderFlagsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HVTOrderFlagsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HVTOrderFlagsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
