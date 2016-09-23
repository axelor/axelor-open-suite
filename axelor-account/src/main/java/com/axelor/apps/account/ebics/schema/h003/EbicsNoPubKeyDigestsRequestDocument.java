/*
 * An XML document type.
 * Localname: ebicsNoPubKeyDigestsRequest
 * Namespace: http://www.ebics.org/H003
 * Java type: EbicsNoPubKeyDigestsRequestDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * A document containing one ebicsNoPubKeyDigestsRequest(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public interface EbicsNoPubKeyDigestsRequestDocument extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EbicsNoPubKeyDigestsRequestDocument.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("ebicsnopubkeydigestsrequest0b77doctype");
    
    /**
     * Gets the "ebicsNoPubKeyDigestsRequest" element
     */
    EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest getEbicsNoPubKeyDigestsRequest();
    
    /**
     * Sets the "ebicsNoPubKeyDigestsRequest" element
     */
    void setEbicsNoPubKeyDigestsRequest(EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest ebicsNoPubKeyDigestsRequest);
    
    /**
     * Appends and returns a new empty "ebicsNoPubKeyDigestsRequest" element
     */
    EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest addNewEbicsNoPubKeyDigestsRequest();
    
    /**
     * An XML ebicsNoPubKeyDigestsRequest(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public interface EbicsNoPubKeyDigestsRequest extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EbicsNoPubKeyDigestsRequest.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("ebicsnopubkeydigestsrequest3162elemtype");
        
        /**
         * Gets the "header" element
         */
        EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header getHeader();
        
        /**
         * Sets the "header" element
         */
        void setHeader(EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header header);
        
        /**
         * Appends and returns a new empty "header" element
         */
        EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header addNewHeader();
        
        /**
         * Gets the "AuthSignature" element
         */
        SignatureType getAuthSignature();
        
        /**
         * Sets the "AuthSignature" element
         */
        void setAuthSignature(SignatureType authSignature);
        
        /**
         * Appends and returns a new empty "AuthSignature" element
         */
        SignatureType addNewAuthSignature();
        
        /**
         * Gets the "body" element
         */
        EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body getBody();
        
        /**
         * Sets the "body" element
         */
        void setBody(EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body body);
        
        /**
         * Appends and returns a new empty "body" element
         */
        EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body addNewBody();
        
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
                org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Header.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("header4dcbelemtype");
            
            /**
             * Gets the "static" element
             */
            NoPubKeyDigestsRequestStaticHeaderType getStatic();
            
            /**
             * Sets the "static" element
             */
            void setStatic(NoPubKeyDigestsRequestStaticHeaderType xstatic);
            
            /**
             * Appends and returns a new empty "static" element
             */
            NoPubKeyDigestsRequestStaticHeaderType addNewStatic();
            
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
                public static EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header newInstance() {
                  return (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                
                public static EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header newInstance(org.apache.xmlbeans.XmlOptions options) {
                  return (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                
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
                org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Body.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("body1520elemtype");
            
            /**
             * A factory class with static methods for creating instances
             * of this type.
             */
            
            public static final class Factory
            {
                public static EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body newInstance() {
                  return (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                
                public static EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body newInstance(org.apache.xmlbeans.XmlOptions options) {
                  return (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                
                private Factory() { } // No instance of this class allowed
            }
        }
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest newInstance() {
              return (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static EbicsNoPubKeyDigestsRequestDocument newInstance() {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static EbicsNoPubKeyDigestsRequestDocument parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static EbicsNoPubKeyDigestsRequestDocument parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static EbicsNoPubKeyDigestsRequestDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static EbicsNoPubKeyDigestsRequestDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static EbicsNoPubKeyDigestsRequestDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (EbicsNoPubKeyDigestsRequestDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
