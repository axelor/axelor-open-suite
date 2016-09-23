/*
 * An XML document type.
 * Localname: HPDResponseOrderData
 * Namespace: http://www.ebics.org/H003
 * Java type: HPDResponseOrderDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HPDResponseOrderDataDocument;
import com.axelor.apps.account.ebics.schema.h003.HPDResponseOrderDataType;

/**
 * A document containing one HPDResponseOrderData(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HPDResponseOrderDataDocumentImpl extends EBICSOrderDataDocumentImpl implements HPDResponseOrderDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public HPDResponseOrderDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HPDRESPONSEORDERDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HPDResponseOrderData");
    
    
    /**
     * Gets the "HPDResponseOrderData" element
     */
    public HPDResponseOrderDataType getHPDResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HPDResponseOrderDataType target = null;
            target = (HPDResponseOrderDataType)get_store().find_element_user(HPDRESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HPDResponseOrderData" element
     */
    public void setHPDResponseOrderData(HPDResponseOrderDataType hpdResponseOrderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HPDResponseOrderDataType target = null;
            target = (HPDResponseOrderDataType)get_store().find_element_user(HPDRESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                target = (HPDResponseOrderDataType)get_store().add_element_user(HPDRESPONSEORDERDATA$0);
            }
            target.set(hpdResponseOrderData);
        }
    }
    
    /**
     * Appends and returns a new empty "HPDResponseOrderData" element
     */
    public HPDResponseOrderDataType addNewHPDResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HPDResponseOrderDataType target = null;
            target = (HPDResponseOrderDataType)get_store().add_element_user(HPDRESPONSEORDERDATA$0);
            return target;
        }
    }
}
