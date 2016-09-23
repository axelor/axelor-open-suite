/*
 * An XML document type.
 * Localname: Manifest
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: ManifestDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.ManifestDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.ManifestType;

/**
 * A document containing one Manifest(@http://www.w3.org/2000/09/xmldsig#) element.
 *
 * This is a complex type.
 */
public class ManifestDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements ManifestDocument
{
    private static final long serialVersionUID = 1L;
    
    public ManifestDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName MANIFEST$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Manifest");
    
    
    /**
     * Gets the "Manifest" element
     */
    public ManifestType getManifest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ManifestType target = null;
            target = (ManifestType)get_store().find_element_user(MANIFEST$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "Manifest" element
     */
    public void setManifest(ManifestType manifest)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ManifestType target = null;
            target = (ManifestType)get_store().find_element_user(MANIFEST$0, 0);
            if (target == null)
            {
                target = (ManifestType)get_store().add_element_user(MANIFEST$0);
            }
            target.set(manifest);
        }
    }
    
    /**
     * Appends and returns a new empty "Manifest" element
     */
    public ManifestType addNewManifest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ManifestType target = null;
            target = (ManifestType)get_store().add_element_user(MANIFEST$0);
            return target;
        }
    }
}
