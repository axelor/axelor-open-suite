/*
 * XML Type:  HVUResponseOrderDataType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVUResponseOrderDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVUOrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.HVUResponseOrderDataType;

/**
 * An XML HVUResponseOrderDataType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class HVUResponseOrderDataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements HVUResponseOrderDataType
{
    private static final long serialVersionUID = 1L;
    
    public HVUResponseOrderDataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ORDERDETAILS$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderDetails");
    
    
    /**
     * Gets array of all "OrderDetails" elements
     */
    public HVUOrderDetailsType[] getOrderDetailsArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ORDERDETAILS$0, targetList);
            HVUOrderDetailsType[] result = new HVUOrderDetailsType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "OrderDetails" element
     */
    public HVUOrderDetailsType getOrderDetailsArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUOrderDetailsType target = null;
            target = (HVUOrderDetailsType)get_store().find_element_user(ORDERDETAILS$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "OrderDetails" element
     */
    public int sizeOfOrderDetailsArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ORDERDETAILS$0);
        }
    }
    
    /**
     * Sets array of all "OrderDetails" element
     */
    public void setOrderDetailsArray(HVUOrderDetailsType[] orderDetailsArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(orderDetailsArray, ORDERDETAILS$0);
        }
    }
    
    /**
     * Sets ith "OrderDetails" element
     */
    public void setOrderDetailsArray(int i, HVUOrderDetailsType orderDetails)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUOrderDetailsType target = null;
            target = (HVUOrderDetailsType)get_store().find_element_user(ORDERDETAILS$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(orderDetails);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "OrderDetails" element
     */
    public HVUOrderDetailsType insertNewOrderDetails(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUOrderDetailsType target = null;
            target = (HVUOrderDetailsType)get_store().insert_element_user(ORDERDETAILS$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "OrderDetails" element
     */
    public HVUOrderDetailsType addNewOrderDetails()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUOrderDetailsType target = null;
            target = (HVUOrderDetailsType)get_store().add_element_user(ORDERDETAILS$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "OrderDetails" element
     */
    public void removeOrderDetails(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ORDERDETAILS$0, i);
        }
    }
}
