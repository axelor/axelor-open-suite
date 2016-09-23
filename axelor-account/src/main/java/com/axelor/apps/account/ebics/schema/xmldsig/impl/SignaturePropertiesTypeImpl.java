/*
 * XML Type:  SignaturePropertiesType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: SignaturePropertiesType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.SignaturePropertiesType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignaturePropertyType;

/**
 * An XML SignaturePropertiesType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class SignaturePropertiesTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignaturePropertiesType
{
    private static final long serialVersionUID = 1L;
    
    public SignaturePropertiesTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREPROPERTY$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignatureProperty");
    private static final javax.xml.namespace.QName ID$2 = 
        new javax.xml.namespace.QName("", "Id");
    
    
    /**
     * Gets array of all "SignatureProperty" elements
     */
    public SignaturePropertyType[] getSignaturePropertyArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SIGNATUREPROPERTY$0, targetList);
            SignaturePropertyType[] result = new SignaturePropertyType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "SignatureProperty" element
     */
    public SignaturePropertyType getSignaturePropertyArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePropertyType target = null;
            target = (SignaturePropertyType)get_store().find_element_user(SIGNATUREPROPERTY$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "SignatureProperty" element
     */
    public int sizeOfSignaturePropertyArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNATUREPROPERTY$0);
        }
    }
    
    /**
     * Sets array of all "SignatureProperty" element
     */
    public void setSignaturePropertyArray(SignaturePropertyType[] signaturePropertyArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(signaturePropertyArray, SIGNATUREPROPERTY$0);
        }
    }
    
    /**
     * Sets ith "SignatureProperty" element
     */
    public void setSignaturePropertyArray(int i, SignaturePropertyType signatureProperty)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePropertyType target = null;
            target = (SignaturePropertyType)get_store().find_element_user(SIGNATUREPROPERTY$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(signatureProperty);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "SignatureProperty" element
     */
    public SignaturePropertyType insertNewSignatureProperty(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePropertyType target = null;
            target = (SignaturePropertyType)get_store().insert_element_user(SIGNATUREPROPERTY$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "SignatureProperty" element
     */
    public SignaturePropertyType addNewSignatureProperty()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePropertyType target = null;
            target = (SignaturePropertyType)get_store().add_element_user(SIGNATUREPROPERTY$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "SignatureProperty" element
     */
    public void removeSignatureProperty(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNATUREPROPERTY$0, i);
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
