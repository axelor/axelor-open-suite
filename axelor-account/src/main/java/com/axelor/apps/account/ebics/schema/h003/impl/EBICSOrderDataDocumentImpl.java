/*
 * An XML document type.
 * Localname: EBICSOrderData
 * Namespace: http://www.ebics.org/H003
 * Java type: EBICSOrderDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.EBICSOrderDataDocument;

/**
 * A document containing one EBICSOrderData(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class EBICSOrderDataDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements EBICSOrderDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public EBICSOrderDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EBICSORDERDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "EBICSOrderData");
    private static final org.apache.xmlbeans.QNameSet EBICSORDERDATA$1 = org.apache.xmlbeans.QNameSet.forArray( new javax.xml.namespace.QName[] { 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "EBICSOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HIARequestOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVZResponseOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HCSRequestOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVTResponseOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HAAResponseOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HPBResponseOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HSARequestOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HPDResponseOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HKDResponseOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVUResponseOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVDResponseOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HVSRequestOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HTDResponseOrderData"),
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HCARequestOrderData"),
    });
    
    
    /**
     * Gets the "EBICSOrderData" element
     */
    public org.apache.xmlbeans.XmlObject getEBICSOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(EBICSORDERDATA$1, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "EBICSOrderData" element
     */
    public void setEBICSOrderData(org.apache.xmlbeans.XmlObject ebicsOrderData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(EBICSORDERDATA$1, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(EBICSORDERDATA$0);
            }
            target.set(ebicsOrderData);
        }
    }
    
    /**
     * Appends and returns a new empty "EBICSOrderData" element
     */
    public org.apache.xmlbeans.XmlObject addNewEBICSOrderData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(EBICSORDERDATA$0);
            return target;
        }
    }
}
