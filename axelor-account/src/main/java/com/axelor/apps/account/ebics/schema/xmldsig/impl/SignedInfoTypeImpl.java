/*
 * XML Type:  SignedInfoType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: SignedInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.CanonicalizationMethodType;
import com.axelor.apps.account.ebics.schema.xmldsig.ReferenceType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureMethodType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignedInfoType;

/**
 * An XML SignedInfoType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class SignedInfoTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignedInfoType
{
    private static final long serialVersionUID = 1L;
    
    public SignedInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CANONICALIZATIONMETHOD$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "CanonicalizationMethod");
    private static final javax.xml.namespace.QName SIGNATUREMETHOD$2 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignatureMethod");
    private static final javax.xml.namespace.QName REFERENCE$4 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Reference");
    private static final javax.xml.namespace.QName ID$6 = 
        new javax.xml.namespace.QName("", "Id");
    
    
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
    
    /**
     * Gets the "SignatureMethod" element
     */
    public SignatureMethodType getSignatureMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureMethodType target = null;
            target = (SignatureMethodType)get_store().find_element_user(SIGNATUREMETHOD$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "SignatureMethod" element
     */
    public void setSignatureMethod(SignatureMethodType signatureMethod)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureMethodType target = null;
            target = (SignatureMethodType)get_store().find_element_user(SIGNATUREMETHOD$2, 0);
            if (target == null)
            {
                target = (SignatureMethodType)get_store().add_element_user(SIGNATUREMETHOD$2);
            }
            target.set(signatureMethod);
        }
    }
    
    /**
     * Appends and returns a new empty "SignatureMethod" element
     */
    public SignatureMethodType addNewSignatureMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureMethodType target = null;
            target = (SignatureMethodType)get_store().add_element_user(SIGNATUREMETHOD$2);
            return target;
        }
    }
    
    /**
     * Gets array of all "Reference" elements
     */
    public ReferenceType[] getReferenceArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(REFERENCE$4, targetList);
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
            target = (ReferenceType)get_store().find_element_user(REFERENCE$4, i);
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
            return get_store().count_elements(REFERENCE$4);
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
            arraySetterHelper(referenceArray, REFERENCE$4);
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
            target = (ReferenceType)get_store().find_element_user(REFERENCE$4, i);
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
            target = (ReferenceType)get_store().insert_element_user(REFERENCE$4, i);
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
            target = (ReferenceType)get_store().add_element_user(REFERENCE$4);
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
            get_store().remove_element(REFERENCE$4, i);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$6);
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
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$6);
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
            return get_store().find_attribute_user(ID$6) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ID$6);
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
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlID)get_store().add_attribute_user(ID$6);
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
            get_store().remove_attribute(ID$6);
        }
    }
}
