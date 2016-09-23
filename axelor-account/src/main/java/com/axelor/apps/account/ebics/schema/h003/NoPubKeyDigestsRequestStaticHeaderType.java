/*
 * XML Type:  NoPubKeyDigestsRequestStaticHeaderType
 * Namespace: http://www.ebics.org/H003
 * Java type: NoPubKeyDigestsRequestStaticHeaderType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML NoPubKeyDigestsRequestStaticHeaderType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface NoPubKeyDigestsRequestStaticHeaderType extends StaticHeaderBaseType
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(NoPubKeyDigestsRequestStaticHeaderType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("nopubkeydigestsrequeststaticheadertype6a32type");
    
    /**
     * Gets the "Nonce" element
     */
    byte[] getNonce();
    
    /**
     * Gets (as xml) the "Nonce" element
     */
    NonceType xgetNonce();
    
    /**
     * True if has "Nonce" element
     */
    boolean isSetNonce();
    
    /**
     * Sets the "Nonce" element
     */
    void setNonce(byte[] nonce);
    
    /**
     * Sets (as xml) the "Nonce" element
     */
    void xsetNonce(NonceType nonce);
    
    /**
     * Unsets the "Nonce" element
     */
    void unsetNonce();
    
    /**
     * Gets the "Timestamp" element
     */
    java.util.Calendar getTimestamp();
    
    /**
     * Gets (as xml) the "Timestamp" element
     */
    TimestampType xgetTimestamp();
    
    /**
     * True if has "Timestamp" element
     */
    boolean isSetTimestamp();
    
    /**
     * Sets the "Timestamp" element
     */
    void setTimestamp(java.util.Calendar timestamp);
    
    /**
     * Sets (as xml) the "Timestamp" element
     */
    void xsetTimestamp(TimestampType timestamp);
    
    /**
     * Unsets the "Timestamp" element
     */
    void unsetTimestamp();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static NoPubKeyDigestsRequestStaticHeaderType newInstance() {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static NoPubKeyDigestsRequestStaticHeaderType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static NoPubKeyDigestsRequestStaticHeaderType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static NoPubKeyDigestsRequestStaticHeaderType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static NoPubKeyDigestsRequestStaticHeaderType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static NoPubKeyDigestsRequestStaticHeaderType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (NoPubKeyDigestsRequestStaticHeaderType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
