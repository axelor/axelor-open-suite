/*
 * An XML document type.
 * Localname: Transform
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: TransformDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.TransformDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.TransformType;

/**
 * A document containing one Transform(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class TransformDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements TransformDocument
{
    private static final long serialVersionUID = 1L;
    
    public TransformDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName TRANSFORM$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Transform");
    
    
    /**
     * Gets the "Transform" element
     */
    public TransformType getTransform()
    {
        synchronized (monitor())
        {
            check_orphaned();
            TransformType target = null;
            target = (TransformType)get_store().find_element_user(TRANSFORM$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "Transform" element
     */
    public void setTransform(TransformType transform)
    {
        synchronized (monitor())
        {
            check_orphaned();
            TransformType target = null;
            target = (TransformType)get_store().find_element_user(TRANSFORM$0, 0);
            if (target == null)
            {
                target = (TransformType)get_store().add_element_user(TRANSFORM$0);
            }
            target.set(transform);
        }
    }
    
    /**
     * Appends and returns a new empty "Transform" element
     */
    public TransformType addNewTransform()
    {
        synchronized (monitor())
        {
            check_orphaned();
            TransformType target = null;
            target = (TransformType)get_store().add_element_user(TRANSFORM$0);
            return target;
        }
    }
}
