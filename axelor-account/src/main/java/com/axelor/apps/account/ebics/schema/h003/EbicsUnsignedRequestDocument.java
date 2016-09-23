/*
 * An XML document type.
 * Localname: ebicsUnsignedRequest
 * Namespace: http://www.ebics.org/H003
 * Java type: EbicsUnsignedRequestDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * A document containing one ebicsUnsignedRequest(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public interface EbicsUnsignedRequestDocument extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EbicsUnsignedRequestDocument.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("ebicsunsignedrequest3528doctype");
    
    /**
     * Gets the "ebicsUnsignedRequest" element
     */
    EbicsUnsignedRequestDocument.EbicsUnsignedRequest getEbicsUnsignedRequest();
    
    /**
     * Sets the "ebicsUnsignedRequest" element
     */
    void setEbicsUnsignedRequest(EbicsUnsignedRequestDocument.EbicsUnsignedRequest ebicsUnsignedRequest);
    
    /**
     * Appends and returns a new empty "ebicsUnsignedRequest" element
     */
    EbicsUnsignedRequestDocument.EbicsUnsignedRequest addNewEbicsUnsignedRequest();
    
    /**
     * An XML ebicsUnsignedRequest(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface EbicsUnsignedRequest extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EbicsUnsignedRequest.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("ebicsunsignedrequestf26eelemtype");
        
        /**
         * Gets the "header" element
         */
        EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Header getHeader();
        
        /**
         * Sets the "header" element
         */
        void setHeader(EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Header header);
        
        /**
         * Appends and returns a new empty "header" element
         */
        EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Header addNewHeader();
        
        /**
         * Gets the "body" element
         */
        EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body getBody();
        
        /**
         * Sets the "body" element
         */
        void setBody(EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body body);
        
        /**
         * Appends and returns a new empty "body" element
         */
        EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body addNewBody();
        
        /**
         * Gets the "Version" attribute
         */
        java.lang.String getVersion();
        
        /**
         * Gets (as xml) the "Version" attribute
         */
        ProtocolVersionType xgetVersion();
        
        /**
         * Sets the "Version" attribute
         */
        void setVersion(java.lang.String version);
        
        /**
         * Sets (as xml) the "Version" attribute
         */
        void xsetVersion(ProtocolVersionType version);
        
        /**
         * Gets the "Revision" attribute
         */
        int getRevision();
        
        /**
         * Gets (as xml) the "Revision" attribute
         */
        ProtocolRevisionType xgetRevision();
        
        /**
         * True if has "Revision" attribute
         */
        boolean isSetRevision();
        
        /**
         * Sets the "Revision" attribute
         */
        void setRevision(int revision);
        
        /**
         * Sets (as xml) the "Revision" attribute
         */
        void xsetRevision(ProtocolRevisionType revision);
        
        /**
         * Unsets the "Revision" attribute
         */
        void unsetRevision();
        
        /**
         * An XML header(@http://www.ebics.org/H003).
         *
         * This is a complex type.
         */
        public interface Header extends org.apache.xmlbeans.XmlObject
        {
            public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
                org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Header.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("header0a97elemtype");
            
            /**
             * Gets the "static" element
             */
            UnsignedRequestStaticHeaderType getStatic();
            
            /**
             * Sets the "static" element
             */
            void setStatic(UnsignedRequestStaticHeaderType xstatic);
            
            /**
             * Appends and returns a new empty "static" element
             */
            UnsignedRequestStaticHeaderType addNewStatic();
            
            /**
             * Gets the "mutable" element
             */
            EmptyMutableHeaderType getMutable();
            
            /**
             * Sets the "mutable" element
             */
            void setMutable(EmptyMutableHeaderType mutable);
            
            /**
             * Appends and returns a new empty "mutable" element
             */
            EmptyMutableHeaderType addNewMutable();
            
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
                public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Header newInstance() {
                  return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Header) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                
                public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Header newInstance(org.apache.xmlbeans.XmlOptions options) {
                  return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Header) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                
                private Factory() { } // No instance of this class allowed
            }
        }
        
        /**
         * An XML body(@http://www.ebics.org/H003).
         *
         * This is a complex type.
         */
        public interface Body extends org.apache.xmlbeans.XmlObject
        {
            public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
                org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Body.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("body1aacelemtype");
            
            /**
             * Gets the "DataTransfer" element
             */
            EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer getDataTransfer();
            
            /**
             * Sets the "DataTransfer" element
             */
            void setDataTransfer(EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer dataTransfer);
            
            /**
             * Appends and returns a new empty "DataTransfer" element
             */
            EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer addNewDataTransfer();
            
            /**
             * An XML DataTransfer(@http://www.ebics.org/H003).
             *
             * This is a complex type.
             */
            public interface DataTransfer extends org.apache.xmlbeans.XmlObject
            {
                public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
                    org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(DataTransfer.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("datatransferd81belemtype");
                
                /**
                 * Gets the "SignatureData" element
                 */
                EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.SignatureData getSignatureData();
                
                /**
                 * Sets the "SignatureData" element
                 */
                void setSignatureData(EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.SignatureData signatureData);
                
                /**
                 * Appends and returns a new empty "SignatureData" element
                 */
                EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.SignatureData addNewSignatureData();
                
                /**
                 * Gets the "OrderData" element
                 */
                EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.OrderData getOrderData();
                
                /**
                 * Sets the "OrderData" element
                 */
                void setOrderData(EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.OrderData orderData);
                
                /**
                 * Appends and returns a new empty "OrderData" element
                 */
                EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.OrderData addNewOrderData();
                
                /**
                 * An XML SignatureData(@http://www.ebics.org/H003).
                 *
                 * This is an atomic type that is a restriction of EbicsUnsignedRequestDocument$EbicsUnsignedRequest$Body$DataTransfer$SignatureData.
                 */
                public interface SignatureData extends SignatureDataType
                {
                    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
                      org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(SignatureData.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("signaturedataca69elemtype");
                    
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
                      public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.SignatureData newInstance() {
                        return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.SignatureData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                      
                      public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.SignatureData newInstance(org.apache.xmlbeans.XmlOptions options) {
                        return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.SignatureData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                      
                      private Factory() { } // No instance of this class allowed
                    }
                }
                
                /**
                 * An XML OrderData(@http://www.ebics.org/H003).
                 *
                 * This is an atomic type that is a restriction of EbicsUnsignedRequestDocument$EbicsUnsignedRequest$Body$DataTransfer$OrderData.
                 */
                public interface OrderData extends OrderDataType
                {
                    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
                      org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(OrderData.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("orderdata477felemtype");
                    
                    /**
                     * A factory class with static methods for creating instances
                     * of this type.
                     */
                    
                    public static final class Factory
                    {
                      public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.OrderData newInstance() {
                        return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.OrderData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                      
                      public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.OrderData newInstance(org.apache.xmlbeans.XmlOptions options) {
                        return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer.OrderData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                      
                      private Factory() { } // No instance of this class allowed
                    }
                }
                
                /**
                 * A factory class with static methods for creating instances
                 * of this type.
                 */
                
                public static final class Factory
                {
                    public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer newInstance() {
                      return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                    
                    public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer newInstance(org.apache.xmlbeans.XmlOptions options) {
                      return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body.DataTransfer) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                    
                    private Factory() { } // No instance of this class allowed
                }
            }
            
            /**
             * A factory class with static methods for creating instances
             * of this type.
             */
            
            public static final class Factory
            {
                public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body newInstance() {
                  return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                
                public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body newInstance(org.apache.xmlbeans.XmlOptions options) {
                  return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest.Body) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                
                private Factory() { } // No instance of this class allowed
            }
        }
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest newInstance() {
              return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static EbicsUnsignedRequestDocument.EbicsUnsignedRequest newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (EbicsUnsignedRequestDocument.EbicsUnsignedRequest) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static EbicsUnsignedRequestDocument newInstance() {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static EbicsUnsignedRequestDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static EbicsUnsignedRequestDocument parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static EbicsUnsignedRequestDocument parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static EbicsUnsignedRequestDocument parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static EbicsUnsignedRequestDocument parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static EbicsUnsignedRequestDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static EbicsUnsignedRequestDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static EbicsUnsignedRequestDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static EbicsUnsignedRequestDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static EbicsUnsignedRequestDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static EbicsUnsignedRequestDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static EbicsUnsignedRequestDocument parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static EbicsUnsignedRequestDocument parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static EbicsUnsignedRequestDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static EbicsUnsignedRequestDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static EbicsUnsignedRequestDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static EbicsUnsignedRequestDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (EbicsUnsignedRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
