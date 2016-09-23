/*
 * An XML document type.
 * Localname: HAAResponseOrderData
 * Namespace: http://www.ebics.org/H003
 * Java type: HAAResponseOrderDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HAAResponseOrderDataDocument;
import com.axelor.apps.account.ebics.schema.h003.HAAResponseOrderDataType;

/**
 * A document containing one HAAResponseOrderData(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HAAResponseOrderDataDocumentImpl extends EBICSOrderDataDocumentImpl implements HAAResponseOrderDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public HAAResponseOrderDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HAARESPONSEORDERDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HAAResponseOrderData");
    
    
    /**
     * Gets the "HAAResponseOrderData" element
     */
    public HAAResponseOrderDataType getHAAResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HAAResponseOrderDataType target = null;
            target = (HAAResponseOrderDataType)get_store().find_element_user(HAARESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HAAResponseOrderData" element
     */
    public void setHAAResponseOrderData(HAAResponseOrderDataType haaResponseOrderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HAAResponseOrderDataType target = null;
            target = (HAAResponseOrderDataType)get_store().find_element_user(HAARESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                target = (HAAResponseOrderDataType)get_store().add_element_user(HAARESPONSEORDERDATA$0);
            }
            target.set(haaResponseOrderData);
        }
    }
    
    /**
     * Appends and returns a new empty "HAAResponseOrderData" element
     */
    public HAAResponseOrderDataType addNewHAAResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HAAResponseOrderDataType target = null;
            target = (HAAResponseOrderDataType)get_store().add_element_user(HAARESPONSEORDERDATA$0);
            return target;
        }
    }
}
