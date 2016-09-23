/*
 * An XML document type.
 * Localname: PGPData
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: PGPDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.PGPDataDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.PGPDataType;

/**
 * A document containing one PGPData(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class PGPDataDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements PGPDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public PGPDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PGPDATA$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "PGPData");
    
    
    /**
     * Gets the "PGPData" element
     */
    public PGPDataType getPGPData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            PGPDataType target = null;
            target = (PGPDataType)get_store().find_element_user(PGPDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "PGPData" element
     */
    public void setPGPData(PGPDataType pgpData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            PGPDataType target = null;
            target = (PGPDataType)get_store().find_element_user(PGPDATA$0, 0);
            if (target == null)
            {
                target = (PGPDataType)get_store().add_element_user(PGPDATA$0);
            }
            target.set(pgpData);
        }
    }
    
    /**
     * Appends and returns a new empty "PGPData" element
     */
    public PGPDataType addNewPGPData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            PGPDataType target = null;
            target = (PGPDataType)get_store().add_element_user(PGPDATA$0);
            return target;
        }
    }
}
