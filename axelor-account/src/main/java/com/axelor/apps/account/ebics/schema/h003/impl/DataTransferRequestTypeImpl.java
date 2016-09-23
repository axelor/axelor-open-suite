/*
 * XML Type:  DataTransferRequestType
 * Namespace: http://www.ebics.org/H003
 * Java type: DataTransferRequestType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType;

/**
 * An XML DataTransferRequestType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class DataTransferRequestTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements DataTransferRequestType
{
    private static final long serialVersionUID = 1L;
    
    public DataTransferRequestTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName DATAENCRYPTIONINFO$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "DataEncryptionInfo");
    private static final javax.xml.namespace.QName SIGNATUREDATA$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "SignatureData");
    private static final javax.xml.namespace.QName ORDERDATA$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderData");
    
    
    /**
     * Gets the "DataEncryptionInfo" element
     */
    public DataTransferRequestType.DataEncryptionInfo getDataEncryptionInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferRequestType.DataEncryptionInfo target = null;
            target = (DataTransferRequestType.DataEncryptionInfo)get_store().find_element_user(DATAENCRYPTIONINFO$0, 0);
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
    public void setDataEncryptionInfo(DataTransferRequestType.DataEncryptionInfo dataEncryptionInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferRequestType.DataEncryptionInfo target = null;
            target = (DataTransferRequestType.DataEncryptionInfo)get_store().find_element_user(DATAENCRYPTIONINFO$0, 0);
            if (target == null)
            {
                target = (DataTransferRequestType.DataEncryptionInfo)get_store().add_element_user(DATAENCRYPTIONINFO$0);
            }
            target.set(dataEncryptionInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "DataEncryptionInfo" element
     */
    public DataTransferRequestType.DataEncryptionInfo addNewDataEncryptionInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferRequestType.DataEncryptionInfo target = null;
            target = (DataTransferRequestType.DataEncryptionInfo)get_store().add_element_user(DATAENCRYPTIONINFO$0);
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
     * Gets the "SignatureData" element
     */
    public DataTransferRequestType.SignatureData getSignatureData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferRequestType.SignatureData target = null;
            target = (DataTransferRequestType.SignatureData)get_store().find_element_user(SIGNATUREDATA$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "SignatureData" element
     */
    public boolean isSetSignatureData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNATUREDATA$2) != 0;
        }
    }
    
    /**
     * Sets the "SignatureData" element
     */
    public void setSignatureData(DataTransferRequestType.SignatureData signatureData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferRequestType.SignatureData target = null;
            target = (DataTransferRequestType.SignatureData)get_store().find_element_user(SIGNATUREDATA$2, 0);
            if (target == null)
            {
                target = (DataTransferRequestType.SignatureData)get_store().add_element_user(SIGNATUREDATA$2);
            }
            target.set(signatureData);
        }
    }
    
    /**
     * Appends and returns a new empty "SignatureData" element
     */
    public DataTransferRequestType.SignatureData addNewSignatureData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferRequestType.SignatureData target = null;
            target = (DataTransferRequestType.SignatureData)get_store().add_element_user(SIGNATUREDATA$2);
            return target;
        }
    }
    
    /**
     * Unsets the "SignatureData" element
     */
    public void unsetSignatureData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNATUREDATA$2, 0);
        }
    }
    
    /**
     * Gets the "OrderData" element
     */
    public DataTransferRequestType.OrderData getOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferRequestType.OrderData target = null;
            target = (DataTransferRequestType.OrderData)get_store().find_element_user(ORDERDATA$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "OrderData" element
     */
    public boolean isSetOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ORDERDATA$4) != 0;
        }
    }
    
    /**
     * Sets the "OrderData" element
     */
    public void setOrderData(DataTransferRequestType.OrderData orderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferRequestType.OrderData target = null;
            target = (DataTransferRequestType.OrderData)get_store().find_element_user(ORDERDATA$4, 0);
            if (target == null)
            {
                target = (DataTransferRequestType.OrderData)get_store().add_element_user(ORDERDATA$4);
            }
            target.set(orderData);
        }
    }
    
    /**
     * Appends and returns a new empty "OrderData" element
     */
    public DataTransferRequestType.OrderData addNewOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataTransferRequestType.OrderData target = null;
            target = (DataTransferRequestType.OrderData)get_store().add_element_user(ORDERDATA$4);
            return target;
        }
    }
    
    /**
     * Unsets the "OrderData" element
     */
    public void unsetOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ORDERDATA$4, 0);
        }
    }
    /**
     * An XML DataEncryptionInfo(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public static class DataEncryptionInfoImpl extends DataEncryptionInfoTypeImpl implements DataTransferRequestType.DataEncryptionInfo
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
     * An XML SignatureData(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of DataTransferRequestType$SignatureData.
     */
    public static class SignatureDataImpl extends org.apache.xmlbeans.impl.values.JavaBase64HolderEx implements DataTransferRequestType.SignatureData
    {
        private static final long serialVersionUID = 1L;
        
        public SignatureDataImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected SignatureDataImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
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
     * This is an atomic type that is a restriction of DataTransferRequestType$OrderData.
     */
    public static class OrderDataImpl extends org.apache.xmlbeans.impl.values.JavaBase64HolderEx implements DataTransferRequestType.OrderData
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
