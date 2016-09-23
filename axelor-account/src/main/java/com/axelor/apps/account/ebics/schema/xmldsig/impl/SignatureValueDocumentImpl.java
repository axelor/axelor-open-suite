/*
 * An XML document type.
 * Localname: SignatureValue
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: SignatureValueDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.SignatureValueDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureValueType;

/**
 * A document containing one SignatureValue(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class SignatureValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignatureValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignatureValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREVALUE$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignatureValue");
    
    
    /**
     * Gets the "SignatureValue" element
     */
    public SignatureValueType getSignatureValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureValueType target = null;
            target = (SignatureValueType)get_store().find_element_user(SIGNATUREVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "SignatureValue" element
     */
    public void setSignatureValue(SignatureValueType signatureValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureValueType target = null;
            target = (SignatureValueType)get_store().find_element_user(SIGNATUREVALUE$0, 0);
            if (target == null)
            {
                target = (SignatureValueType)get_store().add_element_user(SIGNATUREVALUE$0);
            }
            target.set(signatureValue);
        }
    }
    
    /**
     * Appends and returns a new empty "SignatureValue" element
     */
    public SignatureValueType addNewSignatureValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureValueType target = null;
            target = (SignatureValueType)get_store().add_element_user(SIGNATUREVALUE$0);
            return target;
        }
    }
}
