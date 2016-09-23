/*
 * An XML document type.
 * Localname: Reference
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: ReferenceDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.ReferenceDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.ReferenceType;

/**
 * A document containing one Reference(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class ReferenceDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements ReferenceDocument
{
    private static final long serialVersionUID = 1L;
    
    public ReferenceDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName REFERENCE$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Reference");
    
    
    /**
     * Gets the "Reference" element
     */
    public ReferenceType getReference()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReferenceType target = null;
            target = (ReferenceType)get_store().find_element_user(REFERENCE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "Reference" element
     */
    public void setReference(ReferenceType reference)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReferenceType target = null;
            target = (ReferenceType)get_store().find_element_user(REFERENCE$0, 0);
            if (target == null)
            {
                target = (ReferenceType)get_store().add_element_user(REFERENCE$0);
            }
            target.set(reference);
        }
    }
    
    /**
     * Appends and returns a new empty "Reference" element
     */
    public ReferenceType addNewReference()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReferenceType target = null;
            target = (ReferenceType)get_store().add_element_user(REFERENCE$0);
            return target;
        }
    }
}
