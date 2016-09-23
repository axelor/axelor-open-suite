/*
 * XML Type:  ObjectType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: ObjectType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.ObjectType;

/**
 * An XML ObjectType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class ObjectTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements ObjectType
{
    private static final long serialVersionUID = 1L;
    
    public ObjectTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ID$0 = 
        new javax.xml.namespace.QName("", "Id");
    private static final javax.xml.namespace.QName MIMETYPE$2 = 
        new javax.xml.namespace.QName("", "MimeType");
    private static final javax.xml.namespace.QName ENCODING$4 = 
        new javax.xml.namespace.QName("", "Encoding");
    
    
    /**
     * Gets the "Id" attribute
     */
    public java.lang.String getId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$0);
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
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$0);
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
            return get_store().find_attribute_user(ID$0) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ID$0);
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
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlID)get_store().add_attribute_user(ID$0);
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
            get_store().remove_attribute(ID$0);
        }
    }
    
    /**
     * Gets the "MimeType" attribute
     */
    public java.lang.String getMimeType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MIMETYPE$2);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "MimeType" attribute
     */
    public org.apache.xmlbeans.XmlString xgetMimeType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(MIMETYPE$2);
            return target;
        }
    }
    
    /**
     * True if has "MimeType" attribute
     */
    public boolean isSetMimeType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(MIMETYPE$2) != null;
        }
    }
    
    /**
     * Sets the "MimeType" attribute
     */
    public void setMimeType(java.lang.String mimeType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MIMETYPE$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(MIMETYPE$2);
            }
            target.setStringValue(mimeType);
        }
    }
    
    /**
     * Sets (as xml) the "MimeType" attribute
     */
    public void xsetMimeType(org.apache.xmlbeans.XmlString mimeType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(MIMETYPE$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(MIMETYPE$2);
            }
            target.set(mimeType);
        }
    }
    
    /**
     * Unsets the "MimeType" attribute
     */
    public void unsetMimeType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(MIMETYPE$2);
        }
    }
    
    /**
     * Gets the "Encoding" attribute
     */
    public java.lang.String getEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ENCODING$4);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Encoding" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ENCODING$4);
            return target;
        }
    }
    
    /**
     * True if has "Encoding" attribute
     */
    public boolean isSetEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(ENCODING$4) != null;
        }
    }
    
    /**
     * Sets the "Encoding" attribute
     */
    public void setEncoding(java.lang.String encoding)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ENCODING$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ENCODING$4);
            }
            target.setStringValue(encoding);
        }
    }
    
    /**
     * Sets (as xml) the "Encoding" attribute
     */
    public void xsetEncoding(org.apache.xmlbeans.XmlAnyURI encoding)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ENCODING$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(ENCODING$4);
            }
            target.set(encoding);
        }
    }
    
    /**
     * Unsets the "Encoding" attribute
     */
    public void unsetEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(ENCODING$4);
        }
    }
}
