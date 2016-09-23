/*
 * An XML document type.
 * Localname: KeyName
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: KeyNameDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.KeyNameDocument;

/**
 * A document containing one KeyName(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class KeyNameDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements KeyNameDocument
{
    private static final long serialVersionUID = 1L;
    
    public KeyNameDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName KEYNAME$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "KeyName");
    
    
    /**
     * Gets the "KeyName" element
     */
    public java.lang.String getKeyName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(KEYNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "KeyName" element
     */
    public org.apache.xmlbeans.XmlString xgetKeyName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(KEYNAME$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "KeyName" element
     */
    public void setKeyName(java.lang.String keyName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(KEYNAME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(KEYNAME$0);
            }
            target.setStringValue(keyName);
        }
    }
    
    /**
     * Sets (as xml) the "KeyName" element
     */
    public void xsetKeyName(org.apache.xmlbeans.XmlString keyName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(KEYNAME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(KEYNAME$0);
            }
            target.set(keyName);
        }
    }
}
