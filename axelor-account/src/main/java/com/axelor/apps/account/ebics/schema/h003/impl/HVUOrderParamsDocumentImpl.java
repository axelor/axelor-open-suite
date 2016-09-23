/*
 * An XML document type.
 * Localname: HVUOrderParams
 * Namespace: http://www.ebics.org/H003
 * Java type: HVUOrderParamsDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVUOrderParamsDocument;
import com.axelor.apps.account.ebics.schema.h003.HVUOrderParamsType;

/**
 * A document containing one HVUOrderParams(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HVUOrderParamsDocumentImpl extends OrderParamsDocumentImpl implements HVUOrderParamsDocument
{
    private static final long serialVersionUID = 1L;
    
    public HVUOrderParamsDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HVUORDERPARAMS$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVUOrderParams");
    
    
    /**
     * Gets the "HVUOrderParams" element
     */
    public HVUOrderParamsType getHVUOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUOrderParamsType target = null;
            target = (HVUOrderParamsType)get_store().find_element_user(HVUORDERPARAMS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HVUOrderParams" element
     */
    public void setHVUOrderParams(HVUOrderParamsType hvuOrderParams)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUOrderParamsType target = null;
            target = (HVUOrderParamsType)get_store().find_element_user(HVUORDERPARAMS$0, 0);
            if (target == null)
            {
                target = (HVUOrderParamsType)get_store().add_element_user(HVUORDERPARAMS$0);
            }
            target.set(hvuOrderParams);
        }
    }
    
    /**
     * Appends and returns a new empty "HVUOrderParams" element
     */
    public HVUOrderParamsType addNewHVUOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVUOrderParamsType target = null;
            target = (HVUOrderParamsType)get_store().add_element_user(HVUORDERPARAMS$0);
            return target;
        }
    }
}
