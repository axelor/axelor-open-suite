/*
 * XML Type:  AttributedAccountType
 * Namespace: http://www.ebics.org/H003
 * Java type: AttributedAccountType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.AccountDescriptionType;
import com.axelor.apps.account.ebics.schema.h003.AccountHolderRoleType;
import com.axelor.apps.account.ebics.schema.h003.AccountNumberRoleType;
import com.axelor.apps.account.ebics.schema.h003.AttributedAccountType;
import com.axelor.apps.account.ebics.schema.h003.BankCodePrefixType;
import com.axelor.apps.account.ebics.schema.h003.BankCodeRoleType;
import com.axelor.apps.account.ebics.schema.h003.CurrencyBaseType;

/**
 * An XML AttributedAccountType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class AttributedAccountTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements AttributedAccountType
{
    private static final long serialVersionUID = 1L;
    
    public AttributedAccountTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ACCOUNTNUMBER$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "AccountNumber");
    private static final javax.xml.namespace.QName NATIONALACCOUNTNUMBER$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "NationalAccountNumber");
    private static final javax.xml.namespace.QName BANKCODE$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "BankCode");
    private static final javax.xml.namespace.QName NATIONALBANKCODE$6 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "NationalBankCode");
    private static final javax.xml.namespace.QName ACCOUNTHOLDER$8 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "AccountHolder");
    private static final javax.xml.namespace.QName CURRENCY$10 = 
        new javax.xml.namespace.QName("", "Currency");
    private static final javax.xml.namespace.QName DESCRIPTION$12 = 
        new javax.xml.namespace.QName("", "Description");
    
    
    /**
     * Gets array of all "AccountNumber" elements
     */
    public AttributedAccountType.AccountNumber[] getAccountNumberArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ACCOUNTNUMBER$0, targetList);
            AttributedAccountType.AccountNumber[] result = new AttributedAccountType.AccountNumber[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "AccountNumber" element
     */
    public AttributedAccountType.AccountNumber getAccountNumberArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.AccountNumber target = null;
            target = (AttributedAccountType.AccountNumber)get_store().find_element_user(ACCOUNTNUMBER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "AccountNumber" element
     */
    public int sizeOfAccountNumberArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ACCOUNTNUMBER$0);
        }
    }
    
    /**
     * Sets array of all "AccountNumber" element
     */
    public void setAccountNumberArray(AttributedAccountType.AccountNumber[] accountNumberArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(accountNumberArray, ACCOUNTNUMBER$0);
        }
    }
    
    /**
     * Sets ith "AccountNumber" element
     */
    public void setAccountNumberArray(int i, AttributedAccountType.AccountNumber accountNumber)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.AccountNumber target = null;
            target = (AttributedAccountType.AccountNumber)get_store().find_element_user(ACCOUNTNUMBER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(accountNumber);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "AccountNumber" element
     */
    public AttributedAccountType.AccountNumber insertNewAccountNumber(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.AccountNumber target = null;
            target = (AttributedAccountType.AccountNumber)get_store().insert_element_user(ACCOUNTNUMBER$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "AccountNumber" element
     */
    public AttributedAccountType.AccountNumber addNewAccountNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.AccountNumber target = null;
            target = (AttributedAccountType.AccountNumber)get_store().add_element_user(ACCOUNTNUMBER$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "AccountNumber" element
     */
    public void removeAccountNumber(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ACCOUNTNUMBER$0, i);
        }
    }
    
    /**
     * Gets array of all "NationalAccountNumber" elements
     */
    public AttributedAccountType.NationalAccountNumber[] getNationalAccountNumberArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(NATIONALACCOUNTNUMBER$2, targetList);
            AttributedAccountType.NationalAccountNumber[] result = new AttributedAccountType.NationalAccountNumber[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "NationalAccountNumber" element
     */
    public AttributedAccountType.NationalAccountNumber getNationalAccountNumberArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.NationalAccountNumber target = null;
            target = (AttributedAccountType.NationalAccountNumber)get_store().find_element_user(NATIONALACCOUNTNUMBER$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "NationalAccountNumber" element
     */
    public int sizeOfNationalAccountNumberArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(NATIONALACCOUNTNUMBER$2);
        }
    }
    
    /**
     * Sets array of all "NationalAccountNumber" element
     */
    public void setNationalAccountNumberArray(AttributedAccountType.NationalAccountNumber[] nationalAccountNumberArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(nationalAccountNumberArray, NATIONALACCOUNTNUMBER$2);
        }
    }
    
    /**
     * Sets ith "NationalAccountNumber" element
     */
    public void setNationalAccountNumberArray(int i, AttributedAccountType.NationalAccountNumber nationalAccountNumber)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.NationalAccountNumber target = null;
            target = (AttributedAccountType.NationalAccountNumber)get_store().find_element_user(NATIONALACCOUNTNUMBER$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(nationalAccountNumber);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "NationalAccountNumber" element
     */
    public AttributedAccountType.NationalAccountNumber insertNewNationalAccountNumber(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.NationalAccountNumber target = null;
            target = (AttributedAccountType.NationalAccountNumber)get_store().insert_element_user(NATIONALACCOUNTNUMBER$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "NationalAccountNumber" element
     */
    public AttributedAccountType.NationalAccountNumber addNewNationalAccountNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.NationalAccountNumber target = null;
            target = (AttributedAccountType.NationalAccountNumber)get_store().add_element_user(NATIONALACCOUNTNUMBER$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "NationalAccountNumber" element
     */
    public void removeNationalAccountNumber(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(NATIONALACCOUNTNUMBER$2, i);
        }
    }
    
    /**
     * Gets array of all "BankCode" elements
     */
    public AttributedAccountType.BankCode[] getBankCodeArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(BANKCODE$4, targetList);
            AttributedAccountType.BankCode[] result = new AttributedAccountType.BankCode[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "BankCode" element
     */
    public AttributedAccountType.BankCode getBankCodeArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.BankCode target = null;
            target = (AttributedAccountType.BankCode)get_store().find_element_user(BANKCODE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "BankCode" element
     */
    public int sizeOfBankCodeArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(BANKCODE$4);
        }
    }
    
    /**
     * Sets array of all "BankCode" element
     */
    public void setBankCodeArray(AttributedAccountType.BankCode[] bankCodeArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(bankCodeArray, BANKCODE$4);
        }
    }
    
    /**
     * Sets ith "BankCode" element
     */
    public void setBankCodeArray(int i, AttributedAccountType.BankCode bankCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.BankCode target = null;
            target = (AttributedAccountType.BankCode)get_store().find_element_user(BANKCODE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(bankCode);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "BankCode" element
     */
    public AttributedAccountType.BankCode insertNewBankCode(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.BankCode target = null;
            target = (AttributedAccountType.BankCode)get_store().insert_element_user(BANKCODE$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "BankCode" element
     */
    public AttributedAccountType.BankCode addNewBankCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.BankCode target = null;
            target = (AttributedAccountType.BankCode)get_store().add_element_user(BANKCODE$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "BankCode" element
     */
    public void removeBankCode(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(BANKCODE$4, i);
        }
    }
    
    /**
     * Gets array of all "NationalBankCode" elements
     */
    public AttributedAccountType.NationalBankCode[] getNationalBankCodeArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(NATIONALBANKCODE$6, targetList);
            AttributedAccountType.NationalBankCode[] result = new AttributedAccountType.NationalBankCode[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "NationalBankCode" element
     */
    public AttributedAccountType.NationalBankCode getNationalBankCodeArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.NationalBankCode target = null;
            target = (AttributedAccountType.NationalBankCode)get_store().find_element_user(NATIONALBANKCODE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "NationalBankCode" element
     */
    public int sizeOfNationalBankCodeArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(NATIONALBANKCODE$6);
        }
    }
    
    /**
     * Sets array of all "NationalBankCode" element
     */
    public void setNationalBankCodeArray(AttributedAccountType.NationalBankCode[] nationalBankCodeArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(nationalBankCodeArray, NATIONALBANKCODE$6);
        }
    }
    
    /**
     * Sets ith "NationalBankCode" element
     */
    public void setNationalBankCodeArray(int i, AttributedAccountType.NationalBankCode nationalBankCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.NationalBankCode target = null;
            target = (AttributedAccountType.NationalBankCode)get_store().find_element_user(NATIONALBANKCODE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(nationalBankCode);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "NationalBankCode" element
     */
    public AttributedAccountType.NationalBankCode insertNewNationalBankCode(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.NationalBankCode target = null;
            target = (AttributedAccountType.NationalBankCode)get_store().insert_element_user(NATIONALBANKCODE$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "NationalBankCode" element
     */
    public AttributedAccountType.NationalBankCode addNewNationalBankCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.NationalBankCode target = null;
            target = (AttributedAccountType.NationalBankCode)get_store().add_element_user(NATIONALBANKCODE$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "NationalBankCode" element
     */
    public void removeNationalBankCode(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(NATIONALBANKCODE$6, i);
        }
    }
    
    /**
     * Gets the "AccountHolder" element
     */
    public AttributedAccountType.AccountHolder getAccountHolder()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.AccountHolder target = null;
            target = (AttributedAccountType.AccountHolder)get_store().find_element_user(ACCOUNTHOLDER$8, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "AccountHolder" element
     */
    public boolean isSetAccountHolder()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ACCOUNTHOLDER$8) != 0;
        }
    }
    
    /**
     * Sets the "AccountHolder" element
     */
    public void setAccountHolder(AttributedAccountType.AccountHolder accountHolder)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.AccountHolder target = null;
            target = (AttributedAccountType.AccountHolder)get_store().find_element_user(ACCOUNTHOLDER$8, 0);
            if (target == null)
            {
                target = (AttributedAccountType.AccountHolder)get_store().add_element_user(ACCOUNTHOLDER$8);
            }
            target.set(accountHolder);
        }
    }
    
    /**
     * Appends and returns a new empty "AccountHolder" element
     */
    public AttributedAccountType.AccountHolder addNewAccountHolder()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AttributedAccountType.AccountHolder target = null;
            target = (AttributedAccountType.AccountHolder)get_store().add_element_user(ACCOUNTHOLDER$8);
            return target;
        }
    }
    
    /**
     * Unsets the "AccountHolder" element
     */
    public void unsetAccountHolder()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ACCOUNTHOLDER$8, 0);
        }
    }
    
    /**
     * Gets the "Currency" attribute
     */
    public java.lang.String getCurrency()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(CURRENCY$10);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_default_attribute_value(CURRENCY$10);
            }
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Currency" attribute
     */
    public CurrencyBaseType xgetCurrency()
    {
        synchronized (monitor())
        {
            check_orphaned();
            CurrencyBaseType target = null;
            target = (CurrencyBaseType)get_store().find_attribute_user(CURRENCY$10);
            if (target == null)
            {
                target = (CurrencyBaseType)get_default_attribute_value(CURRENCY$10);
            }
            return target;
        }
    }
    
    /**
     * True if has "Currency" attribute
     */
    public boolean isSetCurrency()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(CURRENCY$10) != null;
        }
    }
    
    /**
     * Sets the "Currency" attribute
     */
    public void setCurrency(java.lang.String currency)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(CURRENCY$10);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(CURRENCY$10);
            }
            target.setStringValue(currency);
        }
    }
    
    /**
     * Sets (as xml) the "Currency" attribute
     */
    public void xsetCurrency(CurrencyBaseType currency)
    {
        synchronized (monitor())
        {
            check_orphaned();
            CurrencyBaseType target = null;
            target = (CurrencyBaseType)get_store().find_attribute_user(CURRENCY$10);
            if (target == null)
            {
                target = (CurrencyBaseType)get_store().add_attribute_user(CURRENCY$10);
            }
            target.set(currency);
        }
    }
    
    /**
     * Unsets the "Currency" attribute
     */
    public void unsetCurrency()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(CURRENCY$10);
        }
    }
    
    /**
     * Gets the "Description" attribute
     */
    public java.lang.String getDescription()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$12);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Description" attribute
     */
    public AccountDescriptionType xgetDescription()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AccountDescriptionType target = null;
            target = (AccountDescriptionType)get_store().find_attribute_user(DESCRIPTION$12);
            return target;
        }
    }
    
    /**
     * True if has "Description" attribute
     */
    public boolean isSetDescription()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(DESCRIPTION$12) != null;
        }
    }
    
    /**
     * Sets the "Description" attribute
     */
    public void setDescription(java.lang.String description)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$12);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(DESCRIPTION$12);
            }
            target.setStringValue(description);
        }
    }
    
    /**
     * Sets (as xml) the "Description" attribute
     */
    public void xsetDescription(AccountDescriptionType description)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AccountDescriptionType target = null;
            target = (AccountDescriptionType)get_store().find_attribute_user(DESCRIPTION$12);
            if (target == null)
            {
                target = (AccountDescriptionType)get_store().add_attribute_user(DESCRIPTION$12);
            }
            target.set(description);
        }
    }
    
    /**
     * Unsets the "Description" attribute
     */
    public void unsetDescription()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(DESCRIPTION$12);
        }
    }
    /**
     * An XML AccountNumber(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of AttributedAccountType$AccountNumber.
     */
    public static class AccountNumberImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements AttributedAccountType.AccountNumber
    {
        private static final long serialVersionUID = 1L;
        
        public AccountNumberImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected AccountNumberImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName ROLE$0 = 
            new javax.xml.namespace.QName("", "Role");
        private static final javax.xml.namespace.QName DESCRIPTION$2 = 
            new javax.xml.namespace.QName("", "Description");
        private static final javax.xml.namespace.QName INTERNATIONAL$4 = 
            new javax.xml.namespace.QName("", "international");
        
        
        /**
         * Gets the "Role" attribute
         */
        public AccountNumberRoleType.Enum getRole()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    return null;
                }
                return (AccountNumberRoleType.Enum)target.getEnumValue();
            }
        }
        
        /**
         * Gets (as xml) the "Role" attribute
         */
        public AccountNumberRoleType xgetRole()
        {
            synchronized (monitor())
            {
                check_orphaned();
                AccountNumberRoleType target = null;
                target = (AccountNumberRoleType)get_store().find_attribute_user(ROLE$0);
                return target;
            }
        }
        
        /**
         * Sets the "Role" attribute
         */
        public void setRole(AccountNumberRoleType.Enum role)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ROLE$0);
                }
                target.setEnumValue(role);
            }
        }
        
        /**
         * Sets (as xml) the "Role" attribute
         */
        public void xsetRole(AccountNumberRoleType role)
        {
            synchronized (monitor())
            {
                check_orphaned();
                AccountNumberRoleType target = null;
                target = (AccountNumberRoleType)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    target = (AccountNumberRoleType)get_store().add_attribute_user(ROLE$0);
                }
                target.set(role);
            }
        }
        
        /**
         * Gets the "Description" attribute
         */
        public java.lang.String getDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "Description" attribute
         */
        public org.apache.xmlbeans.XmlNormalizedString xgetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlNormalizedString target = null;
                target = (org.apache.xmlbeans.XmlNormalizedString)get_store().find_attribute_user(DESCRIPTION$2);
                return target;
            }
        }
        
        /**
         * True if has "Description" attribute
         */
        public boolean isSetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(DESCRIPTION$2) != null;
            }
        }
        
        /**
         * Sets the "Description" attribute
         */
        public void setDescription(java.lang.String description)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(DESCRIPTION$2);
                }
                target.setStringValue(description);
            }
        }
        
        /**
         * Sets (as xml) the "Description" attribute
         */
        public void xsetDescription(org.apache.xmlbeans.XmlNormalizedString description)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlNormalizedString target = null;
                target = (org.apache.xmlbeans.XmlNormalizedString)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlNormalizedString)get_store().add_attribute_user(DESCRIPTION$2);
                }
                target.set(description);
            }
        }
        
        /**
         * Unsets the "Description" attribute
         */
        public void unsetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(DESCRIPTION$2);
            }
        }
        
        /**
         * Gets the "international" attribute
         */
        public boolean getInternational()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(INTERNATIONAL$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_default_attribute_value(INTERNATIONAL$4);
                }
                if (target == null)
                {
                    return false;
                }
                return target.getBooleanValue();
            }
        }
        
        /**
         * Gets (as xml) the "international" attribute
         */
        public org.apache.xmlbeans.XmlBoolean xgetInternational()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlBoolean target = null;
                target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(INTERNATIONAL$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlBoolean)get_default_attribute_value(INTERNATIONAL$4);
                }
                return target;
            }
        }
        
        /**
         * True if has "international" attribute
         */
        public boolean isSetInternational()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(INTERNATIONAL$4) != null;
            }
        }
        
        /**
         * Sets the "international" attribute
         */
        public void setInternational(boolean international)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(INTERNATIONAL$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(INTERNATIONAL$4);
                }
                target.setBooleanValue(international);
            }
        }
        
        /**
         * Sets (as xml) the "international" attribute
         */
        public void xsetInternational(org.apache.xmlbeans.XmlBoolean international)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlBoolean target = null;
                target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(INTERNATIONAL$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlBoolean)get_store().add_attribute_user(INTERNATIONAL$4);
                }
                target.set(international);
            }
        }
        
        /**
         * Unsets the "international" attribute
         */
        public void unsetInternational()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(INTERNATIONAL$4);
            }
        }
    }
    /**
     * An XML NationalAccountNumber(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of AttributedAccountType$NationalAccountNumber.
     */
    public static class NationalAccountNumberImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements AttributedAccountType.NationalAccountNumber
    {
        private static final long serialVersionUID = 1L;
        
        public NationalAccountNumberImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected NationalAccountNumberImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName ROLE$0 = 
            new javax.xml.namespace.QName("", "Role");
        private static final javax.xml.namespace.QName DESCRIPTION$2 = 
            new javax.xml.namespace.QName("", "Description");
        private static final javax.xml.namespace.QName FORMAT$4 = 
            new javax.xml.namespace.QName("", "format");
        
        
        /**
         * Gets the "Role" attribute
         */
        public AccountNumberRoleType.Enum getRole()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    return null;
                }
                return (AccountNumberRoleType.Enum)target.getEnumValue();
            }
        }
        
        /**
         * Gets (as xml) the "Role" attribute
         */
        public AccountNumberRoleType xgetRole()
        {
            synchronized (monitor())
            {
                check_orphaned();
                AccountNumberRoleType target = null;
                target = (AccountNumberRoleType)get_store().find_attribute_user(ROLE$0);
                return target;
            }
        }
        
        /**
         * Sets the "Role" attribute
         */
        public void setRole(AccountNumberRoleType.Enum role)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ROLE$0);
                }
                target.setEnumValue(role);
            }
        }
        
        /**
         * Sets (as xml) the "Role" attribute
         */
        public void xsetRole(AccountNumberRoleType role)
        {
            synchronized (monitor())
            {
                check_orphaned();
                AccountNumberRoleType target = null;
                target = (AccountNumberRoleType)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    target = (AccountNumberRoleType)get_store().add_attribute_user(ROLE$0);
                }
                target.set(role);
            }
        }
        
        /**
         * Gets the "Description" attribute
         */
        public java.lang.String getDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "Description" attribute
         */
        public org.apache.xmlbeans.XmlNormalizedString xgetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlNormalizedString target = null;
                target = (org.apache.xmlbeans.XmlNormalizedString)get_store().find_attribute_user(DESCRIPTION$2);
                return target;
            }
        }
        
        /**
         * True if has "Description" attribute
         */
        public boolean isSetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(DESCRIPTION$2) != null;
            }
        }
        
        /**
         * Sets the "Description" attribute
         */
        public void setDescription(java.lang.String description)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(DESCRIPTION$2);
                }
                target.setStringValue(description);
            }
        }
        
        /**
         * Sets (as xml) the "Description" attribute
         */
        public void xsetDescription(org.apache.xmlbeans.XmlNormalizedString description)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlNormalizedString target = null;
                target = (org.apache.xmlbeans.XmlNormalizedString)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlNormalizedString)get_store().add_attribute_user(DESCRIPTION$2);
                }
                target.set(description);
            }
        }
        
        /**
         * Unsets the "Description" attribute
         */
        public void unsetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(DESCRIPTION$2);
            }
        }
        
        /**
         * Gets the "format" attribute
         */
        public java.lang.String getFormat()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(FORMAT$4);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "format" attribute
         */
        public org.apache.xmlbeans.XmlToken xgetFormat()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlToken target = null;
                target = (org.apache.xmlbeans.XmlToken)get_store().find_attribute_user(FORMAT$4);
                return target;
            }
        }
        
        /**
         * Sets the "format" attribute
         */
        public void setFormat(java.lang.String format)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(FORMAT$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(FORMAT$4);
                }
                target.setStringValue(format);
            }
        }
        
        /**
         * Sets (as xml) the "format" attribute
         */
        public void xsetFormat(org.apache.xmlbeans.XmlToken format)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlToken target = null;
                target = (org.apache.xmlbeans.XmlToken)get_store().find_attribute_user(FORMAT$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlToken)get_store().add_attribute_user(FORMAT$4);
                }
                target.set(format);
            }
        }
    }
    /**
     * An XML BankCode(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of AttributedAccountType$BankCode.
     */
    public static class BankCodeImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements AttributedAccountType.BankCode
    {
        private static final long serialVersionUID = 1L;
        
        public BankCodeImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected BankCodeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName ROLE$0 = 
            new javax.xml.namespace.QName("", "Role");
        private static final javax.xml.namespace.QName DESCRIPTION$2 = 
            new javax.xml.namespace.QName("", "Description");
        private static final javax.xml.namespace.QName INTERNATIONAL$4 = 
            new javax.xml.namespace.QName("", "international");
        private static final javax.xml.namespace.QName PREFIX$6 = 
            new javax.xml.namespace.QName("", "Prefix");
        
        
        /**
         * Gets the "Role" attribute
         */
        public BankCodeRoleType.Enum getRole()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    return null;
                }
                return (BankCodeRoleType.Enum)target.getEnumValue();
            }
        }
        
        /**
         * Gets (as xml) the "Role" attribute
         */
        public BankCodeRoleType xgetRole()
        {
            synchronized (monitor())
            {
                check_orphaned();
                BankCodeRoleType target = null;
                target = (BankCodeRoleType)get_store().find_attribute_user(ROLE$0);
                return target;
            }
        }
        
        /**
         * Sets the "Role" attribute
         */
        public void setRole(BankCodeRoleType.Enum role)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ROLE$0);
                }
                target.setEnumValue(role);
            }
        }
        
        /**
         * Sets (as xml) the "Role" attribute
         */
        public void xsetRole(BankCodeRoleType role)
        {
            synchronized (monitor())
            {
                check_orphaned();
                BankCodeRoleType target = null;
                target = (BankCodeRoleType)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    target = (BankCodeRoleType)get_store().add_attribute_user(ROLE$0);
                }
                target.set(role);
            }
        }
        
        /**
         * Gets the "Description" attribute
         */
        public java.lang.String getDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "Description" attribute
         */
        public org.apache.xmlbeans.XmlNormalizedString xgetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlNormalizedString target = null;
                target = (org.apache.xmlbeans.XmlNormalizedString)get_store().find_attribute_user(DESCRIPTION$2);
                return target;
            }
        }
        
        /**
         * True if has "Description" attribute
         */
        public boolean isSetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(DESCRIPTION$2) != null;
            }
        }
        
        /**
         * Sets the "Description" attribute
         */
        public void setDescription(java.lang.String description)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(DESCRIPTION$2);
                }
                target.setStringValue(description);
            }
        }
        
        /**
         * Sets (as xml) the "Description" attribute
         */
        public void xsetDescription(org.apache.xmlbeans.XmlNormalizedString description)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlNormalizedString target = null;
                target = (org.apache.xmlbeans.XmlNormalizedString)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlNormalizedString)get_store().add_attribute_user(DESCRIPTION$2);
                }
                target.set(description);
            }
        }
        
        /**
         * Unsets the "Description" attribute
         */
        public void unsetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(DESCRIPTION$2);
            }
        }
        
        /**
         * Gets the "international" attribute
         */
        public boolean getInternational()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(INTERNATIONAL$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_default_attribute_value(INTERNATIONAL$4);
                }
                if (target == null)
                {
                    return false;
                }
                return target.getBooleanValue();
            }
        }
        
        /**
         * Gets (as xml) the "international" attribute
         */
        public org.apache.xmlbeans.XmlBoolean xgetInternational()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlBoolean target = null;
                target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(INTERNATIONAL$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlBoolean)get_default_attribute_value(INTERNATIONAL$4);
                }
                return target;
            }
        }
        
        /**
         * True if has "international" attribute
         */
        public boolean isSetInternational()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(INTERNATIONAL$4) != null;
            }
        }
        
        /**
         * Sets the "international" attribute
         */
        public void setInternational(boolean international)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(INTERNATIONAL$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(INTERNATIONAL$4);
                }
                target.setBooleanValue(international);
            }
        }
        
        /**
         * Sets (as xml) the "international" attribute
         */
        public void xsetInternational(org.apache.xmlbeans.XmlBoolean international)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlBoolean target = null;
                target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(INTERNATIONAL$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlBoolean)get_store().add_attribute_user(INTERNATIONAL$4);
                }
                target.set(international);
            }
        }
        
        /**
         * Unsets the "international" attribute
         */
        public void unsetInternational()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(INTERNATIONAL$4);
            }
        }
        
        /**
         * Gets the "Prefix" attribute
         */
        public java.lang.String getPrefix()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(PREFIX$6);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "Prefix" attribute
         */
        public BankCodePrefixType xgetPrefix()
        {
            synchronized (monitor())
            {
                check_orphaned();
                BankCodePrefixType target = null;
                target = (BankCodePrefixType)get_store().find_attribute_user(PREFIX$6);
                return target;
            }
        }
        
        /**
         * True if has "Prefix" attribute
         */
        public boolean isSetPrefix()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(PREFIX$6) != null;
            }
        }
        
        /**
         * Sets the "Prefix" attribute
         */
        public void setPrefix(java.lang.String prefix)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(PREFIX$6);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(PREFIX$6);
                }
                target.setStringValue(prefix);
            }
        }
        
        /**
         * Sets (as xml) the "Prefix" attribute
         */
        public void xsetPrefix(BankCodePrefixType prefix)
        {
            synchronized (monitor())
            {
                check_orphaned();
                BankCodePrefixType target = null;
                target = (BankCodePrefixType)get_store().find_attribute_user(PREFIX$6);
                if (target == null)
                {
                    target = (BankCodePrefixType)get_store().add_attribute_user(PREFIX$6);
                }
                target.set(prefix);
            }
        }
        
        /**
         * Unsets the "Prefix" attribute
         */
        public void unsetPrefix()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(PREFIX$6);
            }
        }
    }
    /**
     * An XML NationalBankCode(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of AttributedAccountType$NationalBankCode.
     */
    public static class NationalBankCodeImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements AttributedAccountType.NationalBankCode
    {
        private static final long serialVersionUID = 1L;
        
        public NationalBankCodeImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected NationalBankCodeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName ROLE$0 = 
            new javax.xml.namespace.QName("", "Role");
        private static final javax.xml.namespace.QName DESCRIPTION$2 = 
            new javax.xml.namespace.QName("", "Description");
        private static final javax.xml.namespace.QName FORMAT$4 = 
            new javax.xml.namespace.QName("", "format");
        
        
        /**
         * Gets the "Role" attribute
         */
        public BankCodeRoleType.Enum getRole()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    return null;
                }
                return (BankCodeRoleType.Enum)target.getEnumValue();
            }
        }
        
        /**
         * Gets (as xml) the "Role" attribute
         */
        public BankCodeRoleType xgetRole()
        {
            synchronized (monitor())
            {
                check_orphaned();
                BankCodeRoleType target = null;
                target = (BankCodeRoleType)get_store().find_attribute_user(ROLE$0);
                return target;
            }
        }
        
        /**
         * Sets the "Role" attribute
         */
        public void setRole(BankCodeRoleType.Enum role)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ROLE$0);
                }
                target.setEnumValue(role);
            }
        }
        
        /**
         * Sets (as xml) the "Role" attribute
         */
        public void xsetRole(BankCodeRoleType role)
        {
            synchronized (monitor())
            {
                check_orphaned();
                BankCodeRoleType target = null;
                target = (BankCodeRoleType)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    target = (BankCodeRoleType)get_store().add_attribute_user(ROLE$0);
                }
                target.set(role);
            }
        }
        
        /**
         * Gets the "Description" attribute
         */
        public java.lang.String getDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "Description" attribute
         */
        public org.apache.xmlbeans.XmlNormalizedString xgetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlNormalizedString target = null;
                target = (org.apache.xmlbeans.XmlNormalizedString)get_store().find_attribute_user(DESCRIPTION$2);
                return target;
            }
        }
        
        /**
         * True if has "Description" attribute
         */
        public boolean isSetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(DESCRIPTION$2) != null;
            }
        }
        
        /**
         * Sets the "Description" attribute
         */
        public void setDescription(java.lang.String description)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(DESCRIPTION$2);
                }
                target.setStringValue(description);
            }
        }
        
        /**
         * Sets (as xml) the "Description" attribute
         */
        public void xsetDescription(org.apache.xmlbeans.XmlNormalizedString description)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlNormalizedString target = null;
                target = (org.apache.xmlbeans.XmlNormalizedString)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlNormalizedString)get_store().add_attribute_user(DESCRIPTION$2);
                }
                target.set(description);
            }
        }
        
        /**
         * Unsets the "Description" attribute
         */
        public void unsetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(DESCRIPTION$2);
            }
        }
        
        /**
         * Gets the "format" attribute
         */
        public java.lang.String getFormat()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(FORMAT$4);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "format" attribute
         */
        public org.apache.xmlbeans.XmlToken xgetFormat()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlToken target = null;
                target = (org.apache.xmlbeans.XmlToken)get_store().find_attribute_user(FORMAT$4);
                return target;
            }
        }
        
        /**
         * Sets the "format" attribute
         */
        public void setFormat(java.lang.String format)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(FORMAT$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(FORMAT$4);
                }
                target.setStringValue(format);
            }
        }
        
        /**
         * Sets (as xml) the "format" attribute
         */
        public void xsetFormat(org.apache.xmlbeans.XmlToken format)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlToken target = null;
                target = (org.apache.xmlbeans.XmlToken)get_store().find_attribute_user(FORMAT$4);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlToken)get_store().add_attribute_user(FORMAT$4);
                }
                target.set(format);
            }
        }
    }
    /**
     * An XML AccountHolder(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of AttributedAccountType$AccountHolder.
     */
    public static class AccountHolderImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements AttributedAccountType.AccountHolder
    {
        private static final long serialVersionUID = 1L;
        
        public AccountHolderImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected AccountHolderImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName ROLE$0 = 
            new javax.xml.namespace.QName("", "Role");
        private static final javax.xml.namespace.QName DESCRIPTION$2 = 
            new javax.xml.namespace.QName("", "Description");
        
        
        /**
         * Gets the "Role" attribute
         */
        public AccountHolderRoleType.Enum getRole()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    return null;
                }
                return (AccountHolderRoleType.Enum)target.getEnumValue();
            }
        }
        
        /**
         * Gets (as xml) the "Role" attribute
         */
        public AccountHolderRoleType xgetRole()
        {
            synchronized (monitor())
            {
                check_orphaned();
                AccountHolderRoleType target = null;
                target = (AccountHolderRoleType)get_store().find_attribute_user(ROLE$0);
                return target;
            }
        }
        
        /**
         * Sets the "Role" attribute
         */
        public void setRole(AccountHolderRoleType.Enum role)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ROLE$0);
                }
                target.setEnumValue(role);
            }
        }
        
        /**
         * Sets (as xml) the "Role" attribute
         */
        public void xsetRole(AccountHolderRoleType role)
        {
            synchronized (monitor())
            {
                check_orphaned();
                AccountHolderRoleType target = null;
                target = (AccountHolderRoleType)get_store().find_attribute_user(ROLE$0);
                if (target == null)
                {
                    target = (AccountHolderRoleType)get_store().add_attribute_user(ROLE$0);
                }
                target.set(role);
            }
        }
        
        /**
         * Gets the "Description" attribute
         */
        public java.lang.String getDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "Description" attribute
         */
        public org.apache.xmlbeans.XmlNormalizedString xgetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlNormalizedString target = null;
                target = (org.apache.xmlbeans.XmlNormalizedString)get_store().find_attribute_user(DESCRIPTION$2);
                return target;
            }
        }
        
        /**
         * True if has "Description" attribute
         */
        public boolean isSetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(DESCRIPTION$2) != null;
            }
        }
        
        /**
         * Sets the "Description" attribute
         */
        public void setDescription(java.lang.String description)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(DESCRIPTION$2);
                }
                target.setStringValue(description);
            }
        }
        
        /**
         * Sets (as xml) the "Description" attribute
         */
        public void xsetDescription(org.apache.xmlbeans.XmlNormalizedString description)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlNormalizedString target = null;
                target = (org.apache.xmlbeans.XmlNormalizedString)get_store().find_attribute_user(DESCRIPTION$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlNormalizedString)get_store().add_attribute_user(DESCRIPTION$2);
                }
                target.set(description);
            }
        }
        
        /**
         * Unsets the "Description" attribute
         */
        public void unsetDescription()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(DESCRIPTION$2);
            }
        }
    }
}
