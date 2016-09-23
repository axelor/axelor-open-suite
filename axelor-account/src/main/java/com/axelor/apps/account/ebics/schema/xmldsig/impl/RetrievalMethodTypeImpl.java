/*
 * XML Type:  RetrievalMethodType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: RetrievalMethodType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.RetrievalMethodType;
import com.axelor.apps.account.ebics.schema.xmldsig.TransformsType;

/**
 * An XML RetrievalMethodType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class RetrievalMethodTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements RetrievalMethodType
{
    private static final long serialVersionUID = 1L;
    
    public RetrievalMethodTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName TRANSFORMS$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Transforms");
    private static final javax.xml.namespace.QName URI$2 = 
        new javax.xml.namespace.QName("", "URI");
    private static final javax.xml.namespace.QName TYPE$4 = 
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
     * Gets the "URI" attribute
     */
    public java.lang.String getURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(URI$2);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(URI$2);
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
            return get_store().find_attribute_user(URI$2) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(URI$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(URI$2);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(URI$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(URI$2);
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
            get_store().remove_attribute(URI$2);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$4);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(TYPE$4);
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
            return get_store().find_attribute_user(TYPE$4) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TYPE$4);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(TYPE$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(TYPE$4);
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
            get_store().remove_attribute(TYPE$4);
        }
    }
}
