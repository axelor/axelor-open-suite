/*
 * An XML document type.
 * Localname: HVTOrderParams
 * Namespace: http://www.ebics.org/H003
 * Java type: HVTOrderParamsDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVTOrderParamsDocument;
import com.axelor.apps.account.ebics.schema.h003.HVTOrderParamsType;

/**
 * A document containing one HVTOrderParams(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HVTOrderParamsDocumentImpl extends OrderParamsDocumentImpl implements HVTOrderParamsDocument
{
    private static final long serialVersionUID = 1L;
    
    public HVTOrderParamsDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HVTORDERPARAMS$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVTOrderParams");
    
    
    /**
     * Gets the "HVTOrderParams" element
     */
    public HVTOrderParamsType getHVTOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderParamsType target = null;
            target = (HVTOrderParamsType)get_store().find_element_user(HVTORDERPARAMS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HVTOrderParams" element
     */
    public void setHVTOrderParams(HVTOrderParamsType hvtOrderParams)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderParamsType target = null;
            target = (HVTOrderParamsType)get_store().find_element_user(HVTORDERPARAMS$0, 0);
            if (target == null)
            {
                target = (HVTOrderParamsType)get_store().add_element_user(HVTORDERPARAMS$0);
            }
            target.set(hvtOrderParams);
        }
    }
    
    /**
     * Appends and returns a new empty "HVTOrderParams" element
     */
    public HVTOrderParamsType addNewHVTOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTOrderParamsType target = null;
            target = (HVTOrderParamsType)get_store().add_element_user(HVTORDERPARAMS$0);
            return target;
        }
    }
}
