/*
 * An XML document type.
 * Localname: SignedInfo
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: SignedInfoDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.SignedInfoDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.SignedInfoType;

/**
 * A document containing one SignedInfo(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class SignedInfoDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignedInfoDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignedInfoDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNEDINFO$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignedInfo");
    
    
    /**
     * Gets the "SignedInfo" element
     */
    public SignedInfoType getSignedInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignedInfoType target = null;
            target = (SignedInfoType)get_store().find_element_user(SIGNEDINFO$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "SignedInfo" element
     */
    public void setSignedInfo(SignedInfoType signedInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignedInfoType target = null;
            target = (SignedInfoType)get_store().find_element_user(SIGNEDINFO$0, 0);
            if (target == null)
            {
                target = (SignedInfoType)get_store().add_element_user(SIGNEDINFO$0);
            }
            target.set(signedInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "SignedInfo" element
     */
    public SignedInfoType addNewSignedInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignedInfoType target = null;
            target = (SignedInfoType)get_store().add_element_user(SIGNEDINFO$0);
            return target;
        }
    }
}
