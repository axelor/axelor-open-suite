/*
 * XML Type:  DataTransferResponseType
 * Namespace: http://www.ebics.org/H003
 * Java type: DataTransferResponseType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.DataTransferResponseType;

/**
 * An XML DataTransferResponseType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class DataTransferResponseTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements DataTransferResponseType
{
    private static final long serialVersionUID = 1L;
    
    public DataTransferResponseTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName DATAENCRYPTIONINFO$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "DataEncryptionInfo");
    private static final javax.xml.namespace.QName ORDERDATA$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderData");
    
    
    /**
     * Gets the "DataEncryptionInfo" element
     */
    public DataTransferResponseType.DataEncryptionInfo getDataEncryptionInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferResponseType.DataEncryptionInfo target = null;
            target = (DataTransferResponseType.DataEncryptionInfo)get_store().find_element_user(DATAENCRYPTIONINFO$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "DataEncryptionInfo" element
     */
    public boolean isSetDataEncryptionInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(DATAENCRYPTIONINFO$0) != 0;
        }
    }
    
    /**
     * Sets the "DataEncryptionInfo" element
     */
    public void setDataEncryptionInfo(DataTransferResponseType.DataEncryptionInfo dataEncryptionInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferResponseType.DataEncryptionInfo target = null;
            target = (DataTransferResponseType.DataEncryptionInfo)get_store().find_element_user(DATAENCRYPTIONINFO$0, 0);
            if (target == null)
            {
                target = (DataTransferResponseType.DataEncryptionInfo)get_store().add_element_user(DATAENCRYPTIONINFO$0);
            }
            target.set(dataEncryptionInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "DataEncryptionInfo" element
     */
    public DataTransferResponseType.DataEncryptionInfo addNewDataEncryptionInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferResponseType.DataEncryptionInfo target = null;
            target = (DataTransferResponseType.DataEncryptionInfo)get_store().add_element_user(DATAENCRYPTIONINFO$0);
            return target;
        }
    }
    
    /**
     * Unsets the "DataEncryptionInfo" element
     */
    public void unsetDataEncryptionInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(DATAENCRYPTIONINFO$0, 0);
        }
    }
    
    /**
     * Gets the "OrderData" element
     */
    public DataTransferResponseType.OrderData getOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferResponseType.OrderData target = null;
            target = (DataTransferResponseType.OrderData)get_store().find_element_user(ORDERDATA$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "OrderData" element
     */
    public void setOrderData(DataTransferResponseType.OrderData orderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferResponseType.OrderData target = null;
            target = (DataTransferResponseType.OrderData)get_store().find_element_user(ORDERDATA$2, 0);
            if (target == null)
            {
                target = (DataTransferResponseType.OrderData)get_store().add_element_user(ORDERDATA$2);
            }
            target.set(orderData);
        }
    }
    
    /**
     * Appends and returns a new empty "OrderData" element
     */
    public DataTransferResponseType.OrderData addNewOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferResponseType.OrderData target = null;
            target = (DataTransferResponseType.OrderData)get_store().add_element_user(ORDERDATA$2);
            return target;
        }
    }
    /**
     * An XML DataEncryptionInfo(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public static class DataEncryptionInfoImpl extends DataEncryptionInfoTypeImpl implements DataTransferResponseType.DataEncryptionInfo
    {
        private static final long serialVersionUID = 1L;
        
        public DataEncryptionInfoImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName AUTHENTICATE$0 = 
            new javax.xml.namespace.QName("", "authenticate");
        
        
        /**
         * Gets the "authenticate" attribute
         */
        public boolean getAuthenticate()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHENTICATE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_default_attribute_value(AUTHENTICATE$0);
                }
                if (target == null)
                {
                    return false;
                }
                return target.getBooleanValue();
            }
        }
        
        /**
         * Gets (as xml) the "authenticate" attribute
         */
        public org.apache.xmlbeans.XmlBoolean xgetAuthenticate()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlBoolean target = null;
                target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(AUTHENTICATE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlBoolean)get_default_attribute_value(AUTHENTICATE$0);
                }
                return target;
            }
        }
        
        /**
         * Sets the "authenticate" attribute
         */
        public void setAuthenticate(boolean authenticate)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHENTICATE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(AUTHENTICATE$0);
                }
                target.setBooleanValue(authenticate);
            }
        }
        
        /**
         * Sets (as xml) the "authenticate" attribute
         */
        public void xsetAuthenticate(org.apache.xmlbeans.XmlBoolean authenticate)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlBoolean target = null;
                target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(AUTHENTICATE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlBoolean)get_store().add_attribute_user(AUTHENTICATE$0);
                }
                target.set(authenticate);
            }
        }
    }
    /**
     * An XML OrderData(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of DataTransferResponseType$OrderData.
     */
    public static class OrderDataImpl extends org.apache.xmlbeans.impl.values.JavaBase64HolderEx implements DataTransferResponseType.OrderData
    {
        private static final long serialVersionUID = 1L;
        
        public OrderDataImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected OrderDataImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        
    }
}
