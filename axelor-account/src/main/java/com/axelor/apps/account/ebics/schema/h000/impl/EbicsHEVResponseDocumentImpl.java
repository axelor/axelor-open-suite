/*
 * An XML document type.
 * Localname: ebicsHEVResponse
 * Namespace: http://www.ebics.org/H000
 * Java type: EbicsHEVResponseDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h000.impl;

import com.axelor.apps.account.ebics.schema.h000.EbicsHEVResponseDocument;
import com.axelor.apps.account.ebics.schema.h000.HEVResponseDataType;

/**
 * A document containing one ebicsHEVResponse(@http://www.ebics.org/H000) element.
 *
 * This is a complex type.
 */
public class EbicsHEVResponseDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements EbicsHEVResponseDocument
{
    private static final long serialVersionUID = 1L;
    
    public EbicsHEVResponseDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EBICSHEVRESPONSE$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H000", "ebicsHEVResponse");
    
    
    /**
     * Gets the "ebicsHEVResponse" element
     */
    public HEVResponseDataType getEbicsHEVResponse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HEVResponseDataType target = null;
            target = (HEVResponseDataType)get_store().find_element_user(EBICSHEVRESPONSE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "ebicsHEVResponse" element
     */
    public void setEbicsHEVResponse(HEVResponseDataType ebicsHEVResponse)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HEVResponseDataType target = null;
            target = (HEVResponseDataType)get_store().find_element_user(EBICSHEVRESPONSE$0, 0);
            if (target == null)
            {
                target = (HEVResponseDataType)get_store().add_element_user(EBICSHEVRESPONSE$0);
            }
            target.set(ebicsHEVResponse);
        }
    }
    
    /**
     * Appends and returns a new empty "ebicsHEVResponse" element
     */
    public HEVResponseDataType addNewEbicsHEVResponse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HEVResponseDataType target = null;
            target = (HEVResponseDataType)get_store().add_element_user(EBICSHEVRESPONSE$0);
            return target;
        }
    }
}
