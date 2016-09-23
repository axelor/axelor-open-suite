/*
 * XML Type:  ManifestType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: ManifestType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.ManifestType;
import com.axelor.apps.account.ebics.schema.xmldsig.ReferenceType;

/**
 * An XML ManifestType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class ManifestTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements ManifestType
{
    private static final long serialVersionUID = 1L;
    
    public ManifestTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName REFERENCE$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Reference");
    private static final javax.xml.namespace.QName ID$2 = 
        new javax.xml.namespace.QName("", "Id");
    
    
    /**
     * Gets array of all "Reference" elements
     */
    public ReferenceType[] getReferenceArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(REFERENCE$0, targetList);
            ReferenceType[] result = new ReferenceType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "Reference" element
     */
    public ReferenceType getReferenceArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReferenceType target = null;
            target = (ReferenceType)get_store().find_element_user(REFERENCE$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "Reference" element
     */
    public int sizeOfReferenceArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(REFERENCE$0);
        }
    }
    
    /**
     * Sets array of all "Reference" element
     */
    public void setReferenceArray(ReferenceType[] referenceArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(referenceArray, REFERENCE$0);
        }
    }
    
    /**
     * Sets ith "Reference" element
     */
    public void setReferenceArray(int i, ReferenceType reference)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReferenceType target = null;
            target = (ReferenceType)get_store().find_element_user(REFERENCE$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(reference);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "Reference" element
     */
    public ReferenceType insertNewReference(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReferenceType target = null;
            target = (ReferenceType)get_store().insert_element_user(REFERENCE$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "Reference" element
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
    
    /**
     * Removes the ith "Reference" element
     */
    public void removeReference(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(REFERENCE$0, i);
        }
    }
    
    /**
     * Gets the "Id" attribute
     */
    public java.lang.String getId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$2);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Id" attribute
     */
    public org.apache.xmlbeans.XmlID xgetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$2);
            return target;
        }
    }
    
    /**
     * True if has "Id" attribute
     */
    public boolean isSetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(ID$2) != null;
        }
    }
    
    /**
     * Sets the "Id" attribute
     */
    public void setId(java.lang.String id)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ID$2);
            }
            target.setStringValue(id);
        }
    }
    
    /**
     * Sets (as xml) the "Id" attribute
     */
    public void xsetId(org.apache.xmlbeans.XmlID id)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlID)get_store().add_attribute_user(ID$2);
            }
            target.set(id);
        }
    }
    
    /**
     * Unsets the "Id" attribute
     */
    public void unsetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(ID$2);
        }
    }
}
