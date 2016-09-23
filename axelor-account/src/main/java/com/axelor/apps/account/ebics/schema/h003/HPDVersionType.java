/*
 * XML Type:  HPDVersionType
 * Namespace: http://www.ebics.org/H003
 * Java type: HPDVersionType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML HPDVersionType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface HPDVersionType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(HPDVersionType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("hpdversiontypef4d0type");
    
    /**
     * Gets the "Protocol" element
     */
    java.util.List getProtocol();
    
    /**
     * Gets (as xml) the "Protocol" element
     */
    HPDVersionType.Protocol xgetProtocol();
    
    /**
     * Sets the "Protocol" element
     */
    void setProtocol(java.util.List protocol);
    
    /**
     * Sets (as xml) the "Protocol" element
     */
    void xsetProtocol(HPDVersionType.Protocol protocol);
    
    /**
     * Gets the "Authentication" element
     */
    java.util.List getAuthentication();
    
    /**
     * Gets (as xml) the "Authentication" element
     */
    HPDVersionType.Authentication xgetAuthentication();
    
    /**
     * Sets the "Authentication" element
     */
    void setAuthentication(java.util.List authentication);
    
    /**
     * Sets (as xml) the "Authentication" element
     */
    void xsetAuthentication(HPDVersionType.Authentication authentication);
    
    /**
     * Gets the "Encryption" element
     */
    java.util.List getEncryption();
    
    /**
     * Gets (as xml) the "Encryption" element
     */
    HPDVersionType.Encryption xgetEncryption();
    
    /**
     * Sets the "Encryption" element
     */
    void setEncryption(java.util.List encryption);
    
    /**
     * Sets (as xml) the "Encryption" element
     */
    void xsetEncryption(HPDVersionType.Encryption encryption);
    
    /**
     * Gets the "Signature" element
     */
    java.util.List getSignature();
    
    /**
     * Gets (as xml) the "Signature" element
     */
    HPDVersionType.Signature xgetSignature();
    
    /**
     * Sets the "Signature" element
     */
    void setSignature(java.util.List signature);
    
    /**
     * Sets (as xml) the "Signature" element
     */
    void xsetSignature(HPDVersionType.Signature signature);
    
    /**
     * An XML Protocol(@http://www.ebics.org/H003).
     *
     * This is a list type whose items are ProtocolVersionType.
     */
    public interface Protocol extends org.apache.xmlbeans.XmlAnySimpleType
    {
        java.util.List getListValue();
        java.util.List xgetListValue();
        void setListValue(java.util.List list);
        /** @deprecated */
        java.util.List listValue();
        /** @deprecated */
        java.util.List xlistValue();
        /** @deprecated */
        void set(java.util.List list);
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Protocol.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("protocolc4fcelemtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HPDVersionType.Protocol newValue(java.lang.Object obj) {
              return (HPDVersionType.Protocol) type.newValue( obj ); }
            
            public static HPDVersionType.Protocol newInstance() {
              return (HPDVersionType.Protocol) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HPDVersionType.Protocol newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HPDVersionType.Protocol) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML Authentication(@http://www.ebics.org/H003).
     *
     * This is a list type whose items are AuthenticationVersionType.
     */
    public interface Authentication extends org.apache.xmlbeans.XmlAnySimpleType
    {
        java.util.List getListValue();
        java.util.List xgetListValue();
        void setListValue(java.util.List list);
        /** @deprecated */
        java.util.List listValue();
        /** @deprecated */
        java.util.List xlistValue();
        /** @deprecated */
        void set(java.util.List list);
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Authentication.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("authentication6f1celemtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HPDVersionType.Authentication newValue(java.lang.Object obj) {
              return (HPDVersionType.Authentication) type.newValue( obj ); }
            
            public static HPDVersionType.Authentication newInstance() {
              return (HPDVersionType.Authentication) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HPDVersionType.Authentication newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HPDVersionType.Authentication) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML Encryption(@http://www.ebics.org/H003).
     *
     * This is a list type whose items are EncryptionVersionType.
     */
    public interface Encryption extends org.apache.xmlbeans.XmlAnySimpleType
    {
        java.util.List getListValue();
        java.util.List xgetListValue();
        void setListValue(java.util.List list);
        /** @deprecated */
        java.util.List listValue();
        /** @deprecated */
        java.util.List xlistValue();
        /** @deprecated */
        void set(java.util.List list);
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Encryption.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("encryption0d31elemtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HPDVersionType.Encryption newValue(java.lang.Object obj) {
              return (HPDVersionType.Encryption) type.newValue( obj ); }
            
            public static HPDVersionType.Encryption newInstance() {
              return (HPDVersionType.Encryption) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HPDVersionType.Encryption newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HPDVersionType.Encryption) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML Signature(@http://www.ebics.org/H003).
     *
     * This is a list type whose items are SignatureVersionType.
     */
    public interface Signature extends org.apache.xmlbeans.XmlAnySimpleType
    {
        java.util.List getListValue();
        java.util.List xgetListValue();
        void setListValue(java.util.List list);
        /** @deprecated */
        java.util.List listValue();
        /** @deprecated */
        java.util.List xlistValue();
        /** @deprecated */
        void set(java.util.List list);
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Signature.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("signaturedf4celemtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HPDVersionType.Signature newValue(java.lang.Object obj) {
              return (HPDVersionType.Signature) type.newValue( obj ); }
            
            public static HPDVersionType.Signature newInstance() {
              return (HPDVersionType.Signature) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HPDVersionType.Signature newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HPDVersionType.Signature) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static HPDVersionType newInstance() {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static HPDVersionType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static HPDVersionType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static HPDVersionType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static HPDVersionType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static HPDVersionType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static HPDVersionType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static HPDVersionType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static HPDVersionType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static HPDVersionType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static HPDVersionType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static HPDVersionType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static HPDVersionType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static HPDVersionType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static HPDVersionType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static HPDVersionType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HPDVersionType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HPDVersionType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HPDVersionType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
