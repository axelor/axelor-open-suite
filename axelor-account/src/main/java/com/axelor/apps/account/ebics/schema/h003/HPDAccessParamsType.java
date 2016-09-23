/*
 * XML Type:  HPDAccessParamsType
 * Namespace: http://www.ebics.org/H003
 * Java type: HPDAccessParamsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML HPDAccessParamsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface HPDAccessParamsType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(HPDAccessParamsType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("hpdaccessparamstype8f88type");
    
    /**
     * Gets array of all "URL" elements
     */
    HPDAccessParamsType.URL[] getURLArray();
    
    /**
     * Gets ith "URL" element
     */
    HPDAccessParamsType.URL getURLArray(int i);
    
    /**
     * Returns number of "URL" element
     */
    int sizeOfURLArray();
    
    /**
     * Sets array of all "URL" element
     */
    void setURLArray(HPDAccessParamsType.URL[] urlArray);
    
    /**
     * Sets ith "URL" element
     */
    void setURLArray(int i, HPDAccessParamsType.URL url);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "URL" element
     */
    HPDAccessParamsType.URL insertNewURL(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "URL" element
     */
    HPDAccessParamsType.URL addNewURL();
    
    /**
     * Removes the ith "URL" element
     */
    void removeURL(int i);
    
    /**
     * Gets the "Institute" element
     */
    java.lang.String getInstitute();
    
    /**
     * Gets (as xml) the "Institute" element
     */
    HPDAccessParamsType.Institute xgetInstitute();
    
    /**
     * Sets the "Institute" element
     */
    void setInstitute(java.lang.String institute);
    
    /**
     * Sets (as xml) the "Institute" element
     */
    void xsetInstitute(HPDAccessParamsType.Institute institute);
    
    /**
     * Gets the "HostID" element
     */
    java.lang.String getHostID();
    
    /**
     * Gets (as xml) the "HostID" element
     */
    HostIDType xgetHostID();
    
    /**
     * True if has "HostID" element
     */
    boolean isSetHostID();
    
    /**
     * Sets the "HostID" element
     */
    void setHostID(java.lang.String hostID);
    
    /**
     * Sets (as xml) the "HostID" element
     */
    void xsetHostID(HostIDType hostID);
    
    /**
     * Unsets the "HostID" element
     */
    void unsetHostID();
    
    /**
     * An XML URL(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of HPDAccessParamsType$URL.
     */
    public interface URL extends org.apache.xmlbeans.XmlAnyURI
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(URL.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("url6743elemtype");
        
        /**
         * Gets the "valid_from" attribute
         */
        java.util.Calendar getValidFrom();
        
        /**
         * Gets (as xml) the "valid_from" attribute
         */
        TimestampType xgetValidFrom();
        
        /**
         * True if has "valid_from" attribute
         */
        boolean isSetValidFrom();
        
        /**
         * Sets the "valid_from" attribute
         */
        void setValidFrom(java.util.Calendar validFrom);
        
        /**
         * Sets (as xml) the "valid_from" attribute
         */
        void xsetValidFrom(TimestampType validFrom);
        
        /**
         * Unsets the "valid_from" attribute
         */
        void unsetValidFrom();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HPDAccessParamsType.URL newInstance() {
              return (HPDAccessParamsType.URL) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HPDAccessParamsType.URL newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HPDAccessParamsType.URL) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML Institute(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of HPDAccessParamsType$Institute.
     */
    public interface Institute extends org.apache.xmlbeans.XmlNormalizedString
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Institute.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("institutee5e9elemtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HPDAccessParamsType.Institute newValue(java.lang.Object obj) {
              return (HPDAccessParamsType.Institute) type.newValue( obj ); }
            
            public static HPDAccessParamsType.Institute newInstance() {
              return (HPDAccessParamsType.Institute) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HPDAccessParamsType.Institute newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HPDAccessParamsType.Institute) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static HPDAccessParamsType newInstance() {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static HPDAccessParamsType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static HPDAccessParamsType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static HPDAccessParamsType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static HPDAccessParamsType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static HPDAccessParamsType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static HPDAccessParamsType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static HPDAccessParamsType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static HPDAccessParamsType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static HPDAccessParamsType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static HPDAccessParamsType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static HPDAccessParamsType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static HPDAccessParamsType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static HPDAccessParamsType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static HPDAccessParamsType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static HPDAccessParamsType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HPDAccessParamsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HPDAccessParamsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HPDAccessParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
