/*
 * An XML document type.
 * Localname: HCARequestOrderData
 * Namespace: http://www.ebics.org/H003
 * Java type: HCARequestOrderDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HCARequestOrderDataDocument;
import com.axelor.apps.account.ebics.schema.h003.HCARequestOrderDataType;

/**
 * A document containing one HCARequestOrderData(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HCARequestOrderDataDocumentImpl extends EBICSOrderDataDocumentImpl implements HCARequestOrderDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public HCARequestOrderDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HCAREQUESTORDERDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HCARequestOrderData");
    
    
    /**
     * Gets the "HCARequestOrderData" element
     */
    public HCARequestOrderDataType getHCARequestOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HCARequestOrderDataType target = null;
            target = (HCARequestOrderDataType)get_store().find_element_user(HCAREQUESTORDERDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HCARequestOrderData" element
     */
    public void setHCARequestOrderData(HCARequestOrderDataType hcaRequestOrderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HCARequestOrderDataType target = null;
            target = (HCARequestOrderDataType)get_store().find_element_user(HCAREQUESTORDERDATA$0, 0);
            if (target == null)
            {
                target = (HCARequestOrderDataType)get_store().add_element_user(HCAREQUESTORDERDATA$0);
            }
            target.set(hcaRequestOrderData);
        }
    }
    
    /**
     * Appends and returns a new empty "HCARequestOrderData" element
     */
    public HCARequestOrderDataType addNewHCARequestOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HCARequestOrderDataType target = null;
            target = (HCARequestOrderDataType)get_store().add_element_user(HCAREQUESTORDERDATA$0);
            return target;
        }
    }
}
