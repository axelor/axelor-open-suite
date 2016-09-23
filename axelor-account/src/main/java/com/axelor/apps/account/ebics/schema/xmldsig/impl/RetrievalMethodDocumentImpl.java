/*
 * An XML document type.
 * Localname: RetrievalMethod
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: RetrievalMethodDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.RetrievalMethodDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.RetrievalMethodType;

/**
 * A document containing one RetrievalMethod(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class RetrievalMethodDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements RetrievalMethodDocument
{
    private static final long serialVersionUID = 1L;
    
    public RetrievalMethodDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RETRIEVALMETHOD$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "RetrievalMethod");
    
    
    /**
     * Gets the "RetrievalMethod" element
     */
    public RetrievalMethodType getRetrievalMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            RetrievalMethodType target = null;
            target = (RetrievalMethodType)get_store().find_element_user(RETRIEVALMETHOD$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "RetrievalMethod" element
     */
    public void setRetrievalMethod(RetrievalMethodType retrievalMethod)
    {
        synchronized (monitor())
        {
            check_orphaned();
            RetrievalMethodType target = null;
            target = (RetrievalMethodType)get_store().find_element_user(RETRIEVALMETHOD$0, 0);
            if (target == null)
            {
                target = (RetrievalMethodType)get_store().add_element_user(RETRIEVALMETHOD$0);
            }
            target.set(retrievalMethod);
        }
    }
    
    /**
     * Appends and returns a new empty "RetrievalMethod" element
     */
    public RetrievalMethodType addNewRetrievalMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            RetrievalMethodType target = null;
            target = (RetrievalMethodType)get_store().add_element_user(RETRIEVALMETHOD$0);
            return target;
        }
    }
}
