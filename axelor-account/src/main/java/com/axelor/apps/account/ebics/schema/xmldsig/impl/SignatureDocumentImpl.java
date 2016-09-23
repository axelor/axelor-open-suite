/*
 * An XML document type.
 * Localname: Signature
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: SignatureDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.SignatureDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureType;

/**
 * A document containing one Signature(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class SignatureDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignatureDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignatureDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATURE$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Signature");
    
    
    /**
     * Gets the "Signature" element
     */
    public SignatureType getSignature()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureType target = null;
            target = (SignatureType)get_store().find_element_user(SIGNATURE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "Signature" element
     */
    public void setSignature(SignatureType signature)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureType target = null;
            target = (SignatureType)get_store().find_element_user(SIGNATURE$0, 0);
            if (target == null)
            {
                target = (SignatureType)get_store().add_element_user(SIGNATURE$0);
            }
            target.set(signature);
        }
    }
    
    /**
     * Appends and returns a new empty "Signature" element
     */
    public SignatureType addNewSignature()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureType target = null;
            target = (SignatureType)get_store().add_element_user(SIGNATURE$0);
            return target;
        }
    }
}
