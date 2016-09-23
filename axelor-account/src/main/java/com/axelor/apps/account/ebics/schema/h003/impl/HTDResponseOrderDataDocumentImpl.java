/*
 * An XML document type.
 * Localname: HTDResponseOrderData
 * Namespace: http://www.ebics.org/H003
 * Java type: HTDResponseOrderDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HTDReponseOrderDataType;
import com.axelor.apps.account.ebics.schema.h003.HTDResponseOrderDataDocument;

/**
 * A document containing one HTDResponseOrderData(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HTDResponseOrderDataDocumentImpl extends EBICSOrderDataDocumentImpl implements HTDResponseOrderDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public HTDResponseOrderDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HTDRESPONSEORDERDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HTDResponseOrderData");
    
    
    /**
     * Gets the "HTDResponseOrderData" element
     */
    public HTDReponseOrderDataType getHTDResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HTDReponseOrderDataType target = null;
            target = (HTDReponseOrderDataType)get_store().find_element_user(HTDRESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HTDResponseOrderData" element
     */
    public void setHTDResponseOrderData(HTDReponseOrderDataType htdResponseOrderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HTDReponseOrderDataType target = null;
            target = (HTDReponseOrderDataType)get_store().find_element_user(HTDRESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                target = (HTDReponseOrderDataType)get_store().add_element_user(HTDRESPONSEORDERDATA$0);
            }
            target.set(htdResponseOrderData);
        }
    }
    
    /**
     * Appends and returns a new empty "HTDResponseOrderData" element
     */
    public HTDReponseOrderDataType addNewHTDResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HTDReponseOrderDataType target = null;
            target = (HTDReponseOrderDataType)get_store().add_element_user(HTDRESPONSEORDERDATA$0);
            return target;
        }
    }
}
