/*
 * An XML document type.
 * Localname: SignaturePubKeyInfo
 * Namespace: http://www.ebics.org/S001
 * Java type: SignaturePubKeyInfoDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.s001.impl;

import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyInfoDocument;
import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyInfoType;

/**
 * A document containing one SignaturePubKeyInfo(@http://www.ebics.org/S001) element.
 *
 * This is a complex type.
 */
public class SignaturePubKeyInfoDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignaturePubKeyInfoDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignaturePubKeyInfoDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREPUBKEYINFO$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "SignaturePubKeyInfo");
    
    
    /**
     * Gets the "SignaturePubKeyInfo" element
     */
    public SignaturePubKeyInfoType getSignaturePubKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePubKeyInfoType target = null;
            target = (SignaturePubKeyInfoType)get_store().find_element_user(SIGNATUREPUBKEYINFO$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "SignaturePubKeyInfo" element
     */
    public void setSignaturePubKeyInfo(SignaturePubKeyInfoType signaturePubKeyInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePubKeyInfoType target = null;
            target = (SignaturePubKeyInfoType)get_store().find_element_user(SIGNATUREPUBKEYINFO$0, 0);
            if (target == null)
            {
                target = (SignaturePubKeyInfoType)get_store().add_element_user(SIGNATUREPUBKEYINFO$0);
            }
            target.set(signaturePubKeyInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "SignaturePubKeyInfo" element
     */
    public SignaturePubKeyInfoType addNewSignaturePubKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePubKeyInfoType target = null;
            target = (SignaturePubKeyInfoType)get_store().add_element_user(SIGNATUREPUBKEYINFO$0);
            return target;
        }
    }
}
