/*
 * XML Type:  PubKeyDigestType
 * Namespace: http://www.ebics.org/H003
 * Java type: PubKeyDigestType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.PubKeyDigestType;

/**
 * An XML PubKeyDigestType(@http://www.ebics.org/H003).
 *
 * This is an atomic type that is a restriction of PubKeyDigestType.
 */
public class PubKeyDigestTypeImpl extends org.apache.xmlbeans.impl.values.JavaBase64HolderEx implements PubKeyDigestType
{
    private static final long serialVersionUID = 1L;
    
    public PubKeyDigestTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType, true);
    }
    
    protected PubKeyDigestTypeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
    {
        super(sType, b);
    }
    
    private static final javax.xml.namespace.QName ALGORITHM$0 = 
        new javax.xml.namespace.QName("", "Algorithm");
    
    
    /**
     * Gets the "Algorithm" attribute
     */
    public java.lang.String getAlgorithm()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ALGORITHM$0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Algorithm" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetAlgorithm()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ALGORITHM$0);
            return target;
        }
    }
    
    /**
     * Sets the "Algorithm" attribute
     */
    public void setAlgorithm(java.lang.String algorithm)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ALGORITHM$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ALGORITHM$0);
            }
            target.setStringValue(algorithm);
        }
    }
    
    /**
     * Sets (as xml) the "Algorithm" attribute
     */
    public void xsetAlgorithm(org.apache.xmlbeans.XmlAnyURI algorithm)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ALGORITHM$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(ALGORITHM$0);
            }
            target.set(algorithm);
        }
    }
}
