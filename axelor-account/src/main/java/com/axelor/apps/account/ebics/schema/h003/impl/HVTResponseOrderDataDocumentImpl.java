/*
 * An XML document type.
 * Localname: HVTResponseOrderData
 * Namespace: http://www.ebics.org/H003
 * Java type: HVTResponseOrderDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVTResponseOrderDataDocument;

/**
 * A document containing one HVTResponseOrderData(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class HVTResponseOrderDataDocumentImpl extends EBICSOrderDataDocumentImpl implements HVTResponseOrderDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public HVTResponseOrderDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HVTRESPONSEORDERDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVTResponseOrderData");
    
    
    /**
     * Gets the "HVTResponseOrderData" element
     */
    public HVTResponseOrderDataDocument.HVTResponseOrderData getHVTResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTResponseOrderDataDocument.HVTResponseOrderData target = null;
            target = (HVTResponseOrderDataDocument.HVTResponseOrderData)get_store().find_element_user(HVTRESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "HVTResponseOrderData" element
     */
    public void setHVTResponseOrderData(HVTResponseOrderDataDocument.HVTResponseOrderData hvtResponseOrderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTResponseOrderDataDocument.HVTResponseOrderData target = null;
            target = (HVTResponseOrderDataDocument.HVTResponseOrderData)get_store().find_element_user(HVTRESPONSEORDERDATA$0, 0);
            if (target == null)
            {
                target = (HVTResponseOrderDataDocument.HVTResponseOrderData)get_store().add_element_user(HVTRESPONSEORDERDATA$0);
            }
            target.set(hvtResponseOrderData);
        }
    }
    
    /**
     * Appends and returns a new empty "HVTResponseOrderData" element
     */
    public HVTResponseOrderDataDocument.HVTResponseOrderData addNewHVTResponseOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HVTResponseOrderDataDocument.HVTResponseOrderData target = null;
            target = (HVTResponseOrderDataDocument.HVTResponseOrderData)get_store().add_element_user(HVTRESPONSEORDERDATA$0);
            return target;
        }
    }
    /**
     * An XML HVTResponseOrderData(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public static class HVTResponseOrderDataImpl extends HVTResponseOrderDataTypeImpl implements HVTResponseOrderDataDocument.HVTResponseOrderData
    {
        private static final long serialVersionUID = 1L;
        
        public HVTResponseOrderDataImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        
    }
}
