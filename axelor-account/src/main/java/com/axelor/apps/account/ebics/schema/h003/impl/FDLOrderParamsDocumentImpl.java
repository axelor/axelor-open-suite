/*
 * An XML document type.
 * Localname: FDLOrderParams
 * Namespace: http://www.ebics.org/H003
 * Java type: FDLOrderParamsDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.FDLOrderParamsDocument;
import com.axelor.apps.account.ebics.schema.h003.FDLOrderParamsType;

/**
 * A document containing one FDLOrderParams(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class FDLOrderParamsDocumentImpl extends OrderParamsDocumentImpl implements FDLOrderParamsDocument
{
    private static final long serialVersionUID = 1L;
    
    public FDLOrderParamsDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FDLORDERPARAMS$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "FDLOrderParams");
    
    
    /**
     * Gets the "FDLOrderParams" element
     */
    public FDLOrderParamsType getFDLOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            FDLOrderParamsType target = null;
            target = (FDLOrderParamsType)get_store().find_element_user(FDLORDERPARAMS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "FDLOrderParams" element
     */
    public void setFDLOrderParams(FDLOrderParamsType fdlOrderParams)
    {
        synchronized (monitor())
        {
            check_orphaned();
            FDLOrderParamsType target = null;
            target = (FDLOrderParamsType)get_store().find_element_user(FDLORDERPARAMS$0, 0);
            if (target == null)
            {
                target = (FDLOrderParamsType)get_store().add_element_user(FDLORDERPARAMS$0);
            }
            target.set(fdlOrderParams);
        }
    }
    
    /**
     * Appends and returns a new empty "FDLOrderParams" element
     */
    public FDLOrderParamsType addNewFDLOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            FDLOrderParamsType target = null;
            target = (FDLOrderParamsType)get_store().add_element_user(FDLORDERPARAMS$0);
            return target;
        }
    }
}
