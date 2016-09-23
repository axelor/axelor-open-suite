/*
 * XML Type:  OrderDetailsType
 * Namespace: http://www.ebics.org/H003
 * Java type: OrderDetailsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.OrderAttributeBaseType;
import com.axelor.apps.account.ebics.schema.h003.OrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.OrderIDType;
import com.axelor.apps.account.ebics.schema.h003.OrderTBaseType;

/**
 * An XML OrderDetailsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class OrderDetailsTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements OrderDetailsType
{
    private static final long serialVersionUID = 1L;
    
    public OrderDetailsTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ORDERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderType");
    private static final javax.xml.namespace.QName ORDERID$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderID");
    private static final javax.xml.namespace.QName ORDERATTRIBUTE$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderAttribute");
    
    
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
     * True if has "OrderID" element
     */
    public boolean isSetOrderID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ORDERID$2) != 0;
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
     * Unsets the "OrderID" element
     */
    public void unsetOrderID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ORDERID$2, 0);
        }
    }
    
    /**
     * Gets the "OrderAttribute" element
     */
    public java.lang.String getOrderAttribute()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERATTRIBUTE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "OrderAttribute" element
     */
    public OrderAttributeBaseType xgetOrderAttribute()
    {
        synchronized (monitor())
        {
            check_orphaned();
            OrderAttributeBaseType target = null;
            target = (OrderAttributeBaseType)get_store().find_element_user(ORDERATTRIBUTE$4, 0);
            return target;
        }
    }
    
    /**
     * Sets the "OrderAttribute" element
     */
    public void setOrderAttribute(java.lang.String orderAttribute)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERATTRIBUTE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ORDERATTRIBUTE$4);
            }
            target.setStringValue(orderAttribute);
        }
    }
    
    /**
     * Sets (as xml) the "OrderAttribute" element
     */
    public void xsetOrderAttribute(OrderAttributeBaseType orderAttribute)
    {
        synchronized (monitor())
        {
            check_orphaned();
            OrderAttributeBaseType target = null;
            target = (OrderAttributeBaseType)get_store().find_element_user(ORDERATTRIBUTE$4, 0);
            if (target == null)
            {
                target = (OrderAttributeBaseType)get_store().add_element_user(ORDERATTRIBUTE$4);
            }
            target.set(orderAttribute);
        }
    }
}
