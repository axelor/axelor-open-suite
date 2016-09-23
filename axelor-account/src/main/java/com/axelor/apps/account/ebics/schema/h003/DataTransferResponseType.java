/*
 * XML Type:  DataTransferResponseType
 * Namespace: http://www.ebics.org/H003
 * Java type: DataTransferResponseType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML DataTransferResponseType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface DataTransferResponseType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(DataTransferResponseType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("datatransferresponsetype2c4atype");
    
    /**
     * Gets the "DataEncryptionInfo" element
     */
    DataTransferResponseType.DataEncryptionInfo getDataEncryptionInfo();
    
    /**
     * True if has "DataEncryptionInfo" element
     */
    boolean isSetDataEncryptionInfo();
    
    /**
     * Sets the "DataEncryptionInfo" element
     */
    void setDataEncryptionInfo(DataTransferResponseType.DataEncryptionInfo dataEncryptionInfo);
    
    /**
     * Appends and returns a new empty "DataEncryptionInfo" element
     */
    DataTransferResponseType.DataEncryptionInfo addNewDataEncryptionInfo();
    
    /**
     * Unsets the "DataEncryptionInfo" element
     */
    void unsetDataEncryptionInfo();
    
    /**
     * Gets the "OrderData" element
     */
    DataTransferResponseType.OrderData getOrderData();
    
    /**
     * Sets the "OrderData" element
     */
    void setOrderData(DataTransferResponseType.OrderData orderData);
    
    /**
     * Appends and returns a new empty "OrderData" element
     */
    DataTransferResponseType.OrderData addNewOrderData();
    
    /**
     * An XML DataEncryptionInfo(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface DataEncryptionInfo extends DataEncryptionInfoType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(DataEncryptionInfo.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("dataencryptioninfo9cd3elemtype");
        
        /**
         * Gets the "authenticate" attribute
         */
        boolean getAuthenticate();
        
        /**
         * Gets (as xml) the "authenticate" attribute
         */
        org.apache.xmlbeans.XmlBoolean xgetAuthenticate();
        
        /**
         * Sets the "authenticate" attribute
         */
        void setAuthenticate(boolean authenticate);
        
        /**
         * Sets (as xml) the "authenticate" attribute
         */
        void xsetAuthenticate(org.apache.xmlbeans.XmlBoolean authenticate);
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static DataTransferResponseType.DataEncryptionInfo newInstance() {
              return (DataTransferResponseType.DataEncryptionInfo) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static DataTransferResponseType.DataEncryptionInfo newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (DataTransferResponseType.DataEncryptionInfo) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML OrderData(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of DataTransferResponseType$OrderData.
     */
    public interface OrderData extends OrderDataType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(OrderData.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("orderdata8c66elemtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static DataTransferResponseType.OrderData newInstance() {
              return (DataTransferResponseType.OrderData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static DataTransferResponseType.OrderData newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (DataTransferResponseType.OrderData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static DataTransferResponseType newInstance() {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static DataTransferResponseType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static DataTransferResponseType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static DataTransferResponseType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static DataTransferResponseType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static DataTransferResponseType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static DataTransferResponseType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static DataTransferResponseType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static DataTransferResponseType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static DataTransferResponseType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static DataTransferResponseType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static DataTransferResponseType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static DataTransferResponseType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static DataTransferResponseType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static DataTransferResponseType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static DataTransferResponseType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static DataTransferResponseType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static DataTransferResponseType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (DataTransferResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
