/*
 * An XML document type.
 * Localname: CanonicalizationMethod
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: CanonicalizationMethodDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.CanonicalizationMethodDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.CanonicalizationMethodType;

/**
 * A document containing one CanonicalizationMethod(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class CanonicalizationMethodDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements CanonicalizationMethodDocument
{
    private static final long serialVersionUID = 1L;
    
    public CanonicalizationMethodDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CANONICALIZATIONMETHOD$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "CanonicalizationMethod");
    
    
    /**
     * Gets the "CanonicalizationMethod" element
     */
    public CanonicalizationMethodType getCanonicalizationMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            CanonicalizationMethodType target = null;
            target = (CanonicalizationMethodType)get_store().find_element_user(CANONICALIZATIONMETHOD$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "CanonicalizationMethod" element
     */
    public void setCanonicalizationMethod(CanonicalizationMethodType canonicalizationMethod)
    {
        synchronized (monitor())
        {
            check_orphaned();
            CanonicalizationMethodType target = null;
            target = (CanonicalizationMethodType)get_store().find_element_user(CANONICALIZATIONMETHOD$0, 0);
            if (target == null)
            {
                target = (CanonicalizationMethodType)get_store().add_element_user(CANONICALIZATIONMETHOD$0);
            }
            target.set(canonicalizationMethod);
        }
    }
    
    /**
     * Appends and returns a new empty "CanonicalizationMethod" element
     */
    public CanonicalizationMethodType addNewCanonicalizationMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            CanonicalizationMethodType target = null;
            target = (CanonicalizationMethodType)get_store().add_element_user(CANONICALIZATIONMETHOD$0);
            return target;
        }
    }
}
