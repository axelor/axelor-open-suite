/*
 * XML Type:  PubKeyValueType
 * Namespace: http://www.ebics.org/S001
 * Java type: PubKeyValueType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.s001.impl;

import com.axelor.apps.account.ebics.schema.s001.PubKeyValueType;
import com.axelor.apps.account.ebics.schema.s001.TimestampType;
import com.axelor.apps.account.ebics.schema.xmldsig.RSAKeyValueType;

/**
 * An XML PubKeyValueType(@http://www.ebics.org/S001).
 *
 * This is a complex type.
 */
public class PubKeyValueTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements PubKeyValueType
{
    private static final long serialVersionUID = 1L;
    
    public PubKeyValueTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RSAKEYVALUE$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "RSAKeyValue");
    private static final javax.xml.namespace.QName TIMESTAMP$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "TimeStamp");
    
    
    /**
     * Gets the "RSAKeyValue" element
     */
    public RSAKeyValueType getRSAKeyValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            RSAKeyValueType target = null;
            target = (RSAKeyValueType)get_store().find_element_user(RSAKEYVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "RSAKeyValue" element
     */
    public void setRSAKeyValue(RSAKeyValueType rsaKeyValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            RSAKeyValueType target = null;
            target = (RSAKeyValueType)get_store().find_element_user(RSAKEYVALUE$0, 0);
            if (target == null)
            {
                target = (RSAKeyValueType)get_store().add_element_user(RSAKEYVALUE$0);
            }
            target.set(rsaKeyValue);
        }
    }
    
    /**
     * Appends and returns a new empty "RSAKeyValue" element
     */
    public RSAKeyValueType addNewRSAKeyValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            RSAKeyValueType target = null;
            target = (RSAKeyValueType)get_store().add_element_user(RSAKEYVALUE$0);
            return target;
        }
    }
    
    /**
     * Gets the "TimeStamp" element
     */
    public java.util.Calendar getTimeStamp()
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
     * Gets (as xml) the "TimeStamp" element
     */
    public TimestampType xgetTimeStamp()
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
     * True if has "TimeStamp" element
     */
    public boolean isSetTimeStamp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(TIMESTAMP$2) != 0;
        }
    }
    
    /**
     * Sets the "TimeStamp" element
     */
    public void setTimeStamp(java.util.Calendar timeStamp)
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
            target.setCalendarValue(timeStamp);
        }
    }
    
    /**
     * Sets (as xml) the "TimeStamp" element
     */
    public void xsetTimeStamp(TimestampType timeStamp)
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
            target.set(timeStamp);
        }
    }
    
    /**
     * Unsets the "TimeStamp" element
     */
    public void unsetTimeStamp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(TIMESTAMP$2, 0);
        }
    }
}
