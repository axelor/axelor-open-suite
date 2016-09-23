/*
 * XML Type:  ProductElementType
 * Namespace: http://www.ebics.org/H003
 * Java type: ProductElementType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.InstituteIDType;
import com.axelor.apps.account.ebics.schema.h003.LanguageType;
import com.axelor.apps.account.ebics.schema.h003.ProductElementType;

/**
 * An XML ProductElementType(@http://www.ebics.org/H003).
 *
 * This is an atomic type that is a restriction of ProductElementType.
 */
public class ProductElementTypeImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements ProductElementType
{
    private static final long serialVersionUID = 1L;
    
    public ProductElementTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType, true);
    }
    
    protected ProductElementTypeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
    {
        super(sType, b);
    }
    
    private static final javax.xml.namespace.QName LANGUAGE$0 = 
        new javax.xml.namespace.QName("", "Language");
    private static final javax.xml.namespace.QName INSTITUTEID$2 = 
        new javax.xml.namespace.QName("", "InstituteID");
    
    
    /**
     * Gets the "Language" attribute
     */
    public java.lang.String getLanguage()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LANGUAGE$0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Language" attribute
     */
    public LanguageType xgetLanguage()
    {
        synchronized (monitor())
        {
            check_orphaned();
            LanguageType target = null;
            target = (LanguageType)get_store().find_attribute_user(LANGUAGE$0);
            return target;
        }
    }
    
    /**
     * Sets the "Language" attribute
     */
    public void setLanguage(java.lang.String language)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LANGUAGE$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(LANGUAGE$0);
            }
            target.setStringValue(language);
        }
    }
    
    /**
     * Sets (as xml) the "Language" attribute
     */
    public void xsetLanguage(LanguageType language)
    {
        synchronized (monitor())
        {
            check_orphaned();
            LanguageType target = null;
            target = (LanguageType)get_store().find_attribute_user(LANGUAGE$0);
            if (target == null)
            {
                target = (LanguageType)get_store().add_attribute_user(LANGUAGE$0);
            }
            target.set(language);
        }
    }
    
    /**
     * Gets the "InstituteID" attribute
     */
    public java.lang.String getInstituteID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(INSTITUTEID$2);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "InstituteID" attribute
     */
    public InstituteIDType xgetInstituteID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            InstituteIDType target = null;
            target = (InstituteIDType)get_store().find_attribute_user(INSTITUTEID$2);
            return target;
        }
    }
    
    /**
     * True if has "InstituteID" attribute
     */
    public boolean isSetInstituteID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(INSTITUTEID$2) != null;
        }
    }
    
    /**
     * Sets the "InstituteID" attribute
     */
    public void setInstituteID(java.lang.String instituteID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(INSTITUTEID$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(INSTITUTEID$2);
            }
            target.setStringValue(instituteID);
        }
    }
    
    /**
     * Sets (as xml) the "InstituteID" attribute
     */
    public void xsetInstituteID(InstituteIDType instituteID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            InstituteIDType target = null;
            target = (InstituteIDType)get_store().find_attribute_user(INSTITUTEID$2);
            if (target == null)
            {
                target = (InstituteIDType)get_store().add_attribute_user(INSTITUTEID$2);
            }
            target.set(instituteID);
        }
    }
    
    /**
     * Unsets the "InstituteID" attribute
     */
    public void unsetInstituteID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(INSTITUTEID$2);
        }
    }
}
