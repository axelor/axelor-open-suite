/*
 * An XML document type.
 * Localname: HVSRequestOrderData
 * Namespace: http://www.ebics.org/H003
 * Java type: HVSRequestOrderDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVSRequestOrderDataDocument;
import com.axelor.apps.account.ebics.schema.h003.HVSRequestOrderDataType;

/**
 * A document containing one HVSRequestOrderData(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HVSRequestOrderDataDocumentImpl extends EBICSOrderDataDocumentImpl implements HVSRequestOrderDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public HVSRequestOrderDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HVSREQUESTORDERDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVSRequestOrderData");
    
    
    /**
     * Gets the "HVSRequestOrderData" element
     */
    public HVSRequestOrderDataType getHVSRequestOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVSRequestOrderDataType target = null;
            target = (HVSRequestOrderDataType)get_store().find_element_user(HVSREQUESTORDERDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HVSRequestOrderData" element
     */
    public void setHVSRequestOrderData(HVSRequestOrderDataType hvsRequestOrderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVSRequestOrderDataType target = null;
            target = (HVSRequestOrderDataType)get_store().find_element_user(HVSREQUESTORDERDATA$0, 0);
            if (target == null)
            {
                target = (HVSRequestOrderDataType)get_store().add_element_user(HVSREQUESTORDERDATA$0);
            }
            target.set(hvsRequestOrderData);
        }
    }
    
    /**
     * Appends and returns a new empty "HVSRequestOrderData" element
     */
    public HVSRequestOrderDataType addNewHVSRequestOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVSRequestOrderDataType target = null;
            target = (HVSRequestOrderDataType)get_store().add_element_user(HVSREQUESTORDERDATA$0);
            return target;
        }
    }
}
