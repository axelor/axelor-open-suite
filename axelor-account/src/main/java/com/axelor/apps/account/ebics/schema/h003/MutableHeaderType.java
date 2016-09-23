/*
 * XML Type:  MutableHeaderType
 * Namespace: http://www.ebics.org/H003
 * Java type: MutableHeaderType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML MutableHeaderType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface MutableHeaderType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(MutableHeaderType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("mutableheadertype1b55type");
    
    /**
     * Gets the "TransactionPhase" element
     */
    TransactionPhaseType.Enum getTransactionPhase();
    
    /**
     * Gets (as xml) the "TransactionPhase" element
     */
    TransactionPhaseType xgetTransactionPhase();
    
    /**
     * Sets the "TransactionPhase" element
     */
    void setTransactionPhase(TransactionPhaseType.Enum transactionPhase);
    
    /**
     * Sets (as xml) the "TransactionPhase" element
     */
    void xsetTransactionPhase(TransactionPhaseType transactionPhase);
    
    /**
     * Gets the "SegmentNumber" element
     */
    MutableHeaderType.SegmentNumber getSegmentNumber();
    
    /**
     * Tests for nil "SegmentNumber" element
     */
    boolean isNilSegmentNumber();
    
    /**
     * True if has "SegmentNumber" element
     */
    boolean isSetSegmentNumber();
    
    /**
     * Sets the "SegmentNumber" element
     */
    void setSegmentNumber(MutableHeaderType.SegmentNumber segmentNumber);
    
    /**
     * Appends and returns a new empty "SegmentNumber" element
     */
    MutableHeaderType.SegmentNumber addNewSegmentNumber();
    
    /**
     * Nils the "SegmentNumber" element
     */
    void setNilSegmentNumber();
    
    /**
     * Unsets the "SegmentNumber" element
     */
    void unsetSegmentNumber();
    
    /**
     * An XML SegmentNumber(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of MutableHeaderType$SegmentNumber.
     */
    public interface SegmentNumber extends SegmentNumberType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(SegmentNumber.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("segmentnumber87fdelemtype");
        
        /**
         * Gets the "lastSegment" attribute
         */
        boolean getLastSegment();
        
        /**
         * Gets (as xml) the "lastSegment" attribute
         */
        org.apache.xmlbeans.XmlBoolean xgetLastSegment();
        
        /**
         * True if has "lastSegment" attribute
         */
        boolean isSetLastSegment();
        
        /**
         * Sets the "lastSegment" attribute
         */
        void setLastSegment(boolean lastSegment);
        
        /**
         * Sets (as xml) the "lastSegment" attribute
         */
        void xsetLastSegment(org.apache.xmlbeans.XmlBoolean lastSegment);
        
        /**
         * Unsets the "lastSegment" attribute
         */
        void unsetLastSegment();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static MutableHeaderType.SegmentNumber newInstance() {
              return (MutableHeaderType.SegmentNumber) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static MutableHeaderType.SegmentNumber newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (MutableHeaderType.SegmentNumber) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static MutableHeaderType newInstance() {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static MutableHeaderType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static MutableHeaderType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static MutableHeaderType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static MutableHeaderType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static MutableHeaderType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static MutableHeaderType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static MutableHeaderType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static MutableHeaderType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static MutableHeaderType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static MutableHeaderType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static MutableHeaderType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static MutableHeaderType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static MutableHeaderType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static MutableHeaderType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static MutableHeaderType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static MutableHeaderType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static MutableHeaderType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (MutableHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
