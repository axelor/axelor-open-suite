/*
 * XML Type:  FileFormatType
 * Namespace: http://www.ebics.org/H003
 * Java type: FileFormatType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.CountryCodeType;
import com.axelor.apps.account.ebics.schema.h003.FileFormatType;

/**
 * An XML FileFormatType(@http://www.ebics.org/H003).
 *
 * This is an atomic type that is a restriction of FileFormatType.
 */
public class FileFormatTypeImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements FileFormatType
{
    private static final long serialVersionUID = 1L;
    
    public FileFormatTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType, true);
    }
    
    protected FileFormatTypeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
    {
        super(sType, b);
    }
    
    private static final javax.xml.namespace.QName COUNTRYCODE$0 = 
        new javax.xml.namespace.QName("", "CountryCode");
    
    
    /**
     * Gets the "CountryCode" attribute
     */
    public java.lang.String getCountryCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(COUNTRYCODE$0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "CountryCode" attribute
     */
    public CountryCodeType xgetCountryCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            CountryCodeType target = null;
            target = (CountryCodeType)get_store().find_attribute_user(COUNTRYCODE$0);
            return target;
        }
    }
    
    /**
     * True if has "CountryCode" attribute
     */
    public boolean isSetCountryCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(COUNTRYCODE$0) != null;
        }
    }
    
    /**
     * Sets the "CountryCode" attribute
     */
    public void setCountryCode(java.lang.String countryCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(COUNTRYCODE$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(COUNTRYCODE$0);
            }
            target.setStringValue(countryCode);
        }
    }
    
    /**
     * Sets (as xml) the "CountryCode" attribute
     */
    public void xsetCountryCode(CountryCodeType countryCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            CountryCodeType target = null;
            target = (CountryCodeType)get_store().find_attribute_user(COUNTRYCODE$0);
            if (target == null)
            {
                target = (CountryCodeType)get_store().add_attribute_user(COUNTRYCODE$0);
            }
            target.set(countryCode);
        }
    }
    
    /**
     * Unsets the "CountryCode" attribute
     */
    public void unsetCountryCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(COUNTRYCODE$0);
        }
    }
}
