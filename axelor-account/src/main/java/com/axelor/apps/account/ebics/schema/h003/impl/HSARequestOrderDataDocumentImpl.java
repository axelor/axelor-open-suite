/*
 * An XML document type.
 * Localname: HSARequestOrderData
 * Namespace: http://www.ebics.org/H003
 * Java type: HSARequestOrderDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HSARequestOrderDataDocument;
import com.axelor.apps.account.ebics.schema.h003.HSARequestOrderDataType;

/**
 * A document containing one HSARequestOrderData(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HSARequestOrderDataDocumentImpl extends EBICSOrderDataDocumentImpl implements HSARequestOrderDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public HSARequestOrderDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HSAREQUESTORDERDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HSARequestOrderData");
    
    
    /**
     * Gets the "HSARequestOrderData" element
     */
    public HSARequestOrderDataType getHSARequestOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HSARequestOrderDataType target = null;
            target = (HSARequestOrderDataType)get_store().find_element_user(HSAREQUESTORDERDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HSARequestOrderData" element
     */
    public void setHSARequestOrderData(HSARequestOrderDataType hsaRequestOrderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HSARequestOrderDataType target = null;
            target = (HSARequestOrderDataType)get_store().find_element_user(HSAREQUESTORDERDATA$0, 0);
            if (target == null)
            {
                target = (HSARequestOrderDataType)get_store().add_element_user(HSAREQUESTORDERDATA$0);
            }
            target.set(hsaRequestOrderData);
        }
    }
    
    /**
     * Appends and returns a new empty "HSARequestOrderData" element
     */
    public HSARequestOrderDataType addNewHSARequestOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HSARequestOrderDataType target = null;
            target = (HSARequestOrderDataType)get_store().add_element_user(HSAREQUESTORDERDATA$0);
            return target;
        }
    }
}
