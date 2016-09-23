/*
 * An XML document type.
 * Localname: OrderParams
 * Namespace: http://www.ebics.org/H003
 * Java type: OrderParamsDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.OrderParamsDocument;

/**
 * A document containing one OrderParams(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class OrderParamsDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements OrderParamsDocument
{
    private static final long serialVersionUID = 1L;
    
    public OrderParamsDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ORDERPARAMS$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderParams");
    private static final org.apache.xmlbeans.QNameSet ORDERPARAMS$1 = org.apache.xmlbeans.QNameSet.forArray( new javax.xml.namespace.QName[] { 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVDOrderParams"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "OrderParams"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVEOrderParams"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "FDLOrderParams"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "StandardOrderParams"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVUOrderParams"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVSOrderParams"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVZOrderParams"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVTOrderParams"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "GenericOrderParams"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "FULOrderParams"),
    });
    
    
    /**
     * Gets the "OrderParams" element
     */
    public org.apache.xmlbeans.XmlObject getOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(ORDERPARAMS$1, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "OrderParams" element
     */
    public void setOrderParams(org.apache.xmlbeans.XmlObject orderParams)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(ORDERPARAMS$1, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(ORDERPARAMS$0);
            }
            target.set(orderParams);
        }
    }
    
    /**
     * Appends and returns a new empty "OrderParams" element
     */
    public org.apache.xmlbeans.XmlObject addNewOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(ORDERPARAMS$0);
            return target;
        }
    }
}
