/*
 * XML Type:  HPDProtocolParamsType
 * Namespace: http://www.ebics.org/H003
 * Java type: HPDProtocolParamsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML HPDProtocolParamsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface HPDProtocolParamsType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(HPDProtocolParamsType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("hpdprotocolparamstypec5fctype");
    
    /**
     * Gets the "Version" element
     */
    HPDVersionType getVersion();
    
    /**
     * Sets the "Version" element
     */
    void setVersion(HPDVersionType version);
    
    /**
     * Appends and returns a new empty "Version" element
     */
    HPDVersionType addNewVersion();
    
    /**
     * Gets the "Recovery" element
     */
    HPDProtocolParamsType.Recovery getRecovery();
    
    /**
     * True if has "Recovery" element
     */
    boolean isSetRecovery();
    
    /**
     * Sets the "Recovery" element
     */
    void setRecovery(HPDProtocolParamsType.Recovery recovery);
    
    /**
     * Appends and returns a new empty "Recovery" element
     */
    HPDProtocolParamsType.Recovery addNewRecovery();
    
    /**
     * Unsets the "Recovery" element
     */
    void unsetRecovery();
    
    /**
     * Gets the "PreValidation" element
     */
    HPDProtocolParamsType.PreValidation getPreValidation();
    
    /**
     * True if has "PreValidation" element
     */
    boolean isSetPreValidation();
    
    /**
     * Sets the "PreValidation" element
     */
    void setPreValidation(HPDProtocolParamsType.PreValidation preValidation);
    
    /**
     * Appends and returns a new empty "PreValidation" element
     */
    HPDProtocolParamsType.PreValidation addNewPreValidation();
    
    /**
     * Unsets the "PreValidation" element
     */
    void unsetPreValidation();
    
    /**
     * Gets the "X509Data" element
     */
    HPDProtocolParamsType.X509Data getX509Data();
    
    /**
     * True if has "X509Data" element
     */
    boolean isSetX509Data();
    
    /**
     * Sets the "X509Data" element
     */
    void setX509Data(HPDProtocolParamsType.X509Data x509Data);
    
    /**
     * Appends and returns a new empty "X509Data" element
     */
    HPDProtocolParamsType.X509Data addNewX509Data();
    
    /**
     * Unsets the "X509Data" element
     */
    void unsetX509Data();
    
    /**
     * Gets the "ClientDataDownload" element
     */
    HPDProtocolParamsType.ClientDataDownload getClientDataDownload();
    
    /**
     * True if has "ClientDataDownload" element
     */
    boolean isSetClientDataDownload();
    
    /**
     * Sets the "ClientDataDownload" element
     */
    void setClientDataDownload(HPDProtocolParamsType.ClientDataDownload clientDataDownload);
    
    /**
     * Appends and returns a new empty "ClientDataDownload" element
     */
    HPDProtocolParamsType.ClientDataDownload addNewClientDataDownload();
    
    /**
     * Unsets the "ClientDataDownload" element
     */
    void unsetClientDataDownload();
    
    /**
     * Gets the "DownloadableOrderData" element
     */
    HPDProtocolParamsType.DownloadableOrderData getDownloadableOrderData();
    
    /**
     * True if has "DownloadableOrderData" element
     */
    boolean isSetDownloadableOrderData();
    
    /**
     * Sets the "DownloadableOrderData" element
     */
    void setDownloadableOrderData(HPDProtocolParamsType.DownloadableOrderData downloadableOrderData);
    
    /**
     * Appends and returns a new empty "DownloadableOrderData" element
     */
    HPDProtocolParamsType.DownloadableOrderData addNewDownloadableOrderData();
    
    /**
     * Unsets the "DownloadableOrderData" element
     */
    void unsetDownloadableOrderData();
    
    /**
     * An XML Recovery(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface Recovery extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Recovery.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("recovery040delemtype");
        
        /**
         * Gets the "supported" attribute
         */
        boolean getSupported();
        
        /**
         * Gets (as xml) the "supported" attribute
         */
        org.apache.xmlbeans.XmlBoolean xgetSupported();
        
        /**
         * True if has "supported" attribute
         */
        boolean isSetSupported();
        
        /**
         * Sets the "supported" attribute
         */
        void setSupported(boolean supported);
        
        /**
         * Sets (as xml) the "supported" attribute
         */
        void xsetSupported(org.apache.xmlbeans.XmlBoolean supported);
        
        /**
         * Unsets the "supported" attribute
         */
        void unsetSupported();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HPDProtocolParamsType.Recovery newInstance() {
              return (HPDProtocolParamsType.Recovery) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HPDProtocolParamsType.Recovery newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HPDProtocolParamsType.Recovery) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML PreValidation(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface PreValidation extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(PreValidation.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("prevalidationc9c4elemtype");
        
        /**
         * Gets the "supported" attribute
         */
        boolean getSupported();
        
        /**
         * Gets (as xml) the "supported" attribute
         */
        org.apache.xmlbeans.XmlBoolean xgetSupported();
        
        /**
         * True if has "supported" attribute
         */
        boolean isSetSupported();
        
        /**
         * Sets the "supported" attribute
         */
        void setSupported(boolean supported);
        
        /**
         * Sets (as xml) the "supported" attribute
         */
        void xsetSupported(org.apache.xmlbeans.XmlBoolean supported);
        
        /**
         * Unsets the "supported" attribute
         */
        void unsetSupported();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HPDProtocolParamsType.PreValidation newInstance() {
              return (HPDProtocolParamsType.PreValidation) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HPDProtocolParamsType.PreValidation newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HPDProtocolParamsType.PreValidation) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML X509Data(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface X509Data extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(X509Data.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("x509data4a08elemtype");
        
        /**
         * Gets the "supported" attribute
         */
        boolean getSupported();
        
        /**
         * Gets (as xml) the "supported" attribute
         */
        org.apache.xmlbeans.XmlBoolean xgetSupported();
        
        /**
         * True if has "supported" attribute
         */
        boolean isSetSupported();
        
        /**
         * Sets the "supported" attribute
         */
        void setSupported(boolean supported);
        
        /**
         * Sets (as xml) the "supported" attribute
         */
        void xsetSupported(org.apache.xmlbeans.XmlBoolean supported);
        
        /**
         * Unsets the "supported" attribute
         */
        void unsetSupported();
        
        /**
         * Gets the "persistent" attribute
         */
        boolean getPersistent();
        
        /**
         * Gets (as xml) the "persistent" attribute
         */
        org.apache.xmlbeans.XmlBoolean xgetPersistent();
        
        /**
         * True if has "persistent" attribute
         */
        boolean isSetPersistent();
        
        /**
         * Sets the "persistent" attribute
         */
        void setPersistent(boolean persistent);
        
        /**
         * Sets (as xml) the "persistent" attribute
         */
        void xsetPersistent(org.apache.xmlbeans.XmlBoolean persistent);
        
        /**
         * Unsets the "persistent" attribute
         */
        void unsetPersistent();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HPDProtocolParamsType.X509Data newInstance() {
              return (HPDProtocolParamsType.X509Data) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HPDProtocolParamsType.X509Data newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HPDProtocolParamsType.X509Data) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML ClientDataDownload(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface ClientDataDownload extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(ClientDataDownload.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("clientdatadownload6635elemtype");
        
        /**
         * Gets the "supported" attribute
         */
        boolean getSupported();
        
        /**
         * Gets (as xml) the "supported" attribute
         */
        org.apache.xmlbeans.XmlBoolean xgetSupported();
        
        /**
         * True if has "supported" attribute
         */
        boolean isSetSupported();
        
        /**
         * Sets the "supported" attribute
         */
        void setSupported(boolean supported);
        
        /**
         * Sets (as xml) the "supported" attribute
         */
        void xsetSupported(org.apache.xmlbeans.XmlBoolean supported);
        
        /**
         * Unsets the "supported" attribute
         */
        void unsetSupported();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HPDProtocolParamsType.ClientDataDownload newInstance() {
              return (HPDProtocolParamsType.ClientDataDownload) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HPDProtocolParamsType.ClientDataDownload newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HPDProtocolParamsType.ClientDataDownload) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML DownloadableOrderData(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface DownloadableOrderData extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(DownloadableOrderData.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("downloadableorderdata081eelemtype");
        
        /**
         * Gets the "supported" attribute
         */
        boolean getSupported();
        
        /**
         * Gets (as xml) the "supported" attribute
         */
        org.apache.xmlbeans.XmlBoolean xgetSupported();
        
        /**
         * True if has "supported" attribute
         */
        boolean isSetSupported();
        
        /**
         * Sets the "supported" attribute
         */
        void setSupported(boolean supported);
        
        /**
         * Sets (as xml) the "supported" attribute
         */
        void xsetSupported(org.apache.xmlbeans.XmlBoolean supported);
        
        /**
         * Unsets the "supported" attribute
         */
        void unsetSupported();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HPDProtocolParamsType.DownloadableOrderData newInstance() {
              return (HPDProtocolParamsType.DownloadableOrderData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HPDProtocolParamsType.DownloadableOrderData newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HPDProtocolParamsType.DownloadableOrderData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static HPDProtocolParamsType newInstance() {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static HPDProtocolParamsType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static HPDProtocolParamsType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static HPDProtocolParamsType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static HPDProtocolParamsType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static HPDProtocolParamsType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static HPDProtocolParamsType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static HPDProtocolParamsType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static HPDProtocolParamsType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static HPDProtocolParamsType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static HPDProtocolParamsType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static HPDProtocolParamsType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static HPDProtocolParamsType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static HPDProtocolParamsType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static HPDProtocolParamsType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static HPDProtocolParamsType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HPDProtocolParamsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HPDProtocolParamsType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HPDProtocolParamsType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
