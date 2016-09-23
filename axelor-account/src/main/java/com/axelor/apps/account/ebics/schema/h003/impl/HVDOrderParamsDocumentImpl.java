/*
 * An XML document type.
 * Localname: HVDOrderParams
 * Namespace: http://www.ebics.org/H003
 * Java type: HVDOrderParamsDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVDOrderParamsDocument;
import com.axelor.apps.account.ebics.schema.h003.HVDOrderParamsType;

/**
 * A document containing one HVDOrderParams(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HVDOrderParamsDocumentImpl extends OrderParamsDocumentImpl implements HVDOrderParamsDocument
{
    private static final long serialVersionUID = 1L;
    
    public HVDOrderParamsDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HVDORDERPARAMS$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVDOrderParams");
    
    
    /**
     * Gets the "HVDOrderParams" element
     */
    public HVDOrderParamsType getHVDOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVDOrderParamsType target = null;
            target = (HVDOrderParamsType)get_store().find_element_user(HVDORDERPARAMS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HVDOrderParams" element
     */
    public void setHVDOrderParams(HVDOrderParamsType hvdOrderParams)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVDOrderParamsType target = null;
            target = (HVDOrderParamsType)get_store().find_element_user(HVDORDERPARAMS$0, 0);
            if (target == null)
            {
                target = (HVDOrderParamsType)get_store().add_element_user(HVDORDERPARAMS$0);
            }
            target.set(hvdOrderParams);
        }
    }
    
    /**
     * Appends and returns a new empty "HVDOrderParams" element
     */
    public HVDOrderParamsType addNewHVDOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVDOrderParamsType target = null;
            target = (HVDOrderParamsType)get_store().add_element_user(HVDORDERPARAMS$0);
            return target;
        }
    }
}
