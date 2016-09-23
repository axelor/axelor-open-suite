/*
 * An XML document type.
 * Localname: StandardOrderParams
 * Namespace: http://www.ebics.org/H003
 * Java type: StandardOrderParamsDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.StandardOrderParamsDocument;
import com.axelor.apps.account.ebics.schema.h003.StandardOrderParamsType;

/**
 * A document containing one StandardOrderParams(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class StandardOrderParamsDocumentImpl extends OrderParamsDocumentImpl implements StandardOrderParamsDocument
{
    private static final long serialVersionUID = 1L;
    
    public StandardOrderParamsDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STANDARDORDERPARAMS$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "StandardOrderParams");
    
    
    /**
     * Gets the "StandardOrderParams" element
     */
    public StandardOrderParamsType getStandardOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            StandardOrderParamsType target = null;
            target = (StandardOrderParamsType)get_store().find_element_user(STANDARDORDERPARAMS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "StandardOrderParams" element
     */
    public void setStandardOrderParams(StandardOrderParamsType standardOrderParams)
    {
        synchronized (monitor())
        {
            check_orphaned();
            StandardOrderParamsType target = null;
            target = (StandardOrderParamsType)get_store().find_element_user(STANDARDORDERPARAMS$0, 0);
            if (target == null)
            {
                target = (StandardOrderParamsType)get_store().add_element_user(STANDARDORDERPARAMS$0);
            }
            target.set(standardOrderParams);
        }
    }
    
    /**
     * Appends and returns a new empty "StandardOrderParams" element
     */
    public StandardOrderParamsType addNewStandardOrderParams()
    {
        synchronized (monitor())
        {
            check_orphaned();
            StandardOrderParamsType target = null;
            target = (StandardOrderParamsType)get_store().add_element_user(STANDARDORDERPARAMS$0);
            return target;
        }
    }
}
