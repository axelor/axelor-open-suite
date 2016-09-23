/*
 * An XML document type.
 * Localname: DSAKeyValue
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: DSAKeyValueDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.DSAKeyValueDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.DSAKeyValueType;

/**
 * A document containing one DSAKeyValue(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class DSAKeyValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements DSAKeyValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public DSAKeyValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName DSAKEYVALUE$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "DSAKeyValue");
    
    
    /**
     * Gets the "DSAKeyValue" element
     */
    public DSAKeyValueType getDSAKeyValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DSAKeyValueType target = null;
            target = (DSAKeyValueType)get_store().find_element_user(DSAKEYVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "DSAKeyValue" element
     */
    public void setDSAKeyValue(DSAKeyValueType dsaKeyValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            DSAKeyValueType target = null;
            target = (DSAKeyValueType)get_store().find_element_user(DSAKEYVALUE$0, 0);
            if (target == null)
            {
                target = (DSAKeyValueType)get_store().add_element_user(DSAKEYVALUE$0);
            }
            target.set(dsaKeyValue);
        }
    }
    
    /**
     * Appends and returns a new empty "DSAKeyValue" element
     */
    public DSAKeyValueType addNewDSAKeyValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DSAKeyValueType target = null;
            target = (DSAKeyValueType)get_store().add_element_user(DSAKEYVALUE$0);
            return target;
        }
    }
}
