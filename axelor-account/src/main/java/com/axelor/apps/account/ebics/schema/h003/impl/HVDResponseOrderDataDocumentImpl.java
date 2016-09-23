/*
 * An XML document type.
 * Localname: HVDResponseOrderData
 * Namespace: http://www.ebics.org/H003
 * Java type: HVDResponseOrderDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVDResponseOrderDataDocument;
import com.axelor.apps.account.ebics.schema.h003.HVDResponseOrderDataType;

/**
 * A document containing one HVDResponseOrderData(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HVDResponseOrderDataDocumentImpl extends EBICSOrderDataDocumentImpl implements HVDResponseOrderDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public HVDResponseOrderDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HVDRESPONSEORDERDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVDResponseOrderData");
    
    
    /**
     * Gets the "HVDResponseOrderData" element
     */
    public HVDResponseOrderDataType getHVDResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVDResponseOrderDataType target = null;
            target = (HVDResponseOrderDataType)get_store().find_element_user(HVDRESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HVDResponseOrderData" element
     */
    public void setHVDResponseOrderData(HVDResponseOrderDataType hvdResponseOrderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVDResponseOrderDataType target = null;
            target = (HVDResponseOrderDataType)get_store().find_element_user(HVDRESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                target = (HVDResponseOrderDataType)get_store().add_element_user(HVDRESPONSEORDERDATA$0);
            }
            target.set(hvdResponseOrderData);
        }
    }
    
    /**
     * Appends and returns a new empty "HVDResponseOrderData" element
     */
    public HVDResponseOrderDataType addNewHVDResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVDResponseOrderDataType target = null;
            target = (HVDResponseOrderDataType)get_store().add_element_user(HVDRESPONSEORDERDATA$0);
            return target;
        }
    }
}
