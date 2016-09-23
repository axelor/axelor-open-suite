/*
 * XML Type:  SignaturePubKeyInfoType
 * Namespace: http://www.ebics.org/S001
 * Java type: SignaturePubKeyInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.s001.impl;

import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyInfoType;
import com.axelor.apps.account.ebics.schema.s001.SignatureVersionType;

/**
 * An XML SignaturePubKeyInfoType(@http://www.ebics.org/S001).
 *
 * This is a complex type.
 */
public class SignaturePubKeyInfoTypeImpl extends PubKeyInfoTypeImpl implements SignaturePubKeyInfoType
{
    private static final long serialVersionUID = 1L;
    
    public SignaturePubKeyInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREVERSION$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "SignatureVersion");
    
    
    /**
     * Gets the "SignatureVersion" element
     */
    public java.lang.String getSignatureVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREVERSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "SignatureVersion" element
     */
    public SignatureVersionType xgetSignatureVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureVersionType target = null;
            target = (SignatureVersionType)get_store().find_element_user(SIGNATUREVERSION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "SignatureVersion" element
     */
    public void setSignatureVersion(java.lang.String signatureVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SIGNATUREVERSION$0);
            }
            target.setStringValue(signatureVersion);
        }
    }
    
    /**
     * Sets (as xml) the "SignatureVersion" element
     */
    public void xsetSignatureVersion(SignatureVersionType signatureVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureVersionType target = null;
            target = (SignatureVersionType)get_store().find_element_user(SIGNATUREVERSION$0, 0);
            if (target == null)
            {
                target = (SignatureVersionType)get_store().add_element_user(SIGNATUREVERSION$0);
            }
            target.set(signatureVersion);
        }
    }
}
