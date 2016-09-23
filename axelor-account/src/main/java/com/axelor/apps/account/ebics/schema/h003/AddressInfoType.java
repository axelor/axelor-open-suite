/*
 * XML Type:  AddressInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: AddressInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML AddressInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface AddressInfoType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(AddressInfoType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("addressinfotype4244type");
    
    /**
     * Gets the "Name" element
     */
    java.lang.String getName();
    
    /**
     * Gets (as xml) the "Name" element
     */
    NameType xgetName();
    
    /**
     * True if has "Name" element
     */
    boolean isSetName();
    
    /**
     * Sets the "Name" element
     */
    void setName(java.lang.String name);
    
    /**
     * Sets (as xml) the "Name" element
     */
    void xsetName(NameType name);
    
    /**
     * Unsets the "Name" element
     */
    void unsetName();
    
    /**
     * Gets the "Street" element
     */
    java.lang.String getStreet();
    
    /**
     * Gets (as xml) the "Street" element
     */
    NameType xgetStreet();
    
    /**
     * True if has "Street" element
     */
    boolean isSetStreet();
    
    /**
     * Sets the "Street" element
     */
    void setStreet(java.lang.String street);
    
    /**
     * Sets (as xml) the "Street" element
     */
    void xsetStreet(NameType street);
    
    /**
     * Unsets the "Street" element
     */
    void unsetStreet();
    
    /**
     * Gets the "PostCode" element
     */
    java.lang.String getPostCode();
    
    /**
     * Gets (as xml) the "PostCode" element
     */
    org.apache.xmlbeans.XmlToken xgetPostCode();
    
    /**
     * True if has "PostCode" element
     */
    boolean isSetPostCode();
    
    /**
     * Sets the "PostCode" element
     */
    void setPostCode(java.lang.String postCode);
    
    /**
     * Sets (as xml) the "PostCode" element
     */
    void xsetPostCode(org.apache.xmlbeans.XmlToken postCode);
    
    /**
     * Unsets the "PostCode" element
     */
    void unsetPostCode();
    
    /**
     * Gets the "City" element
     */
    java.lang.String getCity();
    
    /**
     * Gets (as xml) the "City" element
     */
    NameType xgetCity();
    
    /**
     * True if has "City" element
     */
    boolean isSetCity();
    
    /**
     * Sets the "City" element
     */
    void setCity(java.lang.String city);
    
    /**
     * Sets (as xml) the "City" element
     */
    void xsetCity(NameType city);
    
    /**
     * Unsets the "City" element
     */
    void unsetCity();
    
    /**
     * Gets the "Region" element
     */
    java.lang.String getRegion();
    
    /**
     * Gets (as xml) the "Region" element
     */
    NameType xgetRegion();
    
    /**
     * True if has "Region" element
     */
    boolean isSetRegion();
    
    /**
     * Sets the "Region" element
     */
    void setRegion(java.lang.String region);
    
    /**
     * Sets (as xml) the "Region" element
     */
    void xsetRegion(NameType region);
    
    /**
     * Unsets the "Region" element
     */
    void unsetRegion();
    
    /**
     * Gets the "Country" element
     */
    java.lang.String getCountry();
    
    /**
     * Gets (as xml) the "Country" element
     */
    NameType xgetCountry();
    
    /**
     * True if has "Country" element
     */
    boolean isSetCountry();
    
    /**
     * Sets the "Country" element
     */
    void setCountry(java.lang.String country);
    
    /**
     * Sets (as xml) the "Country" element
     */
    void xsetCountry(NameType country);
    
    /**
     * Unsets the "Country" element
     */
    void unsetCountry();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static AddressInfoType newInstance() {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static AddressInfoType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static AddressInfoType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static AddressInfoType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static AddressInfoType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static AddressInfoType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static AddressInfoType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static AddressInfoType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static AddressInfoType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static AddressInfoType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static AddressInfoType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static AddressInfoType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static AddressInfoType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static AddressInfoType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static AddressInfoType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static AddressInfoType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static AddressInfoType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static AddressInfoType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (AddressInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
