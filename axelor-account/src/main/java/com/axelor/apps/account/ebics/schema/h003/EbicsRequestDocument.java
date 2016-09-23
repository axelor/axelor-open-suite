/*
 * An XML document type.
 * Localname: ebicsRequest
 * Namespace: http://www.ebics.org/H003
 * Java type: EbicsRequestDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * A document containing one ebicsRequest(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public interface EbicsRequestDocument extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EbicsRequestDocument.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("ebicsrequest369ddoctype");
    
    /**
     * Gets the "ebicsRequest" element
     */
    EbicsRequestDocument.EbicsRequest getEbicsRequest();
    
    /**
     * Sets the "ebicsRequest" element
     */
    void setEbicsRequest(EbicsRequestDocument.EbicsRequest ebicsRequest);
    
    /**
     * Appends and returns a new empty "ebicsRequest" element
     */
    EbicsRequestDocument.EbicsRequest addNewEbicsRequest();
    
    /**
     * An XML ebicsRequest(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface EbicsRequest extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EbicsRequest.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("ebicsrequestb0ceelemtype");
        
        /**
         * Gets the "header" element
         */
        EbicsRequestDocument.EbicsRequest.Header getHeader();
        
        /**
         * Sets the "header" element
         */
        void setHeader(EbicsRequestDocument.EbicsRequest.Header header);
        
        /**
         * Appends and returns a new empty "header" element
         */
        EbicsRequestDocument.EbicsRequest.Header addNewHeader();
        
        /**
         * Gets the "AuthSignature" element
         */
        com.axelor.apps.account.ebics.schema.xmldsig.SignatureType getAuthSignature();
        
        /**
         * Sets the "AuthSignature" element
         */
        void setAuthSignature(com.axelor.apps.account.ebics.schema.xmldsig.SignatureType authSignature);
        
        /**
         * Appends and returns a new empty "AuthSignature" element
         */
        com.axelor.apps.account.ebics.schema.xmldsig.SignatureType addNewAuthSignature();
        
        /**
         * Gets the "body" element
         */
        EbicsRequestDocument.EbicsRequest.Body getBody();
        
        /**
         * Sets the "body" element
         */
        void setBody(EbicsRequestDocument.EbicsRequest.Body body);
        
        /**
         * Appends and returns a new empty "body" element
         */
        EbicsRequestDocument.EbicsRequest.Body addNewBody();
        
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
                org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Header.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("header3af7elemtype");
            
            /**
             * Gets the "static" element
             */
            StaticHeaderType getStatic();
            
            /**
             * Sets the "static" element
             */
            void setStatic(StaticHeaderType xstatic);
            
            /**
             * Appends and returns a new empty "static" element
             */
            StaticHeaderType addNewStatic();
            
            /**
             * Gets the "mutable" element
             */
            MutableHeaderType getMutable();
            
            /**
             * Sets the "mutable" element
             */
            void setMutable(MutableHeaderType mutable);
            
            /**
             * Appends and returns a new empty "mutable" element
             */
            MutableHeaderType addNewMutable();
            
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
                public static EbicsRequestDocument.EbicsRequest.Header newInstance() {
                  return (EbicsRequestDocument.EbicsRequest.Header) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                
                public static EbicsRequestDocument.EbicsRequest.Header newInstance(org.apache.xmlbeans.XmlOptions options) {
                  return (EbicsRequestDocument.EbicsRequest.Header) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                
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
                org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Body.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("body150celemtype");
            
            /**
             * Gets the "PreValidation" element
             */
            EbicsRequestDocument.EbicsRequest.Body.PreValidation getPreValidation();
            
            /**
             * True if has "PreValidation" element
             */
            boolean isSetPreValidation();
            
            /**
             * Sets the "PreValidation" element
             */
            void setPreValidation(EbicsRequestDocument.EbicsRequest.Body.PreValidation preValidation);
            
            /**
             * Appends and returns a new empty "PreValidation" element
             */
            EbicsRequestDocument.EbicsRequest.Body.PreValidation addNewPreValidation();
            
            /**
             * Unsets the "PreValidation" element
             */
            void unsetPreValidation();
            
            /**
             * Gets the "DataTransfer" element
             */
            DataTransferRequestType getDataTransfer();
            
            /**
             * True if has "DataTransfer" element
             */
            boolean isSetDataTransfer();
            
            /**
             * Sets the "DataTransfer" element
             */
            void setDataTransfer(DataTransferRequestType dataTransfer);
            
            /**
             * Appends and returns a new empty "DataTransfer" element
             */
            DataTransferRequestType addNewDataTransfer();
            
            /**
             * Unsets the "DataTransfer" element
             */
            void unsetDataTransfer();
            
            /**
             * Gets the "TransferReceipt" element
             */
            EbicsRequestDocument.EbicsRequest.Body.TransferReceipt getTransferReceipt();
            
            /**
             * True if has "TransferReceipt" element
             */
            boolean isSetTransferReceipt();
            
            /**
             * Sets the "TransferReceipt" element
             */
            void setTransferReceipt(EbicsRequestDocument.EbicsRequest.Body.TransferReceipt transferReceipt);
            
            /**
             * Appends and returns a new empty "TransferReceipt" element
             */
            EbicsRequestDocument.EbicsRequest.Body.TransferReceipt addNewTransferReceipt();
            
            /**
             * Unsets the "TransferReceipt" element
             */
            void unsetTransferReceipt();
            
            /**
             * An XML PreValidation(@http://www.ebics.org/H003).
             *
             * This is a complex type.
             */
            public interface PreValidation extends PreValidationRequestType
            {
                public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
                    org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(PreValidation.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("prevalidation7644elemtype");
                
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
                    public static EbicsRequestDocument.EbicsRequest.Body.PreValidation newInstance() {
                      return (EbicsRequestDocument.EbicsRequest.Body.PreValidation) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                    
                    public static EbicsRequestDocument.EbicsRequest.Body.PreValidation newInstance(org.apache.xmlbeans.XmlOptions options) {
                      return (EbicsRequestDocument.EbicsRequest.Body.PreValidation) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                    
                    private Factory() { } // No instance of this class allowed
                }
            }
            
            /**
             * An XML TransferReceipt(@http://www.ebics.org/H003).
             *
             * This is a complex type.
             */
            public interface TransferReceipt extends TransferReceiptRequestType
            {
                public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
                    org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(TransferReceipt.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("transferreceipt65d3elemtype");
                
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
                    public static EbicsRequestDocument.EbicsRequest.Body.TransferReceipt newInstance() {
                      return (EbicsRequestDocument.EbicsRequest.Body.TransferReceipt) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                    
                    public static EbicsRequestDocument.EbicsRequest.Body.TransferReceipt newInstance(org.apache.xmlbeans.XmlOptions options) {
                      return (EbicsRequestDocument.EbicsRequest.Body.TransferReceipt) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                    
                    private Factory() { } // No instance of this class allowed
                }
            }
            
            /**
             * A factory class with static methods for creating instances
             * of this type.
             */
            
            public static final class Factory
            {
                public static EbicsRequestDocument.EbicsRequest.Body newInstance() {
                  return (EbicsRequestDocument.EbicsRequest.Body) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                
                public static EbicsRequestDocument.EbicsRequest.Body newInstance(org.apache.xmlbeans.XmlOptions options) {
                  return (EbicsRequestDocument.EbicsRequest.Body) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                
                private Factory() { } // No instance of this class allowed
            }
        }
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static EbicsRequestDocument.EbicsRequest newInstance() {
              return (EbicsRequestDocument.EbicsRequest) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static EbicsRequestDocument.EbicsRequest newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (EbicsRequestDocument.EbicsRequest) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static EbicsRequestDocument newInstance() {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static EbicsRequestDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static EbicsRequestDocument parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static EbicsRequestDocument parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static EbicsRequestDocument parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static EbicsRequestDocument parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static EbicsRequestDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static EbicsRequestDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static EbicsRequestDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static EbicsRequestDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static EbicsRequestDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static EbicsRequestDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static EbicsRequestDocument parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static EbicsRequestDocument parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static EbicsRequestDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static EbicsRequestDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static EbicsRequestDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static EbicsRequestDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (EbicsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
