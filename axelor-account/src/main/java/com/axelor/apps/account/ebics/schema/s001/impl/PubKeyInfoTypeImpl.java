/*
 * XML Type:  PubKeyInfoType
 * Namespace: http://www.ebics.org/S001
 * Java type: PubKeyInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.s001.impl;

import com.axelor.apps.account.ebics.schema.s001.PubKeyInfoType;
import com.axelor.apps.account.ebics.schema.s001.PubKeyValueType;
import com.axelor.apps.account.ebics.schema.xmldsig.X509DataType;

/**
 * An XML PubKeyInfoType(@http://www.ebics.org/S001).
 *
 * This is a complex type.
 */
public class PubKeyInfoTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements PubKeyInfoType
{
    private static final long serialVersionUID = 1L;
    
    public PubKeyInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName X509DATA$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509Data");
    private static final javax.xml.namespace.QName PUBKEYVALUE$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "PubKeyValue");
    
    
    /**
     * Gets the "X509Data" element
     */
    public X509DataType getX509Data()
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509DataType target = null;
            target = (X509DataType)get_store().find_element_user(X509DATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "X509Data" element
     */
    public boolean isSetX509Data()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(X509DATA$0) != 0;
        }
    }
    
    /**
     * Sets the "X509Data" element
     */
    public void setX509Data(X509DataType x509Data)
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509DataType target = null;
            target = (X509DataType)get_store().find_element_user(X509DATA$0, 0);
            if (target == null)
            {
                target = (X509DataType)get_store().add_element_user(X509DATA$0);
            }
            target.set(x509Data);
        }
    }
    
    /**
     * Appends and returns a new empty "X509Data" element
     */
    public X509DataType addNewX509Data()
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509DataType target = null;
            target = (X509DataType)get_store().add_element_user(X509DATA$0);
            return target;
        }
    }
    
    /**
     * Unsets the "X509Data" element
     */
    public void unsetX509Data()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(X509DATA$0, 0);
        }
    }
    
    /**
     * Gets the "PubKeyValue" element
     */
    public PubKeyValueType getPubKeyValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            PubKeyValueType target = null;
            target = (PubKeyValueType)get_store().find_element_user(PUBKEYVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "PubKeyValue" element
     */
    public void setPubKeyValue(PubKeyValueType pubKeyValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            PubKeyValueType target = null;
            target = (PubKeyValueType)get_store().find_element_user(PUBKEYVALUE$2, 0);
            if (target == null)
            {
                target = (PubKeyValueType)get_store().add_element_user(PUBKEYVALUE$2);
            }
            target.set(pubKeyValue);
        }
    }
    
    /**
     * Appends and returns a new empty "PubKeyValue" element
     */
    public PubKeyValueType addNewPubKeyValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            PubKeyValueType target = null;
            target = (PubKeyValueType)get_store().add_element_user(PUBKEYVALUE$2);
            return target;
        }
    }
}
