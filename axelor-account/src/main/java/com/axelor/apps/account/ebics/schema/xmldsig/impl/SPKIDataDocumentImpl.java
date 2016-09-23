/*
 * An XML document type.
 * Localname: SPKIData
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: SPKIDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.SPKIDataDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.SPKIDataType;

/**
 * A document containing one SPKIData(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class SPKIDataDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SPKIDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public SPKIDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SPKIDATA$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SPKIData");
    
    
    /**
     * Gets the "SPKIData" element
     */
    public SPKIDataType getSPKIData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SPKIDataType target = null;
            target = (SPKIDataType)get_store().find_element_user(SPKIDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "SPKIData" element
     */
    public void setSPKIData(SPKIDataType spkiData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SPKIDataType target = null;
            target = (SPKIDataType)get_store().find_element_user(SPKIDATA$0, 0);
            if (target == null)
            {
                target = (SPKIDataType)get_store().add_element_user(SPKIDATA$0);
            }
            target.set(spkiData);
        }
    }
    
    /**
     * Appends and returns a new empty "SPKIData" element
     */
    public SPKIDataType addNewSPKIData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SPKIDataType target = null;
            target = (SPKIDataType)get_store().add_element_user(SPKIDATA$0);
            return target;
        }
    }
}
