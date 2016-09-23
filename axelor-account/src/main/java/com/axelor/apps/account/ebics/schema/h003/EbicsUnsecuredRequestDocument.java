/*
 * An XML document type.
 * Localname: ebicsUnsecuredRequest
 * Namespace: http://www.ebics.org/H003
 * Java type: EbicsUnsecuredRequestDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * A document containing one ebicsUnsecuredRequest(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public interface EbicsUnsecuredRequestDocument extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EbicsUnsecuredRequestDocument.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("ebicsunsecuredrequestb60fdoctype");
    
    /**
     * Gets the "ebicsUnsecuredRequest" element
     */
    EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest getEbicsUnsecuredRequest();
    
    /**
     * Sets the "ebicsUnsecuredRequest" element
     */
    void setEbicsUnsecuredRequest(EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest ebicsUnsecuredRequest);
    
    /**
     * Appends and returns a new empty "ebicsUnsecuredRequest" element
     */
    EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest addNewEbicsUnsecuredRequest();
    
    /**
     * An XML ebicsUnsecuredRequest(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface EbicsUnsecuredRequest extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EbicsUnsecuredRequest.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("ebicsunsecuredrequestcf92elemtype");
        
        /**
         * Gets the "header" element
         */
        EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header getHeader();
        
        /**
         * Sets the "header" element
         */
        void setHeader(EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header header);
        
        /**
         * Appends and returns a new empty "header" element
         */
        EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header addNewHeader();
        
        /**
         * Gets the "body" element
         */
        EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body getBody();
        
        /**
         * Sets the "body" element
         */
        void setBody(EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body body);
        
        /**
         * Appends and returns a new empty "body" element
         */
        EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body addNewBody();
        
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
                org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Header.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("header817belemtype");
            
            /**
             * Gets the "static" element
             */
            UnsecuredRequestStaticHeaderType getStatic();
            
            /**
             * Sets the "static" element
             */
            void setStatic(UnsecuredRequestStaticHeaderType xstatic);
            
            /**
             * Appends and returns a new empty "static" element
             */
            UnsecuredRequestStaticHeaderType addNewStatic();
            
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
                public static EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header newInstance() {
                  return (EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                
                public static EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header newInstance(org.apache.xmlbeans.XmlOptions options) {
                  return (EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                
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
                org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Body.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("bodye050elemtype");
            
            /**
             * Gets the "DataTransfer" element
             */
            EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer getDataTransfer();
            
            /**
             * Sets the "DataTransfer" element
             */
            void setDataTransfer(EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer dataTransfer);
            
            /**
             * Appends and returns a new empty "DataTransfer" element
             */
            EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer addNewDataTransfer();
            
            /**
             * An XML DataTransfer(@http://www.ebics.org/H003).
             *
             * This is a complex type.
             */
            public interface DataTransfer extends org.apache.xmlbeans.XmlObject
            {
                public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
                    org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(DataTransfer.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("datatransferfdffelemtype");
                
                /**
                 * Gets the "OrderData" element
                 */
                EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData getOrderData();
                
                /**
                 * Sets the "OrderData" element
                 */
                void setOrderData(EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData orderData);
                
                /**
                 * Appends and returns a new empty "OrderData" element
                 */
                EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData addNewOrderData();
                
                /**
                 * An XML OrderData(@http://www.ebics.org/H003).
                 *
                 * This is an atomic type that is a restriction of EbicsUnsecuredRequestDocument$EbicsUnsecuredRequest$Body$DataTransfer$OrderData.
                 */
                public interface OrderData extends OrderDataType
                {
                    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
                      org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(OrderData.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("orderdata2463elemtype");
                    
                    /**
                     * A factory class with static methods for creating instances
                     * of this type.
                     */
                    
                    public static final class Factory
                    {
                      public static EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData newInstance() {
                        return (EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                      
                      public static EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData newInstance(org.apache.xmlbeans.XmlOptions options) {
                        return (EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                      
                      private Factory() { } // No instance of this class allowed
                    }
                }
                
                /**
                 * A factory class with static methods for creating instances
                 * of this type.
                 */
                
                public static final class Factory
                {
                    public static EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer newInstance() {
                      return (EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                    
                    public static EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer newInstance(org.apache.xmlbeans.XmlOptions options) {
                      return (EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                    
                    private Factory() { } // No instance of this class allowed
                }
            }
            
            /**
             * A factory class with static methods for creating instances
             * of this type.
             */
            
            public static final class Factory
            {
                public static EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body newInstance() {
                  return (EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                
                public static EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body newInstance(org.apache.xmlbeans.XmlOptions options) {
                  return (EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                
                private Factory() { } // No instance of this class allowed
            }
        }
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest newInstance() {
              return (EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static EbicsUnsecuredRequestDocument newInstance() {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static EbicsUnsecuredRequestDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static EbicsUnsecuredRequestDocument parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static EbicsUnsecuredRequestDocument parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static EbicsUnsecuredRequestDocument parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static EbicsUnsecuredRequestDocument parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static EbicsUnsecuredRequestDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static EbicsUnsecuredRequestDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static EbicsUnsecuredRequestDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static EbicsUnsecuredRequestDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static EbicsUnsecuredRequestDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static EbicsUnsecuredRequestDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static EbicsUnsecuredRequestDocument parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static EbicsUnsecuredRequestDocument parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static EbicsUnsecuredRequestDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static EbicsUnsecuredRequestDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static EbicsUnsecuredRequestDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static EbicsUnsecuredRequestDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (EbicsUnsecuredRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
