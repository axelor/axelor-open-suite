/*
 * An XML document type.
 * Localname: SignatureMethod
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: SignatureMethodDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.SignatureMethodDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureMethodType;

/**
 * A document containing one SignatureMethod(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class SignatureMethodDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignatureMethodDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignatureMethodDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREMETHOD$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignatureMethod");
    
    
    /**
     * Gets the "SignatureMethod" element
     */
    public SignatureMethodType getSignatureMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureMethodType target = null;
            target = (SignatureMethodType)get_store().find_element_user(SIGNATUREMETHOD$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "SignatureMethod" element
     */
    public void setSignatureMethod(SignatureMethodType signatureMethod)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureMethodType target = null;
            target = (SignatureMethodType)get_store().find_element_user(SIGNATUREMETHOD$0, 0);
            if (target == null)
            {
                target = (SignatureMethodType)get_store().add_element_user(SIGNATUREMETHOD$0);
            }
            target.set(signatureMethod);
        }
    }
    
    /**
     * Appends and returns a new empty "SignatureMethod" element
     */
    public SignatureMethodType addNewSignatureMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureMethodType target = null;
            target = (SignatureMethodType)get_store().add_element_user(SIGNATUREMETHOD$0);
            return target;
        }
    }
}
