/*
 * XML Type:  EncryptionPubKeyInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: EncryptionPubKeyInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.EncryptionPubKeyInfoType;
import com.axelor.apps.account.ebics.schema.h003.EncryptionVersionType;

/**
 * An XML EncryptionPubKeyInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class EncryptionPubKeyInfoTypeImpl extends PubKeyInfoTypeImpl implements EncryptionPubKeyInfoType
{
    private static final long serialVersionUID = 1L;
    
    public EncryptionPubKeyInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENCRYPTIONVERSION$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "EncryptionVersion");
    
    
    /**
     * Gets the "EncryptionVersion" element
     */
    public java.lang.String getEncryptionVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENCRYPTIONVERSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "EncryptionVersion" element
     */
    public EncryptionVersionType xgetEncryptionVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            EncryptionVersionType target = null;
            target = (EncryptionVersionType)get_store().find_element_user(ENCRYPTIONVERSION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "EncryptionVersion" element
     */
    public void setEncryptionVersion(java.lang.String encryptionVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENCRYPTIONVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENCRYPTIONVERSION$0);
            }
            target.setStringValue(encryptionVersion);
        }
    }
    
    /**
     * Sets (as xml) the "EncryptionVersion" element
     */
    public void xsetEncryptionVersion(EncryptionVersionType encryptionVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            EncryptionVersionType target = null;
            target = (EncryptionVersionType)get_store().find_element_user(ENCRYPTIONVERSION$0, 0);
            if (target == null)
            {
                target = (EncryptionVersionType)get_store().add_element_user(ENCRYPTIONVERSION$0);
            }
            target.set(encryptionVersion);
        }
    }
}
