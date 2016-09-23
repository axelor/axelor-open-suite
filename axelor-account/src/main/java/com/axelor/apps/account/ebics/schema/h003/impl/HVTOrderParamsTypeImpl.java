/*
 * XML Type:  HVTOrderParamsType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVTOrderParamsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVTOrderParamsType;
import com.axelor.apps.account.ebics.schema.h003.OrderIDType;
import com.axelor.apps.account.ebics.schema.h003.OrderTBaseType;
import com.axelor.apps.account.ebics.schema.h003.ParameterDocument;
import com.axelor.apps.account.ebics.schema.h003.PartnerIDType;

/**
 * An XML HVTOrderParamsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class HVTOrderParamsTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements HVTOrderParamsType
{
    private static final long serialVersionUID = 1L;
    
    public HVTOrderParamsTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PARTNERID$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "PartnerID");
    private static final javax.xml.namespace.QName ORDERTYPE$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderType");
    private static final javax.xml.namespace.QName ORDERID$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderID");
    private static final javax.xml.namespace.QName ORDERFLAGS$6 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderFlags");
    private static final javax.xml.namespace.QName PARAMETER$8 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Parameter");
    
    
    /**
     * Gets the "PartnerID" element
     */
    public java.lang.String getPartnerID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTNERID$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "PartnerID" element
     */
    public PartnerIDType xgetPartnerID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            PartnerIDType target = null;
            target = (PartnerIDType)get_store().find_element_user(PARTNERID$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "PartnerID" element
     */
    public void setPartnerID(java.lang.String partnerID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTNERID$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PARTNERID$0);
            }
            target.setStringValue(partnerID);
        }
    }
    
    /**
     * Sets (as xml) the "PartnerID" element
     */
    public void xsetPartnerID(PartnerIDType partnerID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            PartnerIDType target = null;
            target = (PartnerIDType)get_store().find_element_user(PARTNERID$0, 0);
            if (target == null)
            {
                target = (PartnerIDType)get_store().add_element_user(PARTNERID$0);
            }
            target.set(partnerID);
        }
    }
    
    /**
     * Gets the "OrderType" element
     */
    public java.lang.String getOrderType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERTYPE$2, 0);
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
            target = (OrderTBaseType)get_store().find_element_user(ORDERTYPE$2, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERTYPE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ORDERTYPE$2);
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
            target = (OrderTBaseType)get_store().find_element_user(ORDERTYPE$2, 0);
            if (target == null)
            {
                target = (OrderTBaseType)get_store().add_element_user(ORDERTYPE$2);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERID$4, 0);
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
            target = (OrderIDType)get_store().find_element_user(ORDERID$4, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ORDERID$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ORDERID$4);
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
            target = (OrderIDType)get_store().find_element_user(ORDERID$4, 0);
            if (target == null)
            {
                target = (OrderIDType)get_store().add_element_user(ORDERID$4);
            }
            target.set(orderID);
        }
    }
    
    /**
     * Gets the "OrderFlags" element
     */
    public HVTOrderParamsType.OrderFlags getOrderFlags()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderParamsType.OrderFlags target = null;
            target = (HVTOrderParamsType.OrderFlags)get_store().find_element_user(ORDERFLAGS$6, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "OrderFlags" element
     */
    public void setOrderFlags(HVTOrderParamsType.OrderFlags orderFlags)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderParamsType.OrderFlags target = null;
            target = (HVTOrderParamsType.OrderFlags)get_store().find_element_user(ORDERFLAGS$6, 0);
            if (target == null)
            {
                target = (HVTOrderParamsType.OrderFlags)get_store().add_element_user(ORDERFLAGS$6);
            }
            target.set(orderFlags);
        }
    }
    
    /**
     * Appends and returns a new empty "OrderFlags" element
     */
    public HVTOrderParamsType.OrderFlags addNewOrderFlags()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderParamsType.OrderFlags target = null;
            target = (HVTOrderParamsType.OrderFlags)get_store().add_element_user(ORDERFLAGS$6);
            return target;
        }
    }
    
    /**
     * Gets array of all "Parameter" elements
     */
    public ParameterDocument.Parameter[] getParameterArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PARAMETER$8, targetList);
            ParameterDocument.Parameter[] result = new ParameterDocument.Parameter[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "Parameter" element
     */
    public ParameterDocument.Parameter getParameterArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ParameterDocument.Parameter target = null;
            target = (ParameterDocument.Parameter)get_store().find_element_user(PARAMETER$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "Parameter" element
     */
    public int sizeOfParameterArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PARAMETER$8);
        }
    }
    
    /**
     * Sets array of all "Parameter" element
     */
    public void setParameterArray(ParameterDocument.Parameter[] parameterArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(parameterArray, PARAMETER$8);
        }
    }
    
    /**
     * Sets ith "Parameter" element
     */
    public void setParameterArray(int i, ParameterDocument.Parameter parameter)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ParameterDocument.Parameter target = null;
            target = (ParameterDocument.Parameter)get_store().find_element_user(PARAMETER$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(parameter);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "Parameter" element
     */
    public ParameterDocument.Parameter insertNewParameter(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ParameterDocument.Parameter target = null;
            target = (ParameterDocument.Parameter)get_store().insert_element_user(PARAMETER$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "Parameter" element
     */
    public ParameterDocument.Parameter addNewParameter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ParameterDocument.Parameter target = null;
            target = (ParameterDocument.Parameter)get_store().add_element_user(PARAMETER$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "Parameter" element
     */
    public void removeParameter(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PARAMETER$8, i);
        }
    }
    /**
     * An XML OrderFlags(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public static class OrderFlagsImpl extends HVTOrderFlagsTypeImpl implements HVTOrderParamsType.OrderFlags
    {
        private static final long serialVersionUID = 1L;
        
        public OrderFlagsImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        
    }
}
