/*
 * An XML document type.
 * Localname: Transforms
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: TransformsDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.TransformsDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.TransformsType;

/**
 * A document containing one Transforms(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class TransformsDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements TransformsDocument
{
    private static final long serialVersionUID = 1L;
    
    public TransformsDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName TRANSFORMS$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Transforms");
    
    
    /**
     * Gets the "Transforms" element
     */
    public TransformsType getTransforms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            TransformsType target = null;
            target = (TransformsType)get_store().find_element_user(TRANSFORMS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "Transforms" element
     */
    public void setTransforms(TransformsType transforms)
    {
        synchronized (monitor())
        {
            check_orphaned();
            TransformsType target = null;
            target = (TransformsType)get_store().find_element_user(TRANSFORMS$0, 0);
            if (target == null)
            {
                target = (TransformsType)get_store().add_element_user(TRANSFORMS$0);
            }
            target.set(transforms);
        }
    }
    
    /**
     * Appends and returns a new empty "Transforms" element
     */
    public TransformsType addNewTransforms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            TransformsType target = null;
            target = (TransformsType)get_store().add_element_user(TRANSFORMS$0);
            return target;
        }
    }
}
