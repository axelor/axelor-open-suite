/*
 * XML Type:  FDLOrderParamsType
 * Namespace: http://www.ebics.org/H003
 * Java type: FDLOrderParamsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML FDLOrderParamsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface FDLOrderParamsType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(FDLOrderParamsType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("fdlorderparamstypef01atype");
    
    /**
     * Gets the "DateRange" element
     */
    FDLOrderParamsType.DateRange getDateRange();
    
    /**
     * True if has "DateRange" element
     */
    boolean isSetDateRange();
    
    /**
     * Sets the "DateRange" element
     */
    void setDateRange(FDLOrderParamsType.DateRange dateRange);
    
    /**
     * Appends and returns a new empty "DateRange" element
     */
    FDLOrderParamsType.DateRange addNewDateRange();
    
    /**
     * Unsets the "DateRange" element
     */
    void unsetDateRange();
    
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
     * Gets the "FileFormat" element
     */
    FileFormatType getFileFormat();
    
    /**
     * Sets the "FileFormat" element
     */
    void setFileFormat(FileFormatType fileFormat);
    
    /**
     * Appends and returns a new empty "FileFormat" element
     */
    FileFormatType addNewFileFormat();
    
    /**
     * An XML DateRange(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface DateRange extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(DateRange.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("daterange7e3felemtype");
        
        /**
         * Gets the "Start" element
         */
        java.util.Calendar getStart();
        
        /**
         * Gets (as xml) the "Start" element
         */
        DateType xgetStart();
        
        /**
         * Sets the "Start" element
         */
        void setStart(java.util.Calendar start);
        
        /**
         * Sets (as xml) the "Start" element
         */
        void xsetStart(DateType start);
        
        /**
         * Gets the "End" element
         */
        java.util.Calendar getEnd();
        
        /**
         * Gets (as xml) the "End" element
         */
        DateType xgetEnd();
        
        /**
         * Sets the "End" element
         */
        void setEnd(java.util.Calendar end);
        
        /**
         * Sets (as xml) the "End" element
         */
        void xsetEnd(DateType end);
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static FDLOrderParamsType.DateRange newInstance() {
              return (FDLOrderParamsType.DateRange) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static FDLOrderParamsType.DateRange newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (FDLOrderParamsType.DateRange) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static FDLOrderParamsType newInstance() {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static FDLOrderParamsType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static FDLOrderParamsType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static FDLOrderParamsType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static FDLOrderParamsType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static FDLOrderParamsType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static FDLOrderParamsType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static FDLOrderParamsType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static FDLOrderParamsType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static FDLOrderParamsType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static FDLOrderParamsType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static FDLOrderParamsType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static FDLOrderParamsType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static FDLOrderParamsType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static FDLOrderParamsType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static FDLOrderParamsType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static FDLOrderParamsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static FDLOrderParamsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (FDLOrderParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
