/*
 * XML Type:  HVTOrderInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVTOrderInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML HVTOrderInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface HVTOrderInfoType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(HVTOrderInfoType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("hvtorderinfotypeb2catype");
    
    /**
     * Gets the "OrderFormat" element
     */
    java.lang.String getOrderFormat();
    
    /**
     * Gets (as xml) the "OrderFormat" element
     */
    OrderFormatType xgetOrderFormat();
    
    /**
     * True if has "OrderFormat" element
     */
    boolean isSetOrderFormat();
    
    /**
     * Sets the "OrderFormat" element
     */
    void setOrderFormat(java.lang.String orderFormat);
    
    /**
     * Sets (as xml) the "OrderFormat" element
     */
    void xsetOrderFormat(OrderFormatType orderFormat);
    
    /**
     * Unsets the "OrderFormat" element
     */
    void unsetOrderFormat();
    
    /**
     * Gets array of all "AccountInfo" elements
     */
    HVTAccountInfoType[] getAccountInfoArray();
    
    /**
     * Gets ith "AccountInfo" element
     */
    HVTAccountInfoType getAccountInfoArray(int i);
    
    /**
     * Returns number of "AccountInfo" element
     */
    int sizeOfAccountInfoArray();
    
    /**
     * Sets array of all "AccountInfo" element
     */
    void setAccountInfoArray(HVTAccountInfoType[] accountInfoArray);
    
    /**
     * Sets ith "AccountInfo" element
     */
    void setAccountInfoArray(int i, HVTAccountInfoType accountInfo);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "AccountInfo" element
     */
    HVTAccountInfoType insertNewAccountInfo(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "AccountInfo" element
     */
    HVTAccountInfoType addNewAccountInfo();
    
    /**
     * Removes the ith "AccountInfo" element
     */
    void removeAccountInfo(int i);
    
    /**
     * Gets the "ExecutionDate" element
     */
    HVTOrderInfoType.ExecutionDate getExecutionDate();
    
    /**
     * True if has "ExecutionDate" element
     */
    boolean isSetExecutionDate();
    
    /**
     * Sets the "ExecutionDate" element
     */
    void setExecutionDate(HVTOrderInfoType.ExecutionDate executionDate);
    
    /**
     * Appends and returns a new empty "ExecutionDate" element
     */
    HVTOrderInfoType.ExecutionDate addNewExecutionDate();
    
    /**
     * Unsets the "ExecutionDate" element
     */
    void unsetExecutionDate();
    
    /**
     * Gets the "Amount" element
     */
    HVTOrderInfoType.Amount getAmount();
    
    /**
     * Sets the "Amount" element
     */
    void setAmount(HVTOrderInfoType.Amount amount);
    
    /**
     * Appends and returns a new empty "Amount" element
     */
    HVTOrderInfoType.Amount addNewAmount();
    
    /**
     * Gets array of all "Description" elements
     */
    HVTOrderInfoType.Description[] getDescriptionArray();
    
    /**
     * Gets ith "Description" element
     */
    HVTOrderInfoType.Description getDescriptionArray(int i);
    
    /**
     * Returns number of "Description" element
     */
    int sizeOfDescriptionArray();
    
    /**
     * Sets array of all "Description" element
     */
    void setDescriptionArray(HVTOrderInfoType.Description[] descriptionArray);
    
    /**
     * Sets ith "Description" element
     */
    void setDescriptionArray(int i, HVTOrderInfoType.Description description);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "Description" element
     */
    HVTOrderInfoType.Description insertNewDescription(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "Description" element
     */
    HVTOrderInfoType.Description addNewDescription();
    
    /**
     * Removes the ith "Description" element
     */
    void removeDescription(int i);
    
    /**
     * An XML ExecutionDate(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of HVTOrderInfoType$ExecutionDate.
     */
    public interface ExecutionDate extends org.apache.xmlbeans.XmlDate
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(ExecutionDate.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("executiondatefad8elemtype");
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HVTOrderInfoType.ExecutionDate newInstance() {
              return (HVTOrderInfoType.ExecutionDate) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HVTOrderInfoType.ExecutionDate newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HVTOrderInfoType.ExecutionDate) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML Amount(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of HVTOrderInfoType$Amount.
     */
    public interface Amount extends AmountValueType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Amount.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("amount3036elemtype");
        
        /**
         * Gets the "isCredit" attribute
         */
        boolean getIsCredit();
        
        /**
         * Gets (as xml) the "isCredit" attribute
         */
        org.apache.xmlbeans.XmlBoolean xgetIsCredit();
        
        /**
         * True if has "isCredit" attribute
         */
        boolean isSetIsCredit();
        
        /**
         * Sets the "isCredit" attribute
         */
        void setIsCredit(boolean isCredit);
        
        /**
         * Sets (as xml) the "isCredit" attribute
         */
        void xsetIsCredit(org.apache.xmlbeans.XmlBoolean isCredit);
        
        /**
         * Unsets the "isCredit" attribute
         */
        void unsetIsCredit();
        
        /**
         * Gets the "Currency" attribute
         */
        java.lang.String getCurrency();
        
        /**
         * Gets (as xml) the "Currency" attribute
         */
        CurrencyBaseType xgetCurrency();
        
        /**
         * True if has "Currency" attribute
         */
        boolean isSetCurrency();
        
        /**
         * Sets the "Currency" attribute
         */
        void setCurrency(java.lang.String currency);
        
        /**
         * Sets (as xml) the "Currency" attribute
         */
        void xsetCurrency(CurrencyBaseType currency);
        
        /**
         * Unsets the "Currency" attribute
         */
        void unsetCurrency();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HVTOrderInfoType.Amount newInstance() {
              return (HVTOrderInfoType.Amount) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HVTOrderInfoType.Amount newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HVTOrderInfoType.Amount) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML Description(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of HVTOrderInfoType$Description.
     */
    public interface Description extends org.apache.xmlbeans.XmlString
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Description.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("descriptionb382elemtype");
        
        /**
         * Gets the "Type" attribute
         */
        HVTOrderInfoType.Description.Type.Enum getType();
        
        /**
         * Gets (as xml) the "Type" attribute
         */
        HVTOrderInfoType.Description.Type xgetType();
        
        /**
         * Sets the "Type" attribute
         */
        void setType(HVTOrderInfoType.Description.Type.Enum type);
        
        /**
         * Sets (as xml) the "Type" attribute
         */
        void xsetType(HVTOrderInfoType.Description.Type type);
        
        /**
         * An XML Type(@).
         *
         * This is an atomic type that is a restriction of HVTOrderInfoType$Description$Type.
         */
        public interface Type extends org.apache.xmlbeans.XmlToken
        {
            public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
                org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Type.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("typee1c8attrtype");
            
            org.apache.xmlbeans.StringEnumAbstractBase enumValue();
            void set(org.apache.xmlbeans.StringEnumAbstractBase e);
            
            static final Enum PURPOSE = Enum.forString("Purpose");
            static final Enum DETAILS = Enum.forString("Details");
            static final Enum COMMENT = Enum.forString("Comment");
            
            static final int INT_PURPOSE = Enum.INT_PURPOSE;
            static final int INT_DETAILS = Enum.INT_DETAILS;
            static final int INT_COMMENT = Enum.INT_COMMENT;
            
            /**
             * Enumeration value class for HVTOrderInfoType$Description$Type.
             * These enum values can be used as follows:
             * <pre>
             * enum.toString(); // returns the string value of the enum
             * enum.intValue(); // returns an int value, useful for switches
             * // e.g., case Enum.INT_PURPOSE
             * Enum.forString(s); // returns the enum value for a string
             * Enum.forInt(i); // returns the enum value for an int
             * </pre>
             * Enumeration objects are immutable singleton objects that
             * can be compared using == object equality. They have no
             * public constructor. See the constants defined within this
             * class for all the valid values.
             */
            static final class Enum extends org.apache.xmlbeans.StringEnumAbstractBase
            {
                /**
                 * Returns the enum value for a string, or null if none.
                 */
                public static Enum forString(java.lang.String s)
                    { return (Enum)table.forString(s); }
                /**
                 * Returns the enum value corresponding to an int, or null if none.
                 */
                public static Enum forInt(int i)
                    { return (Enum)table.forInt(i); }
                
                private Enum(java.lang.String s, int i)
                    { super(s, i); }
                
                static final int INT_PURPOSE = 1;
                static final int INT_DETAILS = 2;
                static final int INT_COMMENT = 3;
                
                public static final org.apache.xmlbeans.StringEnumAbstractBase.Table table =
                    new org.apache.xmlbeans.StringEnumAbstractBase.Table
                (
                    new Enum[]
                    {
                      new Enum("Purpose", INT_PURPOSE),
                      new Enum("Details", INT_DETAILS),
                      new Enum("Comment", INT_COMMENT),
                    }
                );
                private static final long serialVersionUID = 1L;
                private java.lang.Object readResolve() { return forInt(intValue()); } 
            }
            
            /**
             * A factory class with static methods for creating instances
             * of this type.
             */
            
            public static final class Factory
            {
                public static HVTOrderInfoType.Description.Type newValue(java.lang.Object obj) {
                  return (HVTOrderInfoType.Description.Type) type.newValue( obj ); }
                
                public static HVTOrderInfoType.Description.Type newInstance() {
                  return (HVTOrderInfoType.Description.Type) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                
                public static HVTOrderInfoType.Description.Type newInstance(org.apache.xmlbeans.XmlOptions options) {
                  return (HVTOrderInfoType.Description.Type) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                
                private Factory() { } // No instance of this class allowed
            }
        }
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static HVTOrderInfoType.Description newInstance() {
              return (HVTOrderInfoType.Description) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static HVTOrderInfoType.Description newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (HVTOrderInfoType.Description) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static HVTOrderInfoType newInstance() {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static HVTOrderInfoType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static HVTOrderInfoType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static HVTOrderInfoType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static HVTOrderInfoType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static HVTOrderInfoType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static HVTOrderInfoType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static HVTOrderInfoType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static HVTOrderInfoType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static HVTOrderInfoType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static HVTOrderInfoType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static HVTOrderInfoType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static HVTOrderInfoType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static HVTOrderInfoType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static HVTOrderInfoType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static HVTOrderInfoType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HVTOrderInfoType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static HVTOrderInfoType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (HVTOrderInfoType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
