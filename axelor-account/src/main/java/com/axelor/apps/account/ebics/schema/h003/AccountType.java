/*
 * XML Type:  AccountType
 * Namespace: http://www.ebics.org/H003
 * Java type: AccountType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003;


/**
 * An XML AccountType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public interface AccountType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(AccountType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("accounttype47eftype");
    
    /**
     * Gets array of all "AccountNumber" elements
     */
    AccountType.AccountNumber[] getAccountNumberArray();
    
    /**
     * Gets ith "AccountNumber" element
     */
    AccountType.AccountNumber getAccountNumberArray(int i);
    
    /**
     * Returns number of "AccountNumber" element
     */
    int sizeOfAccountNumberArray();
    
    /**
     * Sets array of all "AccountNumber" element
     */
    void setAccountNumberArray(AccountType.AccountNumber[] accountNumberArray);
    
    /**
     * Sets ith "AccountNumber" element
     */
    void setAccountNumberArray(int i, AccountType.AccountNumber accountNumber);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "AccountNumber" element
     */
    AccountType.AccountNumber insertNewAccountNumber(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "AccountNumber" element
     */
    AccountType.AccountNumber addNewAccountNumber();
    
    /**
     * Removes the ith "AccountNumber" element
     */
    void removeAccountNumber(int i);
    
    /**
     * Gets array of all "NationalAccountNumber" elements
     */
    AccountType.NationalAccountNumber[] getNationalAccountNumberArray();
    
    /**
     * Gets ith "NationalAccountNumber" element
     */
    AccountType.NationalAccountNumber getNationalAccountNumberArray(int i);
    
    /**
     * Returns number of "NationalAccountNumber" element
     */
    int sizeOfNationalAccountNumberArray();
    
    /**
     * Sets array of all "NationalAccountNumber" element
     */
    void setNationalAccountNumberArray(AccountType.NationalAccountNumber[] nationalAccountNumberArray);
    
    /**
     * Sets ith "NationalAccountNumber" element
     */
    void setNationalAccountNumberArray(int i, AccountType.NationalAccountNumber nationalAccountNumber);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "NationalAccountNumber" element
     */
    AccountType.NationalAccountNumber insertNewNationalAccountNumber(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "NationalAccountNumber" element
     */
    AccountType.NationalAccountNumber addNewNationalAccountNumber();
    
    /**
     * Removes the ith "NationalAccountNumber" element
     */
    void removeNationalAccountNumber(int i);
    
    /**
     * Gets array of all "BankCode" elements
     */
    AccountType.BankCode[] getBankCodeArray();
    
    /**
     * Gets ith "BankCode" element
     */
    AccountType.BankCode getBankCodeArray(int i);
    
    /**
     * Returns number of "BankCode" element
     */
    int sizeOfBankCodeArray();
    
    /**
     * Sets array of all "BankCode" element
     */
    void setBankCodeArray(AccountType.BankCode[] bankCodeArray);
    
    /**
     * Sets ith "BankCode" element
     */
    void setBankCodeArray(int i, AccountType.BankCode bankCode);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "BankCode" element
     */
    AccountType.BankCode insertNewBankCode(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "BankCode" element
     */
    AccountType.BankCode addNewBankCode();
    
    /**
     * Removes the ith "BankCode" element
     */
    void removeBankCode(int i);
    
    /**
     * Gets array of all "NationalBankCode" elements
     */
    AccountType.NationalBankCode[] getNationalBankCodeArray();
    
    /**
     * Gets ith "NationalBankCode" element
     */
    AccountType.NationalBankCode getNationalBankCodeArray(int i);
    
    /**
     * Returns number of "NationalBankCode" element
     */
    int sizeOfNationalBankCodeArray();
    
    /**
     * Sets array of all "NationalBankCode" element
     */
    void setNationalBankCodeArray(AccountType.NationalBankCode[] nationalBankCodeArray);
    
    /**
     * Sets ith "NationalBankCode" element
     */
    void setNationalBankCodeArray(int i, AccountType.NationalBankCode nationalBankCode);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "NationalBankCode" element
     */
    AccountType.NationalBankCode insertNewNationalBankCode(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "NationalBankCode" element
     */
    AccountType.NationalBankCode addNewNationalBankCode();
    
    /**
     * Removes the ith "NationalBankCode" element
     */
    void removeNationalBankCode(int i);
    
    /**
     * Gets the "AccountHolder" element
     */
    java.lang.String getAccountHolder();
    
    /**
     * Gets (as xml) the "AccountHolder" element
     */
    AccountHolderType xgetAccountHolder();
    
    /**
     * True if has "AccountHolder" element
     */
    boolean isSetAccountHolder();
    
    /**
     * Sets the "AccountHolder" element
     */
    void setAccountHolder(java.lang.String accountHolder);
    
    /**
     * Sets (as xml) the "AccountHolder" element
     */
    void xsetAccountHolder(AccountHolderType accountHolder);
    
    /**
     * Unsets the "AccountHolder" element
     */
    void unsetAccountHolder();
    
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
     * Gets the "Description" attribute
     */
    java.lang.String getDescription();
    
    /**
     * Gets (as xml) the "Description" attribute
     */
    AccountDescriptionType xgetDescription();
    
    /**
     * True if has "Description" attribute
     */
    boolean isSetDescription();
    
    /**
     * Sets the "Description" attribute
     */
    void setDescription(java.lang.String description);
    
    /**
     * Sets (as xml) the "Description" attribute
     */
    void xsetDescription(AccountDescriptionType description);
    
    /**
     * Unsets the "Description" attribute
     */
    void unsetDescription();
    
    /**
     * An XML AccountNumber(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of AccountType$AccountNumber.
     */
    public interface AccountNumber extends AccountNumberType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(AccountNumber.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("accountnumber1811elemtype");
        
        /**
         * Gets the "international" attribute
         */
        boolean getInternational();
        
        /**
         * Gets (as xml) the "international" attribute
         */
        org.apache.xmlbeans.XmlBoolean xgetInternational();
        
        /**
         * True if has "international" attribute
         */
        boolean isSetInternational();
        
        /**
         * Sets the "international" attribute
         */
        void setInternational(boolean international);
        
        /**
         * Sets (as xml) the "international" attribute
         */
        void xsetInternational(org.apache.xmlbeans.XmlBoolean international);
        
        /**
         * Unsets the "international" attribute
         */
        void unsetInternational();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static AccountType.AccountNumber newInstance() {
              return (AccountType.AccountNumber) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static AccountType.AccountNumber newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (AccountType.AccountNumber) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML NationalAccountNumber(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of AccountType$NationalAccountNumber.
     */
    public interface NationalAccountNumber extends NationalAccountNumberType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(NationalAccountNumber.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("nationalaccountnumbera57felemtype");
        
        /**
         * Gets the "format" attribute
         */
        java.lang.String getFormat();
        
        /**
         * Gets (as xml) the "format" attribute
         */
        org.apache.xmlbeans.XmlToken xgetFormat();
        
        /**
         * Sets the "format" attribute
         */
        void setFormat(java.lang.String format);
        
        /**
         * Sets (as xml) the "format" attribute
         */
        void xsetFormat(org.apache.xmlbeans.XmlToken format);
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static AccountType.NationalAccountNumber newInstance() {
              return (AccountType.NationalAccountNumber) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static AccountType.NationalAccountNumber newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (AccountType.NationalAccountNumber) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML BankCode(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of AccountType$BankCode.
     */
    public interface BankCode extends BankCodeType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(BankCode.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("bankcode1df4elemtype");
        
        /**
         * Gets the "international" attribute
         */
        boolean getInternational();
        
        /**
         * Gets (as xml) the "international" attribute
         */
        org.apache.xmlbeans.XmlBoolean xgetInternational();
        
        /**
         * True if has "international" attribute
         */
        boolean isSetInternational();
        
        /**
         * Sets the "international" attribute
         */
        void setInternational(boolean international);
        
        /**
         * Sets (as xml) the "international" attribute
         */
        void xsetInternational(org.apache.xmlbeans.XmlBoolean international);
        
        /**
         * Unsets the "international" attribute
         */
        void unsetInternational();
        
        /**
         * Gets the "Prefix" attribute
         */
        java.lang.String getPrefix();
        
        /**
         * Gets (as xml) the "Prefix" attribute
         */
        BankCodePrefixType xgetPrefix();
        
        /**
         * True if has "Prefix" attribute
         */
        boolean isSetPrefix();
        
        /**
         * Sets the "Prefix" attribute
         */
        void setPrefix(java.lang.String prefix);
        
        /**
         * Sets (as xml) the "Prefix" attribute
         */
        void xsetPrefix(BankCodePrefixType prefix);
        
        /**
         * Unsets the "Prefix" attribute
         */
        void unsetPrefix();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static AccountType.BankCode newInstance() {
              return (AccountType.BankCode) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static AccountType.BankCode newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (AccountType.BankCode) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML NationalBankCode(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of AccountType$NationalBankCode.
     */
    public interface NationalBankCode extends NationalBankCodeType
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(NationalBankCode.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sE38346ABFB9D0612C4CA50E995509F1D").resolveHandle("nationalbankcode63c6elemtype");
        
        /**
         * Gets the "format" attribute
         */
        java.lang.String getFormat();
        
        /**
         * Gets (as xml) the "format" attribute
         */
        org.apache.xmlbeans.XmlToken xgetFormat();
        
        /**
         * Sets the "format" attribute
         */
        void setFormat(java.lang.String format);
        
        /**
         * Sets (as xml) the "format" attribute
         */
        void xsetFormat(org.apache.xmlbeans.XmlToken format);
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static AccountType.NationalBankCode newInstance() {
              return (AccountType.NationalBankCode) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static AccountType.NationalBankCode newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (AccountType.NationalBankCode) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static AccountType newInstance() {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static AccountType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static AccountType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static AccountType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static AccountType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static AccountType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static AccountType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static AccountType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static AccountType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static AccountType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static AccountType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static AccountType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static AccountType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static AccountType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static AccountType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static AccountType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static AccountType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static AccountType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (AccountType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
