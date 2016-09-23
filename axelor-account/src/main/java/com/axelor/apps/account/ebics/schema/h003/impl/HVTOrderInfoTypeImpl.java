/*
 * XML Type:  HVTOrderInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVTOrderInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.CurrencyBaseType;
import com.axelor.apps.account.ebics.schema.h003.HVTAccountInfoType;
import com.axelor.apps.account.ebics.schema.h003.HVTOrderInfoType;
import com.axelor.apps.account.ebics.schema.h003.OrderFormatType;

/**
 * An XML HVTOrderInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class HVTOrderInfoTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements HVTOrderInfoType
{
    private static final long serialVersionUID = 1L;
    
    public HVTOrderInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ORDERFORMAT$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderFormat");
    private static final javax.xml.namespace.QName ACCOUNTINFO$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "AccountInfo");
    private static final javax.xml.namespace.QName EXECUTIONDATE$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "ExecutionDate");
    private static final javax.xml.namespace.QName AMOUNT$6 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Amount");
    private static final javax.xml.namespace.QName DESCRIPTION$8 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Description");
    
    
    /**
     * Gets the "OrderFormat" element
     */
    public java.lang.String getOrderFormat()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERFORMAT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "OrderFormat" element
     */
    public OrderFormatType xgetOrderFormat()
    {
        synchronized (monitor())
        {
            check_orphaned();
            OrderFormatType target = null;
            target = (OrderFormatType)get_store().find_element_user(ORDERFORMAT$0, 0);
            return target;
        }
    }
    
    /**
     * True if has "OrderFormat" element
     */
    public boolean isSetOrderFormat()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ORDERFORMAT$0) != 0;
        }
    }
    
    /**
     * Sets the "OrderFormat" element
     */
    public void setOrderFormat(java.lang.String orderFormat)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERFORMAT$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ORDERFORMAT$0);
            }
            target.setStringValue(orderFormat);
        }
    }
    
    /**
     * Sets (as xml) the "OrderFormat" element
     */
    public void xsetOrderFormat(OrderFormatType orderFormat)
    {
        synchronized (monitor())
        {
            check_orphaned();
            OrderFormatType target = null;
            target = (OrderFormatType)get_store().find_element_user(ORDERFORMAT$0, 0);
            if (target == null)
            {
                target = (OrderFormatType)get_store().add_element_user(ORDERFORMAT$0);
            }
            target.set(orderFormat);
        }
    }
    
    /**
     * Unsets the "OrderFormat" element
     */
    public void unsetOrderFormat()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ORDERFORMAT$0, 0);
        }
    }
    
    /**
     * Gets array of all "AccountInfo" elements
     */
    public HVTAccountInfoType[] getAccountInfoArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ACCOUNTINFO$2, targetList);
            HVTAccountInfoType[] result = new HVTAccountInfoType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "AccountInfo" element
     */
    public HVTAccountInfoType getAccountInfoArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTAccountInfoType target = null;
            target = (HVTAccountInfoType)get_store().find_element_user(ACCOUNTINFO$2, i);
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
            return get_store().count_elements(ACCOUNTINFO$2);
        }
    }
    
    /**
     * Sets array of all "AccountInfo" element
     */
    public void setAccountInfoArray(HVTAccountInfoType[] accountInfoArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(accountInfoArray, ACCOUNTINFO$2);
        }
    }
    
    /**
     * Sets ith "AccountInfo" element
     */
    public void setAccountInfoArray(int i, HVTAccountInfoType accountInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTAccountInfoType target = null;
            target = (HVTAccountInfoType)get_store().find_element_user(ACCOUNTINFO$2, i);
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
    public HVTAccountInfoType insertNewAccountInfo(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTAccountInfoType target = null;
            target = (HVTAccountInfoType)get_store().insert_element_user(ACCOUNTINFO$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "AccountInfo" element
     */
    public HVTAccountInfoType addNewAccountInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTAccountInfoType target = null;
            target = (HVTAccountInfoType)get_store().add_element_user(ACCOUNTINFO$2);
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
            get_store().remove_element(ACCOUNTINFO$2, i);
        }
    }
    
    /**
     * Gets the "ExecutionDate" element
     */
    public HVTOrderInfoType.ExecutionDate getExecutionDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderInfoType.ExecutionDate target = null;
            target = (HVTOrderInfoType.ExecutionDate)get_store().find_element_user(EXECUTIONDATE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "ExecutionDate" element
     */
    public boolean isSetExecutionDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EXECUTIONDATE$4) != 0;
        }
    }
    
    /**
     * Sets the "ExecutionDate" element
     */
    public void setExecutionDate(HVTOrderInfoType.ExecutionDate executionDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderInfoType.ExecutionDate target = null;
            target = (HVTOrderInfoType.ExecutionDate)get_store().find_element_user(EXECUTIONDATE$4, 0);
            if (target == null)
            {
                target = (HVTOrderInfoType.ExecutionDate)get_store().add_element_user(EXECUTIONDATE$4);
            }
            target.set(executionDate);
        }
    }
    
    /**
     * Appends and returns a new empty "ExecutionDate" element
     */
    public HVTOrderInfoType.ExecutionDate addNewExecutionDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderInfoType.ExecutionDate target = null;
            target = (HVTOrderInfoType.ExecutionDate)get_store().add_element_user(EXECUTIONDATE$4);
            return target;
        }
    }
    
    /**
     * Unsets the "ExecutionDate" element
     */
    public void unsetExecutionDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EXECUTIONDATE$4, 0);
        }
    }
    
    /**
     * Gets the "Amount" element
     */
    public HVTOrderInfoType.Amount getAmount()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderInfoType.Amount target = null;
            target = (HVTOrderInfoType.Amount)get_store().find_element_user(AMOUNT$6, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "Amount" element
     */
    public void setAmount(HVTOrderInfoType.Amount amount)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderInfoType.Amount target = null;
            target = (HVTOrderInfoType.Amount)get_store().find_element_user(AMOUNT$6, 0);
            if (target == null)
            {
                target = (HVTOrderInfoType.Amount)get_store().add_element_user(AMOUNT$6);
            }
            target.set(amount);
        }
    }
    
    /**
     * Appends and returns a new empty "Amount" element
     */
    public HVTOrderInfoType.Amount addNewAmount()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderInfoType.Amount target = null;
            target = (HVTOrderInfoType.Amount)get_store().add_element_user(AMOUNT$6);
            return target;
        }
    }
    
    /**
     * Gets array of all "Description" elements
     */
    public HVTOrderInfoType.Description[] getDescriptionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(DESCRIPTION$8, targetList);
            HVTOrderInfoType.Description[] result = new HVTOrderInfoType.Description[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "Description" element
     */
    public HVTOrderInfoType.Description getDescriptionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderInfoType.Description target = null;
            target = (HVTOrderInfoType.Description)get_store().find_element_user(DESCRIPTION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "Description" element
     */
    public int sizeOfDescriptionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(DESCRIPTION$8);
        }
    }
    
    /**
     * Sets array of all "Description" element
     */
    public void setDescriptionArray(HVTOrderInfoType.Description[] descriptionArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(descriptionArray, DESCRIPTION$8);
        }
    }
    
    /**
     * Sets ith "Description" element
     */
    public void setDescriptionArray(int i, HVTOrderInfoType.Description description)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderInfoType.Description target = null;
            target = (HVTOrderInfoType.Description)get_store().find_element_user(DESCRIPTION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(description);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "Description" element
     */
    public HVTOrderInfoType.Description insertNewDescription(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderInfoType.Description target = null;
            target = (HVTOrderInfoType.Description)get_store().insert_element_user(DESCRIPTION$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "Description" element
     */
    public HVTOrderInfoType.Description addNewDescription()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderInfoType.Description target = null;
            target = (HVTOrderInfoType.Description)get_store().add_element_user(DESCRIPTION$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "Description" element
     */
    public void removeDescription(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(DESCRIPTION$8, i);
        }
    }
    /**
     * An XML ExecutionDate(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of HVTOrderInfoType$ExecutionDate.
     */
    public static class ExecutionDateImpl extends org.apache.xmlbeans.impl.values.JavaGDateHolderEx implements HVTOrderInfoType.ExecutionDate
    {
        private static final long serialVersionUID = 1L;
        
        public ExecutionDateImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected ExecutionDateImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        
    }
    /**
     * An XML Amount(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of HVTOrderInfoType$Amount.
     */
    public static class AmountImpl extends org.apache.xmlbeans.impl.values.JavaDecimalHolderEx implements HVTOrderInfoType.Amount
    {
        private static final long serialVersionUID = 1L;
        
        public AmountImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected AmountImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName ISCREDIT$0 = 
            new javax.xml.namespace.QName("", "isCredit");
        private static final javax.xml.namespace.QName CURRENCY$2 = 
            new javax.xml.namespace.QName("", "Currency");
        
        
        /**
         * Gets the "isCredit" attribute
         */
        public boolean getIsCredit()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ISCREDIT$0);
                if (target == null)
                {
                    return false;
                }
                return target.getBooleanValue();
            }
        }
        
        /**
         * Gets (as xml) the "isCredit" attribute
         */
        public org.apache.xmlbeans.XmlBoolean xgetIsCredit()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlBoolean target = null;
                target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(ISCREDIT$0);
                return target;
            }
        }
        
        /**
         * True if has "isCredit" attribute
         */
        public boolean isSetIsCredit()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(ISCREDIT$0) != null;
            }
        }
        
        /**
         * Sets the "isCredit" attribute
         */
        public void setIsCredit(boolean isCredit)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ISCREDIT$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ISCREDIT$0);
                }
                target.setBooleanValue(isCredit);
            }
        }
        
        /**
         * Sets (as xml) the "isCredit" attribute
         */
        public void xsetIsCredit(org.apache.xmlbeans.XmlBoolean isCredit)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlBoolean target = null;
                target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(ISCREDIT$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlBoolean)get_store().add_attribute_user(ISCREDIT$0);
                }
                target.set(isCredit);
            }
        }
        
        /**
         * Unsets the "isCredit" attribute
         */
        public void unsetIsCredit()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(ISCREDIT$0);
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
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(CURRENCY$2);
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
                target = (CurrencyBaseType)get_store().find_attribute_user(CURRENCY$2);
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
                return get_store().find_attribute_user(CURRENCY$2) != null;
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
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(CURRENCY$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(CURRENCY$2);
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
                target = (CurrencyBaseType)get_store().find_attribute_user(CURRENCY$2);
                if (target == null)
                {
                    target = (CurrencyBaseType)get_store().add_attribute_user(CURRENCY$2);
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
                get_store().remove_attribute(CURRENCY$2);
            }
        }
    }
    /**
     * An XML Description(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of HVTOrderInfoType$Description.
     */
    public static class DescriptionImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements HVTOrderInfoType.Description
    {
        private static final long serialVersionUID = 1L;
        
        public DescriptionImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected DescriptionImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName TYPE$0 = 
            new javax.xml.namespace.QName("", "Type");
        
        
        /**
         * Gets the "Type" attribute
         */
        public HVTOrderInfoType.Description.Type.Enum getType()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$0);
                if (target == null)
                {
                    return null;
                }
                return (HVTOrderInfoType.Description.Type.Enum)target.getEnumValue();
            }
        }
        
        /**
         * Gets (as xml) the "Type" attribute
         */
        public HVTOrderInfoType.Description.Type xgetType()
        {
            synchronized (monitor())
            {
                check_orphaned();
                HVTOrderInfoType.Description.Type target = null;
                target = (HVTOrderInfoType.Description.Type)get_store().find_attribute_user(TYPE$0);
                return target;
            }
        }
        
        /**
         * Sets the "Type" attribute
         */
        public void setType(HVTOrderInfoType.Description.Type.Enum type)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TYPE$0);
                }
                target.setEnumValue(type);
            }
        }
        
        /**
         * Sets (as xml) the "Type" attribute
         */
        public void xsetType(HVTOrderInfoType.Description.Type type)
        {
            synchronized (monitor())
            {
                check_orphaned();
                HVTOrderInfoType.Description.Type target = null;
                target = (HVTOrderInfoType.Description.Type)get_store().find_attribute_user(TYPE$0);
                if (target == null)
                {
                    target = (HVTOrderInfoType.Description.Type)get_store().add_attribute_user(TYPE$0);
                }
                target.set(type);
            }
        }
        /**
         * An XML Type(@).
         *
         * This is an atomic type that is a restriction of HVTOrderInfoType$Description$Type.
         */
        public static class TypeImpl extends org.apache.xmlbeans.impl.values.JavaStringEnumerationHolderEx implements HVTOrderInfoType.Description.Type
        {
            private static final long serialVersionUID = 1L;
            
            public TypeImpl(org.apache.xmlbeans.SchemaType sType)
            {
                super(sType, false);
            }
            
            protected TypeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
            {
                super(sType, b);
            }
        }
    }
}
