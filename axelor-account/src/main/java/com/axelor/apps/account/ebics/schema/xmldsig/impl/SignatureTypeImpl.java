/*
 * XML Type:  SignatureType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: SignatureType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.KeyInfoType;
import com.axelor.apps.account.ebics.schema.xmldsig.ObjectType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureValueType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignedInfoType;

/**
 * An XML SignatureType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class SignatureTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignatureType
{
    private static final long serialVersionUID = 1L;
    
    public SignatureTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNEDINFO$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignedInfo");
    private static final javax.xml.namespace.QName SIGNATUREVALUE$2 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignatureValue");
    private static final javax.xml.namespace.QName KEYINFO$4 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "KeyInfo");
    private static final javax.xml.namespace.QName OBJECT$6 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Object");
    private static final javax.xml.namespace.QName ID$8 = 
        new javax.xml.namespace.QName("", "Id");
    
    
    /**
     * Gets the "SignedInfo" element
     */
    public SignedInfoType getSignedInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignedInfoType target = null;
            target = (SignedInfoType)get_store().find_element_user(SIGNEDINFO$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "SignedInfo" element
     */
    public void setSignedInfo(SignedInfoType signedInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignedInfoType target = null;
            target = (SignedInfoType)get_store().find_element_user(SIGNEDINFO$0, 0);
            if (target == null)
            {
                target = (SignedInfoType)get_store().add_element_user(SIGNEDINFO$0);
            }
            target.set(signedInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "SignedInfo" element
     */
    public SignedInfoType addNewSignedInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignedInfoType target = null;
            target = (SignedInfoType)get_store().add_element_user(SIGNEDINFO$0);
            return target;
        }
    }
    
    /**
     * Gets the "SignatureValue" element
     */
    public SignatureValueType getSignatureValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureValueType target = null;
            target = (SignatureValueType)get_store().find_element_user(SIGNATUREVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "SignatureValue" element
     */
    public void setSignatureValue(SignatureValueType signatureValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureValueType target = null;
            target = (SignatureValueType)get_store().find_element_user(SIGNATUREVALUE$2, 0);
            if (target == null)
            {
                target = (SignatureValueType)get_store().add_element_user(SIGNATUREVALUE$2);
            }
            target.set(signatureValue);
        }
    }
    
    /**
     * Appends and returns a new empty "SignatureValue" element
     */
    public SignatureValueType addNewSignatureValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureValueType target = null;
            target = (SignatureValueType)get_store().add_element_user(SIGNATUREVALUE$2);
            return target;
        }
    }
    
    /**
     * Gets the "KeyInfo" element
     */
    public KeyInfoType getKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            KeyInfoType target = null;
            target = (KeyInfoType)get_store().find_element_user(KEYINFO$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "KeyInfo" element
     */
    public boolean isSetKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(KEYINFO$4) != 0;
        }
    }
    
    /**
     * Sets the "KeyInfo" element
     */
    public void setKeyInfo(KeyInfoType keyInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            KeyInfoType target = null;
            target = (KeyInfoType)get_store().find_element_user(KEYINFO$4, 0);
            if (target == null)
            {
                target = (KeyInfoType)get_store().add_element_user(KEYINFO$4);
            }
            target.set(keyInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "KeyInfo" element
     */
    public KeyInfoType addNewKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            KeyInfoType target = null;
            target = (KeyInfoType)get_store().add_element_user(KEYINFO$4);
            return target;
        }
    }
    
    /**
     * Unsets the "KeyInfo" element
     */
    public void unsetKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(KEYINFO$4, 0);
        }
    }
    
    /**
     * Gets array of all "Object" elements
     */
    public ObjectType[] getObjectArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(OBJECT$6, targetList);
            ObjectType[] result = new ObjectType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "Object" element
     */
    public ObjectType getObjectArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ObjectType target = null;
            target = (ObjectType)get_store().find_element_user(OBJECT$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "Object" element
     */
    public int sizeOfObjectArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OBJECT$6);
        }
    }
    
    /**
     * Sets array of all "Object" element
     */
    public void setObjectArray(ObjectType[] objectArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(objectArray, OBJECT$6);
        }
    }
    
    /**
     * Sets ith "Object" element
     */
    public void setObjectArray(int i, ObjectType object)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ObjectType target = null;
            target = (ObjectType)get_store().find_element_user(OBJECT$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(object);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "Object" element
     */
    public ObjectType insertNewObject(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ObjectType target = null;
            target = (ObjectType)get_store().insert_element_user(OBJECT$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "Object" element
     */
    public ObjectType addNewObject()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ObjectType target = null;
            target = (ObjectType)get_store().add_element_user(OBJECT$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "Object" element
     */
    public void removeObject(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OBJECT$6, i);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$8);
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
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$8);
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
            return get_store().find_attribute_user(ID$8) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ID$8);
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
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlID)get_store().add_attribute_user(ID$8);
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
            get_store().remove_attribute(ID$8);
        }
    }
}
