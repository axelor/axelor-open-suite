/*
 * XML Type:  HVDResponseOrderDataType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVDResponseOrderDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.DataDigestType;
import com.axelor.apps.account.ebics.schema.h003.HVDResponseOrderDataType;
import com.axelor.apps.account.ebics.schema.h003.SignerInfoType;

/**
 * An XML HVDResponseOrderDataType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class HVDResponseOrderDataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements HVDResponseOrderDataType
{
    private static final long serialVersionUID = 1L;
    
    public HVDResponseOrderDataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName DATADIGEST$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "DataDigest");
    private static final javax.xml.namespace.QName DISPLAYFILE$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "DisplayFile");
    private static final javax.xml.namespace.QName ORDERDATAAVAILABLE$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderDataAvailable");
    private static final javax.xml.namespace.QName ORDERDATASIZE$6 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderDataSize");
    private static final javax.xml.namespace.QName ORDERDETAILSAVAILABLE$8 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderDetailsAvailable");
    private static final javax.xml.namespace.QName SIGNERINFO$10 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "SignerInfo");
    
    
    /**
     * Gets the "DataDigest" element
     */
    public DataDigestType getDataDigest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataDigestType target = null;
            target = (DataDigestType)get_store().find_element_user(DATADIGEST$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "DataDigest" element
     */
    public void setDataDigest(DataDigestType dataDigest)
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataDigestType target = null;
            target = (DataDigestType)get_store().find_element_user(DATADIGEST$0, 0);
            if (target == null)
            {
                target = (DataDigestType)get_store().add_element_user(DATADIGEST$0);
            }
            target.set(dataDigest);
        }
    }
    
    /**
     * Appends and returns a new empty "DataDigest" element
     */
    public DataDigestType addNewDataDigest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataDigestType target = null;
            target = (DataDigestType)get_store().add_element_user(DATADIGEST$0);
            return target;
        }
    }
    
    /**
     * Gets the "DisplayFile" element
     */
    public byte[] getDisplayFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(DISPLAYFILE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "DisplayFile" element
     */
    public org.apache.xmlbeans.XmlBase64Binary xgetDisplayFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(DISPLAYFILE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "DisplayFile" element
     */
    public void setDisplayFile(byte[] displayFile)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(DISPLAYFILE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(DISPLAYFILE$2);
            }
            target.setByteArrayValue(displayFile);
        }
    }
    
    /**
     * Sets (as xml) the "DisplayFile" element
     */
    public void xsetDisplayFile(org.apache.xmlbeans.XmlBase64Binary displayFile)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(DISPLAYFILE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlBase64Binary)get_store().add_element_user(DISPLAYFILE$2);
            }
            target.set(displayFile);
        }
    }
    
    /**
     * Gets the "OrderDataAvailable" element
     */
    public boolean getOrderDataAvailable()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERDATAAVAILABLE$4, 0);
            if (target == null)
            {
                return false;
            }
            return target.getBooleanValue();
        }
    }
    
    /**
     * Gets (as xml) the "OrderDataAvailable" element
     */
    public org.apache.xmlbeans.XmlBoolean xgetOrderDataAvailable()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBoolean target = null;
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_element_user(ORDERDATAAVAILABLE$4, 0);
            return target;
        }
    }
    
    /**
     * Sets the "OrderDataAvailable" element
     */
    public void setOrderDataAvailable(boolean orderDataAvailable)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERDATAAVAILABLE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ORDERDATAAVAILABLE$4);
            }
            target.setBooleanValue(orderDataAvailable);
        }
    }
    
    /**
     * Sets (as xml) the "OrderDataAvailable" element
     */
    public void xsetOrderDataAvailable(org.apache.xmlbeans.XmlBoolean orderDataAvailable)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBoolean target = null;
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_element_user(ORDERDATAAVAILABLE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlBoolean)get_store().add_element_user(ORDERDATAAVAILABLE$4);
            }
            target.set(orderDataAvailable);
        }
    }
    
    /**
     * Gets the "OrderDataSize" element
     */
    public java.math.BigInteger getOrderDataSize()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERDATASIZE$6, 0);
            if (target == null)
            {
                return null;
            }
            return target.getBigIntegerValue();
        }
    }
    
    /**
     * Gets (as xml) the "OrderDataSize" element
     */
    public org.apache.xmlbeans.XmlPositiveInteger xgetOrderDataSize()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlPositiveInteger target = null;
            target = (org.apache.xmlbeans.XmlPositiveInteger)get_store().find_element_user(ORDERDATASIZE$6, 0);
            return target;
        }
    }
    
    /**
     * Sets the "OrderDataSize" element
     */
    public void setOrderDataSize(java.math.BigInteger orderDataSize)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERDATASIZE$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ORDERDATASIZE$6);
            }
            target.setBigIntegerValue(orderDataSize);
        }
    }
    
    /**
     * Sets (as xml) the "OrderDataSize" element
     */
    public void xsetOrderDataSize(org.apache.xmlbeans.XmlPositiveInteger orderDataSize)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlPositiveInteger target = null;
            target = (org.apache.xmlbeans.XmlPositiveInteger)get_store().find_element_user(ORDERDATASIZE$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlPositiveInteger)get_store().add_element_user(ORDERDATASIZE$6);
            }
            target.set(orderDataSize);
        }
    }
    
    /**
     * Gets the "OrderDetailsAvailable" element
     */
    public boolean getOrderDetailsAvailable()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERDETAILSAVAILABLE$8, 0);
            if (target == null)
            {
                return false;
            }
            return target.getBooleanValue();
        }
    }
    
    /**
     * Gets (as xml) the "OrderDetailsAvailable" element
     */
    public org.apache.xmlbeans.XmlBoolean xgetOrderDetailsAvailable()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBoolean target = null;
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_element_user(ORDERDETAILSAVAILABLE$8, 0);
            return target;
        }
    }
    
    /**
     * Sets the "OrderDetailsAvailable" element
     */
    public void setOrderDetailsAvailable(boolean orderDetailsAvailable)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERDETAILSAVAILABLE$8, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ORDERDETAILSAVAILABLE$8);
            }
            target.setBooleanValue(orderDetailsAvailable);
        }
    }
    
    /**
     * Sets (as xml) the "OrderDetailsAvailable" element
     */
    public void xsetOrderDetailsAvailable(org.apache.xmlbeans.XmlBoolean orderDetailsAvailable)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBoolean target = null;
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_element_user(ORDERDETAILSAVAILABLE$8, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlBoolean)get_store().add_element_user(ORDERDETAILSAVAILABLE$8);
            }
            target.set(orderDetailsAvailable);
        }
    }
    
    /**
     * Gets array of all "SignerInfo" elements
     */
    public SignerInfoType[] getSignerInfoArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SIGNERINFO$10, targetList);
            SignerInfoType[] result = new SignerInfoType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "SignerInfo" element
     */
    public SignerInfoType getSignerInfoArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignerInfoType target = null;
            target = (SignerInfoType)get_store().find_element_user(SIGNERINFO$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "SignerInfo" element
     */
    public int sizeOfSignerInfoArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNERINFO$10);
        }
    }
    
    /**
     * Sets array of all "SignerInfo" element
     */
    public void setSignerInfoArray(SignerInfoType[] signerInfoArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(signerInfoArray, SIGNERINFO$10);
        }
    }
    
    /**
     * Sets ith "SignerInfo" element
     */
    public void setSignerInfoArray(int i, SignerInfoType signerInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignerInfoType target = null;
            target = (SignerInfoType)get_store().find_element_user(SIGNERINFO$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(signerInfo);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "SignerInfo" element
     */
    public SignerInfoType insertNewSignerInfo(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignerInfoType target = null;
            target = (SignerInfoType)get_store().insert_element_user(SIGNERINFO$10, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "SignerInfo" element
     */
    public SignerInfoType addNewSignerInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignerInfoType target = null;
            target = (SignerInfoType)get_store().add_element_user(SIGNERINFO$10);
            return target;
        }
    }
    
    /**
     * Removes the ith "SignerInfo" element
     */
    public void removeSignerInfo(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNERINFO$10, i);
        }
    }
}
