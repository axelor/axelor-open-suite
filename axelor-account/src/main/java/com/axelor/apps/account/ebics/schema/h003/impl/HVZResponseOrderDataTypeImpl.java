/*
 * XML Type:  HVZResponseOrderDataType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVZResponseOrderDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVZOrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.HVZResponseOrderDataType;

/**
 * An XML HVZResponseOrderDataType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class HVZResponseOrderDataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements HVZResponseOrderDataType
{
    private static final long serialVersionUID = 1L;
    
    public HVZResponseOrderDataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ORDERDETAILS$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderDetails");
    
    
    /**
     * Gets array of all "OrderDetails" elements
     */
    public HVZOrderDetailsType[] getOrderDetailsArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ORDERDETAILS$0, targetList);
            HVZOrderDetailsType[] result = new HVZOrderDetailsType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "OrderDetails" element
     */
    public HVZOrderDetailsType getOrderDetailsArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVZOrderDetailsType target = null;
            target = (HVZOrderDetailsType)get_store().find_element_user(ORDERDETAILS$0, i);
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
    public void setOrderDetailsArray(HVZOrderDetailsType[] orderDetailsArray)
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
    public void setOrderDetailsArray(int i, HVZOrderDetailsType orderDetails)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVZOrderDetailsType target = null;
            target = (HVZOrderDetailsType)get_store().find_element_user(ORDERDETAILS$0, i);
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
    public HVZOrderDetailsType insertNewOrderDetails(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVZOrderDetailsType target = null;
            target = (HVZOrderDetailsType)get_store().insert_element_user(ORDERDETAILS$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "OrderDetails" element
     */
    public HVZOrderDetailsType addNewOrderDetails()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVZOrderDetailsType target = null;
            target = (HVZOrderDetailsType)get_store().add_element_user(ORDERDETAILS$0);
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
