/*
 * An XML document type.
 * Localname: HVZResponseOrderData
 * Namespace: http://www.ebics.org/H003
 * Java type: HVZResponseOrderDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVZResponseOrderDataDocument;
import com.axelor.apps.account.ebics.schema.h003.HVZResponseOrderDataType;

/**
 * A document containing one HVZResponseOrderData(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HVZResponseOrderDataDocumentImpl extends EBICSOrderDataDocumentImpl implements HVZResponseOrderDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public HVZResponseOrderDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HVZRESPONSEORDERDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVZResponseOrderData");
    
    
    /**
     * Gets the "HVZResponseOrderData" element
     */
    public HVZResponseOrderDataType getHVZResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVZResponseOrderDataType target = null;
            target = (HVZResponseOrderDataType)get_store().find_element_user(HVZRESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HVZResponseOrderData" element
     */
    public void setHVZResponseOrderData(HVZResponseOrderDataType hvzResponseOrderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVZResponseOrderDataType target = null;
            target = (HVZResponseOrderDataType)get_store().find_element_user(HVZRESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                target = (HVZResponseOrderDataType)get_store().add_element_user(HVZRESPONSEORDERDATA$0);
            }
            target.set(hvzResponseOrderData);
        }
    }
    
    /**
     * Appends and returns a new empty "HVZResponseOrderData" element
     */
    public HVZResponseOrderDataType addNewHVZResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVZResponseOrderDataType target = null;
            target = (HVZResponseOrderDataType)get_store().add_element_user(HVZRESPONSEORDERDATA$0);
            return target;
        }
    }
}
