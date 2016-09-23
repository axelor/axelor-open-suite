/*
 * XML Type:  AuthenticationPubKeyInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: AuthenticationPubKeyInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.AuthenticationPubKeyInfoType;
import com.axelor.apps.account.ebics.schema.h003.AuthenticationVersionType;

/**
 * An XML AuthenticationPubKeyInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class AuthenticationPubKeyInfoTypeImpl extends PubKeyInfoTypeImpl implements AuthenticationPubKeyInfoType
{
    private static final long serialVersionUID = 1L;
    
    public AuthenticationPubKeyInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AUTHENTICATIONVERSION$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "AuthenticationVersion");
    
    
    /**
     * Gets the "AuthenticationVersion" element
     */
    public java.lang.String getAuthenticationVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AUTHENTICATIONVERSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "AuthenticationVersion" element
     */
    public AuthenticationVersionType xgetAuthenticationVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AuthenticationVersionType target = null;
            target = (AuthenticationVersionType)get_store().find_element_user(AUTHENTICATIONVERSION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "AuthenticationVersion" element
     */
    public void setAuthenticationVersion(java.lang.String authenticationVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AUTHENTICATIONVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(AUTHENTICATIONVERSION$0);
            }
            target.setStringValue(authenticationVersion);
        }
    }
    
    /**
     * Sets (as xml) the "AuthenticationVersion" element
     */
    public void xsetAuthenticationVersion(AuthenticationVersionType authenticationVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AuthenticationVersionType target = null;
            target = (AuthenticationVersionType)get_store().find_element_user(AUTHENTICATIONVERSION$0, 0);
            if (target == null)
            {
                target = (AuthenticationVersionType)get_store().add_element_user(AUTHENTICATIONVERSION$0);
            }
            target.set(authenticationVersion);
        }
    }
}
