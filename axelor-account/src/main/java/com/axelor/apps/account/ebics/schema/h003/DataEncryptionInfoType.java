/*
 * XML Type:  DataEncryptionInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: DataEncryptionInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML DataEncryptionInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface DataEncryptionInfoType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(DataEncryptionInfoType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("dataencryptioninfotyped7cftype");
    
    /**
     * Gets the "EncryptionPubKeyDigest" element
     */
    DataEncryptionInfoType.EncryptionPubKeyDigest getEncryptionPubKeyDigest();
    
    /**
     * Sets the "EncryptionPubKeyDigest" element
     */
    void setEncryptionPubKeyDigest(DataEncryptionInfoType.EncryptionPubKeyDigest encryptionPubKeyDigest);
    
    /**
     * Appends and returns a new empty "EncryptionPubKeyDigest" element
     */
    DataEncryptionInfoType.EncryptionPubKeyDigest addNewEncryptionPubKeyDigest();
    
    /**
     * Gets the "TransactionKey" element
     */
    byte[] getTransactionKey();
    
    /**
     * Gets (as xml) the "TransactionKey" element
     */
    SymmetricKeyType xgetTransactionKey();
    
    /**
     * Sets the "TransactionKey" element
     */
    void setTransactionKey(byte[] transactionKey);
    
    /**
     * Sets (as xml) the "TransactionKey" element
     */
    void xsetTransactionKey(SymmetricKeyType transactionKey);
    
    /**
     * An XML EncryptionPubKeyDigest(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of DataEncryptionInfoType$EncryptionPubKeyDigest.
     */
    public interface EncryptionPubKeyDigest extends PubKeyDigestType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EncryptionPubKeyDigest.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("encryptionpubkeydigestb74aelemtype");
        
        /**
         * Gets the "Version" attribute
         */
        java.lang.String getVersion();
        
        /**
         * Gets (as xml) the "Version" attribute
         */
        EncryptionVersionType xgetVersion();
        
        /**
         * Sets the "Version" attribute
         */
        void setVersion(java.lang.String version);
        
        /**
         * Sets (as xml) the "Version" attribute
         */
        void xsetVersion(EncryptionVersionType version);
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static DataEncryptionInfoType.EncryptionPubKeyDigest newInstance() {
              return (DataEncryptionInfoType.EncryptionPubKeyDigest) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static DataEncryptionInfoType.EncryptionPubKeyDigest newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (DataEncryptionInfoType.EncryptionPubKeyDigest) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static DataEncryptionInfoType newInstance() {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static DataEncryptionInfoType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static DataEncryptionInfoType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static DataEncryptionInfoType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static DataEncryptionInfoType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static DataEncryptionInfoType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static DataEncryptionInfoType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static DataEncryptionInfoType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static DataEncryptionInfoType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static DataEncryptionInfoType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static DataEncryptionInfoType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static DataEncryptionInfoType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static DataEncryptionInfoType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static DataEncryptionInfoType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static DataEncryptionInfoType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static DataEncryptionInfoType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static DataEncryptionInfoType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static DataEncryptionInfoType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (DataEncryptionInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
