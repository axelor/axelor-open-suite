/*
 * An XML document type.
 * Localname: OrderSignatureData
 * Namespace: http://www.ebics.org/S001
 * Java type: OrderSignatureDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.s001.impl;

import com.axelor.apps.account.ebics.schema.s001.OrderSignatureDataDocument;
import com.axelor.apps.account.ebics.schema.s001.OrderSignatureDataType;

/**
 * A document containing one OrderSignatureData(@http://www.ebics.org/S001) element.
 *
 * This is a complex type.
 */
public class OrderSignatureDataDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements OrderSignatureDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public OrderSignatureDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ORDERSIGNATUREDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "OrderSignatureData");
    
    
    /**
     * Gets the "OrderSignatureData" element
     */
    public OrderSignatureDataType getOrderSignatureData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            OrderSignatureDataType target = null;
            target = (OrderSignatureDataType)get_store().find_element_user(ORDERSIGNATUREDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "OrderSignatureData" element
     */
    public void setOrderSignatureData(OrderSignatureDataType orderSignatureData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            OrderSignatureDataType target = null;
            target = (OrderSignatureDataType)get_store().find_element_user(ORDERSIGNATUREDATA$0, 0);
            if (target == null)
            {
                target = (OrderSignatureDataType)get_store().add_element_user(ORDERSIGNATUREDATA$0);
            }
            target.set(orderSignatureData);
        }
    }
    
    /**
     * Appends and returns a new empty "OrderSignatureData" element
     */
    public OrderSignatureDataType addNewOrderSignatureData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            OrderSignatureDataType target = null;
            target = (OrderSignatureDataType)get_store().add_element_user(ORDERSIGNATUREDATA$0);
            return target;
        }
    }
}
