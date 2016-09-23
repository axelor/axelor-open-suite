/*
 * XML Type:  ReferenceType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: ReferenceType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.DigestMethodType;
import com.axelor.apps.account.ebics.schema.xmldsig.DigestValueType;
import com.axelor.apps.account.ebics.schema.xmldsig.ReferenceType;
import com.axelor.apps.account.ebics.schema.xmldsig.TransformsType;

/**
 * An XML ReferenceType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class ReferenceTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements ReferenceType
{
    private static final long serialVersionUID = 1L;
    
    public ReferenceTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName TRANSFORMS$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Transforms");
    private static final javax.xml.namespace.QName DIGESTMETHOD$2 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "DigestMethod");
    private static final javax.xml.namespace.QName DIGESTVALUE$4 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "DigestValue");
    private static final javax.xml.namespace.QName ID$6 = 
        new javax.xml.namespace.QName("", "Id");
    private static final javax.xml.namespace.QName URI$8 = 
        new javax.xml.namespace.QName("", "URI");
    private static final javax.xml.namespace.QName TYPE$10 = 
        new javax.xml.namespace.QName("", "Type");
    
    
    /**
     * Gets the "Transforms" element
     */
    public TransformsType getTransforms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            TransformsType target = null;
            target = (TransformsType)get_store().find_element_user(TRANSFORMS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "Transforms" element
     */
    public boolean isSetTransforms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(TRANSFORMS$0) != 0;
        }
    }
    
    /**
     * Sets the "Transforms" element
     */
    public void setTransforms(TransformsType transforms)
    {
        synchronized (monitor())
        {
            check_orphaned();
            TransformsType target = null;
            target = (TransformsType)get_store().find_element_user(TRANSFORMS$0, 0);
            if (target == null)
            {
                target = (TransformsType)get_store().add_element_user(TRANSFORMS$0);
            }
            target.set(transforms);
        }
    }
    
    /**
     * Appends and returns a new empty "Transforms" element
     */
    public TransformsType addNewTransforms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            TransformsType target = null;
            target = (TransformsType)get_store().add_element_user(TRANSFORMS$0);
            return target;
        }
    }
    
    /**
     * Unsets the "Transforms" element
     */
    public void unsetTransforms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(TRANSFORMS$0, 0);
        }
    }
    
    /**
     * Gets the "DigestMethod" element
     */
    public DigestMethodType getDigestMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DigestMethodType target = null;
            target = (DigestMethodType)get_store().find_element_user(DIGESTMETHOD$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "DigestMethod" element
     */
    public void setDigestMethod(DigestMethodType digestMethod)
    {
        synchronized (monitor())
        {
            check_orphaned();
            DigestMethodType target = null;
            target = (DigestMethodType)get_store().find_element_user(DIGESTMETHOD$2, 0);
            if (target == null)
            {
                target = (DigestMethodType)get_store().add_element_user(DIGESTMETHOD$2);
            }
            target.set(digestMethod);
        }
    }
    
    /**
     * Appends and returns a new empty "DigestMethod" element
     */
    public DigestMethodType addNewDigestMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DigestMethodType target = null;
            target = (DigestMethodType)get_store().add_element_user(DIGESTMETHOD$2);
            return target;
        }
    }
    
    /**
     * Gets the "DigestValue" element
     */
    public byte[] getDigestValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(DIGESTVALUE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "DigestValue" element
     */
    public DigestValueType xgetDigestValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DigestValueType target = null;
            target = (DigestValueType)get_store().find_element_user(DIGESTVALUE$4, 0);
            return target;
        }
    }
    
    /**
     * Sets the "DigestValue" element
     */
    public void setDigestValue(byte[] digestValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(DIGESTVALUE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(DIGESTVALUE$4);
            }
            target.setByteArrayValue(digestValue);
        }
    }
    
    /**
     * Sets (as xml) the "DigestValue" element
     */
    public void xsetDigestValue(DigestValueType digestValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            DigestValueType target = null;
            target = (DigestValueType)get_store().find_element_user(DIGESTVALUE$4, 0);
            if (target == null)
            {
                target = (DigestValueType)get_store().add_element_user(DIGESTVALUE$4);
            }
            target.set(digestValue);
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
    
    /**
     * Gets the "URI" attribute
     */
    public java.lang.String getURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(URI$8);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "URI" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(URI$8);
            return target;
        }
    }
    
    /**
     * True if has "URI" attribute
     */
    public boolean isSetURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(URI$8) != null;
        }
    }
    
    /**
     * Sets the "URI" attribute
     */
    public void setURI(java.lang.String uri)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(URI$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(URI$8);
            }
            target.setStringValue(uri);
        }
    }
    
    /**
     * Sets (as xml) the "URI" attribute
     */
    public void xsetURI(org.apache.xmlbeans.XmlAnyURI uri)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(URI$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(URI$8);
            }
            target.set(uri);
        }
    }
    
    /**
     * Unsets the "URI" attribute
     */
    public void unsetURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(URI$8);
        }
    }
    
    /**
     * Gets the "Type" attribute
     */
    public java.lang.String getType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$10);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Type" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(TYPE$10);
            return target;
        }
    }
    
    /**
     * True if has "Type" attribute
     */
    public boolean isSetType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(TYPE$10) != null;
        }
    }
    
    /**
     * Sets the "Type" attribute
     */
    public void setType(java.lang.String type)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$10);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TYPE$10);
            }
            target.setStringValue(type);
        }
    }
    
    /**
     * Sets (as xml) the "Type" attribute
     */
    public void xsetType(org.apache.xmlbeans.XmlAnyURI type)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(TYPE$10);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(TYPE$10);
            }
            target.set(type);
        }
    }
    
    /**
     * Unsets the "Type" attribute
     */
    public void unsetType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(TYPE$10);
        }
    }
}
