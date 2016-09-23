/*
 * An XML document type.
 * Localname: KeyValue
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: KeyValueDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.KeyValueDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.KeyValueType;

/**
 * A document containing one KeyValue(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class KeyValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements KeyValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public KeyValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName KEYVALUE$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "KeyValue");
    
    
    /**
     * Gets the "KeyValue" element
     */
    public KeyValueType getKeyValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            KeyValueType target = null;
            target = (KeyValueType)get_store().find_element_user(KEYVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "KeyValue" element
     */
    public void setKeyValue(KeyValueType keyValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            KeyValueType target = null;
            target = (KeyValueType)get_store().find_element_user(KEYVALUE$0, 0);
            if (target == null)
            {
                target = (KeyValueType)get_store().add_element_user(KEYVALUE$0);
            }
            target.set(keyValue);
        }
    }
    
    /**
     * Appends and returns a new empty "KeyValue" element
     */
    public KeyValueType addNewKeyValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            KeyValueType target = null;
            target = (KeyValueType)get_store().add_element_user(KEYVALUE$0);
            return target;
        }
    }
}
