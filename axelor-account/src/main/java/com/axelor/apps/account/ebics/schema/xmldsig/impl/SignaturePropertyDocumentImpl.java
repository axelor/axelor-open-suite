/*
 * An XML document type.
 * Localname: SignatureProperty
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: SignaturePropertyDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.SignaturePropertyDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.SignaturePropertyType;

/**
 * A document containing one SignatureProperty(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class SignaturePropertyDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignaturePropertyDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignaturePropertyDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREPROPERTY$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignatureProperty");
    
    
    /**
     * Gets the "SignatureProperty" element
     */
    public SignaturePropertyType getSignatureProperty()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePropertyType target = null;
            target = (SignaturePropertyType)get_store().find_element_user(SIGNATUREPROPERTY$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "SignatureProperty" element
     */
    public void setSignatureProperty(SignaturePropertyType signatureProperty)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePropertyType target = null;
            target = (SignaturePropertyType)get_store().find_element_user(SIGNATUREPROPERTY$0, 0);
            if (target == null)
            {
                target = (SignaturePropertyType)get_store().add_element_user(SIGNATUREPROPERTY$0);
            }
            target.set(signatureProperty);
        }
    }
    
    /**
     * Appends and returns a new empty "SignatureProperty" element
     */
    public SignaturePropertyType addNewSignatureProperty()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePropertyType target = null;
            target = (SignaturePropertyType)get_store().add_element_user(SIGNATUREPROPERTY$0);
            return target;
        }
    }
}
