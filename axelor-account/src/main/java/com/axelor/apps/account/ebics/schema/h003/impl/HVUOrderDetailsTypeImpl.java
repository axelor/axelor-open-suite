/*
 * XML Type:  HVUOrderDetailsType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVUOrderDetailsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVUOrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.HVUOriginatorInfoType;
import com.axelor.apps.account.ebics.schema.h003.HVUSigningInfoType;
import com.axelor.apps.account.ebics.schema.h003.OrderIDType;
import com.axelor.apps.account.ebics.schema.h003.OrderTBaseType;
import com.axelor.apps.account.ebics.schema.h003.SignerInfoType;

/**
 * An XML HVUOrderDetailsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class HVUOrderDetailsTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements HVUOrderDetailsType
{
    private static final long serialVersionUID = 1L;
    
    public HVUOrderDetailsTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ORDERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderType");
    private static final javax.xml.namespace.QName ORDERID$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderID");
    private static final javax.xml.namespace.QName ORDERDATASIZE$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderDataSize");
    private static final javax.xml.namespace.QName SIGNINGINFO$6 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "SigningInfo");
    private static final javax.xml.namespace.QName SIGNERINFO$8 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "SignerInfo");
    private static final javax.xml.namespace.QName ORIGINATORINFO$10 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OriginatorInfo");
    
    
    /**
     * Gets the "OrderType" element
     */
    public java.lang.String getOrderType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "OrderType" element
     */
    public OrderTBaseType xgetOrderType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            OrderTBaseType target = null;
            target = (OrderTBaseType)get_store().find_element_user(ORDERTYPE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "OrderType" element
     */
    public void setOrderType(java.lang.String orderType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERTYPE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ORDERTYPE$0);
            }
            target.setStringValue(orderType);
        }
    }
    
    /**
     * Sets (as xml) the "OrderType" element
     */
    public void xsetOrderType(OrderTBaseType orderType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            OrderTBaseType target = null;
            target = (OrderTBaseType)get_store().find_element_user(ORDERTYPE$0, 0);
            if (target == null)
            {
                target = (OrderTBaseType)get_store().add_element_user(ORDERTYPE$0);
            }
            target.set(orderType);
        }
    }
    
    /**
     * Gets the "OrderID" element
     */
    public java.lang.String getOrderID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERID$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "OrderID" element
     */
    public OrderIDType xgetOrderID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            OrderIDType target = null;
            target = (OrderIDType)get_store().find_element_user(ORDERID$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "OrderID" element
     */
    public void setOrderID(java.lang.String orderID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERID$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ORDERID$2);
            }
            target.setStringValue(orderID);
        }
    }
    
    /**
     * Sets (as xml) the "OrderID" element
     */
    public void xsetOrderID(OrderIDType orderID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            OrderIDType target = null;
            target = (OrderIDType)get_store().find_element_user(ORDERID$2, 0);
            if (target == null)
            {
                target = (OrderIDType)get_store().add_element_user(ORDERID$2);
            }
            target.set(orderID);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERDATASIZE$4, 0);
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
            target = (org.apache.xmlbeans.XmlPositiveInteger)get_store().find_element_user(ORDERDATASIZE$4, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERDATASIZE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ORDERDATASIZE$4);
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
            target = (org.apache.xmlbeans.XmlPositiveInteger)get_store().find_element_user(ORDERDATASIZE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlPositiveInteger)get_store().add_element_user(ORDERDATASIZE$4);
            }
            target.set(orderDataSize);
        }
    }
    
    /**
     * Gets the "SigningInfo" element
     */
    public HVUSigningInfoType getSigningInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUSigningInfoType target = null;
            target = (HVUSigningInfoType)get_store().find_element_user(SIGNINGINFO$6, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "SigningInfo" element
     */
    public void setSigningInfo(HVUSigningInfoType signingInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUSigningInfoType target = null;
            target = (HVUSigningInfoType)get_store().find_element_user(SIGNINGINFO$6, 0);
            if (target == null)
            {
                target = (HVUSigningInfoType)get_store().add_element_user(SIGNINGINFO$6);
            }
            target.set(signingInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "SigningInfo" element
     */
    public HVUSigningInfoType addNewSigningInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUSigningInfoType target = null;
            target = (HVUSigningInfoType)get_store().add_element_user(SIGNINGINFO$6);
            return target;
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
            get_store().find_all_element_users(SIGNERINFO$8, targetList);
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
            target = (SignerInfoType)get_store().find_element_user(SIGNERINFO$8, i);
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
            return get_store().count_elements(SIGNERINFO$8);
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
            arraySetterHelper(signerInfoArray, SIGNERINFO$8);
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
            target = (SignerInfoType)get_store().find_element_user(SIGNERINFO$8, i);
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
            target = (SignerInfoType)get_store().insert_element_user(SIGNERINFO$8, i);
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
            target = (SignerInfoType)get_store().add_element_user(SIGNERINFO$8);
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
            get_store().remove_element(SIGNERINFO$8, i);
        }
    }
    
    /**
     * Gets the "OriginatorInfo" element
     */
    public HVUOriginatorInfoType getOriginatorInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUOriginatorInfoType target = null;
            target = (HVUOriginatorInfoType)get_store().find_element_user(ORIGINATORINFO$10, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "OriginatorInfo" element
     */
    public void setOriginatorInfo(HVUOriginatorInfoType originatorInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUOriginatorInfoType target = null;
            target = (HVUOriginatorInfoType)get_store().find_element_user(ORIGINATORINFO$10, 0);
            if (target == null)
            {
                target = (HVUOriginatorInfoType)get_store().add_element_user(ORIGINATORINFO$10);
            }
            target.set(originatorInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "OriginatorInfo" element
     */
    public HVUOriginatorInfoType addNewOriginatorInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUOriginatorInfoType target = null;
            target = (HVUOriginatorInfoType)get_store().add_element_user(ORIGINATORINFO$10);
            return target;
        }
    }
}
