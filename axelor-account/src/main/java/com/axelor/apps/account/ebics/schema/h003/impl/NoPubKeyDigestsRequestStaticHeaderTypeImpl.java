/*
 * XML Type:  NoPubKeyDigestsRequestStaticHeaderType
 * Namespace: http://www.ebics.org/H003
 * Java type: NoPubKeyDigestsRequestStaticHeaderType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.NoPubKeyDigestsRequestStaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.NonceType;
import com.axelor.apps.account.ebics.schema.h003.TimestampType;

/**
 * An XML NoPubKeyDigestsRequestStaticHeaderType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class NoPubKeyDigestsRequestStaticHeaderTypeImpl extends StaticHeaderBaseTypeImpl implements NoPubKeyDigestsRequestStaticHeaderType
{
    private static final long serialVersionUID = 1L;
    
    public NoPubKeyDigestsRequestStaticHeaderTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName NONCE$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Nonce");
    private static final javax.xml.namespace.QName TIMESTAMP$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Timestamp");
    
    
    /**
     * Gets the "Nonce" element
     */
    public byte[] getNonce()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NONCE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "Nonce" element
     */
    public NonceType xgetNonce()
    {
        synchronized (monitor())
        {
            check_orphaned();
            NonceType target = null;
            target = (NonceType)get_store().find_element_user(NONCE$0, 0);
            return target;
        }
    }
    
    /**
     * True if has "Nonce" element
     */
    public boolean isSetNonce()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(NONCE$0) != 0;
        }
    }
    
    /**
     * Sets the "Nonce" element
     */
    public void setNonce(byte[] nonce)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NONCE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(NONCE$0);
            }
            target.setByteArrayValue(nonce);
        }
    }
    
    /**
     * Sets (as xml) the "Nonce" element
     */
    public void xsetNonce(NonceType nonce)
    {
        synchronized (monitor())
        {
            check_orphaned();
            NonceType target = null;
            target = (NonceType)get_store().find_element_user(NONCE$0, 0);
            if (target == null)
            {
                target = (NonceType)get_store().add_element_user(NONCE$0);
            }
            target.set(nonce);
        }
    }
    
    /**
     * Unsets the "Nonce" element
     */
    public void unsetNonce()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(NONCE$0, 0);
        }
    }
    
    /**
     * Gets the "Timestamp" element
     */
    public java.util.Calendar getTimestamp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TIMESTAMP$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getCalendarValue();
        }
    }
    
    /**
     * Gets (as xml) the "Timestamp" element
     */
    public TimestampType xgetTimestamp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            TimestampType target = null;
            target = (TimestampType)get_store().find_element_user(TIMESTAMP$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "Timestamp" element
     */
    public boolean isSetTimestamp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(TIMESTAMP$2) != 0;
        }
    }
    
    /**
     * Sets the "Timestamp" element
     */
    public void setTimestamp(java.util.Calendar timestamp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TIMESTAMP$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(TIMESTAMP$2);
            }
            target.setCalendarValue(timestamp);
        }
    }
    
    /**
     * Sets (as xml) the "Timestamp" element
     */
    public void xsetTimestamp(TimestampType timestamp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            TimestampType target = null;
            target = (TimestampType)get_store().find_element_user(TIMESTAMP$2, 0);
            if (target == null)
            {
                target = (TimestampType)get_store().add_element_user(TIMESTAMP$2);
            }
            target.set(timestamp);
        }
    }
    
    /**
     * Unsets the "Timestamp" element
     */
    public void unsetTimestamp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(TIMESTAMP$2, 0);
        }
    }
}
