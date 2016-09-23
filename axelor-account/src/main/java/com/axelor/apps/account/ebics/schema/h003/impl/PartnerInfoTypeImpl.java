/*
 * XML Type:  PartnerInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: PartnerInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.AccountIDType;
import com.axelor.apps.account.ebics.schema.h003.AddressInfoType;
import com.axelor.apps.account.ebics.schema.h003.AuthOrderInfoType;
import com.axelor.apps.account.ebics.schema.h003.BankInfoType;
import com.axelor.apps.account.ebics.schema.h003.OrderTListType;
import com.axelor.apps.account.ebics.schema.h003.PartnerInfoType;

/**
 * An XML PartnerInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class PartnerInfoTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements PartnerInfoType
{
    private static final long serialVersionUID = 1L;
    
    public PartnerInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ADDRESSINFO$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "AddressInfo");
    private static final javax.xml.namespace.QName BANKINFO$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "BankInfo");
    private static final javax.xml.namespace.QName ACCOUNTINFO$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "AccountInfo");
    private static final javax.xml.namespace.QName ORDERINFO$6 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderInfo");
    
    
    /**
     * Gets the "AddressInfo" element
     */
    public AddressInfoType getAddressInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AddressInfoType target = null;
            target = (AddressInfoType)get_store().find_element_user(ADDRESSINFO$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "AddressInfo" element
     */
    public void setAddressInfo(AddressInfoType addressInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AddressInfoType target = null;
            target = (AddressInfoType)get_store().find_element_user(ADDRESSINFO$0, 0);
            if (target == null)
            {
                target = (AddressInfoType)get_store().add_element_user(ADDRESSINFO$0);
            }
            target.set(addressInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "AddressInfo" element
     */
    public AddressInfoType addNewAddressInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AddressInfoType target = null;
            target = (AddressInfoType)get_store().add_element_user(ADDRESSINFO$0);
            return target;
        }
    }
    
    /**
     * Gets the "BankInfo" element
     */
    public BankInfoType getBankInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            BankInfoType target = null;
            target = (BankInfoType)get_store().find_element_user(BANKINFO$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "BankInfo" element
     */
    public void setBankInfo(BankInfoType bankInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            BankInfoType target = null;
            target = (BankInfoType)get_store().find_element_user(BANKINFO$2, 0);
            if (target == null)
            {
                target = (BankInfoType)get_store().add_element_user(BANKINFO$2);
            }
            target.set(bankInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "BankInfo" element
     */
    public BankInfoType addNewBankInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            BankInfoType target = null;
            target = (BankInfoType)get_store().add_element_user(BANKINFO$2);
            return target;
        }
    }
    
    /**
     * Gets array of all "AccountInfo" elements
     */
    public PartnerInfoType.AccountInfo[] getAccountInfoArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ACCOUNTINFO$4, targetList);
            PartnerInfoType.AccountInfo[] result = new PartnerInfoType.AccountInfo[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "AccountInfo" element
     */
    public PartnerInfoType.AccountInfo getAccountInfoArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            PartnerInfoType.AccountInfo target = null;
            target = (PartnerInfoType.AccountInfo)get_store().find_element_user(ACCOUNTINFO$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "AccountInfo" element
     */
    public int sizeOfAccountInfoArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ACCOUNTINFO$4);
        }
    }
    
    /**
     * Sets array of all "AccountInfo" element
     */
    public void setAccountInfoArray(PartnerInfoType.AccountInfo[] accountInfoArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(accountInfoArray, ACCOUNTINFO$4);
        }
    }
    
    /**
     * Sets ith "AccountInfo" element
     */
    public void setAccountInfoArray(int i, PartnerInfoType.AccountInfo accountInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            PartnerInfoType.AccountInfo target = null;
            target = (PartnerInfoType.AccountInfo)get_store().find_element_user(ACCOUNTINFO$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(accountInfo);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "AccountInfo" element
     */
    public PartnerInfoType.AccountInfo insertNewAccountInfo(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            PartnerInfoType.AccountInfo target = null;
            target = (PartnerInfoType.AccountInfo)get_store().insert_element_user(ACCOUNTINFO$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "AccountInfo" element
     */
    public PartnerInfoType.AccountInfo addNewAccountInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            PartnerInfoType.AccountInfo target = null;
            target = (PartnerInfoType.AccountInfo)get_store().add_element_user(ACCOUNTINFO$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "AccountInfo" element
     */
    public void removeAccountInfo(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ACCOUNTINFO$4, i);
        }
    }
    
    /**
     * Gets array of all "OrderInfo" elements
     */
    public AuthOrderInfoType[] getOrderInfoArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ORDERINFO$6, targetList);
            AuthOrderInfoType[] result = new AuthOrderInfoType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "OrderInfo" element
     */
    public AuthOrderInfoType getOrderInfoArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AuthOrderInfoType target = null;
            target = (AuthOrderInfoType)get_store().find_element_user(ORDERINFO$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "OrderInfo" element
     */
    public int sizeOfOrderInfoArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ORDERINFO$6);
        }
    }
    
    /**
     * Sets array of all "OrderInfo" element
     */
    public void setOrderInfoArray(AuthOrderInfoType[] orderInfoArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(orderInfoArray, ORDERINFO$6);
        }
    }
    
    /**
     * Sets ith "OrderInfo" element
     */
    public void setOrderInfoArray(int i, AuthOrderInfoType orderInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AuthOrderInfoType target = null;
            target = (AuthOrderInfoType)get_store().find_element_user(ORDERINFO$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(orderInfo);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "OrderInfo" element
     */
    public AuthOrderInfoType insertNewOrderInfo(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AuthOrderInfoType target = null;
            target = (AuthOrderInfoType)get_store().insert_element_user(ORDERINFO$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "OrderInfo" element
     */
    public AuthOrderInfoType addNewOrderInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AuthOrderInfoType target = null;
            target = (AuthOrderInfoType)get_store().add_element_user(ORDERINFO$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "OrderInfo" element
     */
    public void removeOrderInfo(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ORDERINFO$6, i);
        }
    }
    /**
     * An XML AccountInfo(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public static class AccountInfoImpl extends AccountTypeImpl implements PartnerInfoType.AccountInfo
    {
        private static final long serialVersionUID = 1L;
        
        public AccountInfoImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName USAGEORDERTYPES$0 = 
            new javax.xml.namespace.QName("http://www.ebics.org/H003", "UsageOrderTypes");
        private static final javax.xml.namespace.QName ID$2 = 
            new javax.xml.namespace.QName("", "ID");
        
        
        /**
         * Gets the "UsageOrderTypes" element
         */
        public java.util.List getUsageOrderTypes()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USAGEORDERTYPES$0, 0);
                if (target == null)
                {
                    return null;
                }
                return target.getListValue();
            }
        }
        
        /**
         * Gets (as xml) the "UsageOrderTypes" element
         */
        public OrderTListType xgetUsageOrderTypes()
        {
            synchronized (monitor())
            {
                check_orphaned();
                OrderTListType target = null;
                target = (OrderTListType)get_store().find_element_user(USAGEORDERTYPES$0, 0);
                return target;
            }
        }
        
        /**
         * True if has "UsageOrderTypes" element
         */
        public boolean isSetUsageOrderTypes()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(USAGEORDERTYPES$0) != 0;
            }
        }
        
        /**
         * Sets the "UsageOrderTypes" element
         */
        public void setUsageOrderTypes(java.util.List usageOrderTypes)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USAGEORDERTYPES$0, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(USAGEORDERTYPES$0);
                }
                target.setListValue(usageOrderTypes);
            }
        }
        
        /**
         * Sets (as xml) the "UsageOrderTypes" element
         */
        public void xsetUsageOrderTypes(OrderTListType usageOrderTypes)
        {
            synchronized (monitor())
            {
                check_orphaned();
                OrderTListType target = null;
                target = (OrderTListType)get_store().find_element_user(USAGEORDERTYPES$0, 0);
                if (target == null)
                {
                    target = (OrderTListType)get_store().add_element_user(USAGEORDERTYPES$0);
                }
                target.set(usageOrderTypes);
            }
        }
        
        /**
         * Unsets the "UsageOrderTypes" element
         */
        public void unsetUsageOrderTypes()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(USAGEORDERTYPES$0, 0);
            }
        }
        
        /**
         * Gets the "ID" attribute
         */
        public java.lang.String getID()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$2);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "ID" attribute
         */
        public AccountIDType xgetID()
        {
            synchronized (monitor())
            {
                check_orphaned();
                AccountIDType target = null;
                target = (AccountIDType)get_store().find_attribute_user(ID$2);
                return target;
            }
        }
        
        /**
         * Sets the "ID" attribute
         */
        public void setID(java.lang.String id)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ID$2);
                }
                target.setStringValue(id);
            }
        }
        
        /**
         * Sets (as xml) the "ID" attribute
         */
        public void xsetID(AccountIDType id)
        {
            synchronized (monitor())
            {
                check_orphaned();
                AccountIDType target = null;
                target = (AccountIDType)get_store().find_attribute_user(ID$2);
                if (target == null)
                {
                    target = (AccountIDType)get_store().add_attribute_user(ID$2);
                }
                target.set(id);
            }
        }
    }
}
