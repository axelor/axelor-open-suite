/*
 * An XML document type.
 * Localname: DigestValue
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: DigestValueDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.DigestValueDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.DigestValueType;

/**
 * A document containing one DigestValue(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class DigestValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements DigestValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public DigestValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName DIGESTVALUE$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "DigestValue");
    
    
    /**
     * Gets the "DigestValue" element
     */
    public byte[] getDigestValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(DIGESTVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "DigestValue" element
     */
    public DigestValueType xgetDigestValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DigestValueType target = null;
            target = (DigestValueType)get_store().find_element_user(DIGESTVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "DigestValue" element
     */
    public void setDigestValue(byte[] digestValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(DIGESTVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(DIGESTVALUE$0);
            }
            target.setByteArrayValue(digestValue);
        }
    }
    
    /**
     * Sets (as xml) the "DigestValue" element
     */
    public void xsetDigestValue(DigestValueType digestValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            DigestValueType target = null;
            target = (DigestValueType)get_store().find_element_user(DIGESTVALUE$0, 0);
            if (target == null)
            {
                target = (DigestValueType)get_store().add_element_user(DIGESTVALUE$0);
            }
            target.set(digestValue);
        }
    }
}
