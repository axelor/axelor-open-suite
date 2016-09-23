/*
 * XML Type:  X509IssuerSerialType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: X509IssuerSerialType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.X509IssuerSerialType;

/**
 * An XML X509IssuerSerialType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class X509IssuerSerialTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements X509IssuerSerialType
{
    private static final long serialVersionUID = 1L;
    
    public X509IssuerSerialTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName X509ISSUERNAME$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509IssuerName");
    private static final javax.xml.namespace.QName X509SERIALNUMBER$2 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509SerialNumber");
    
    
    /**
     * Gets the "X509IssuerName" element
     */
    public java.lang.String getX509IssuerName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509ISSUERNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "X509IssuerName" element
     */
    public org.apache.xmlbeans.XmlString xgetX509IssuerName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(X509ISSUERNAME$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "X509IssuerName" element
     */
    public void setX509IssuerName(java.lang.String x509IssuerName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509ISSUERNAME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(X509ISSUERNAME$0);
            }
            target.setStringValue(x509IssuerName);
        }
    }
    
    /**
     * Sets (as xml) the "X509IssuerName" element
     */
    public void xsetX509IssuerName(org.apache.xmlbeans.XmlString x509IssuerName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(X509ISSUERNAME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(X509ISSUERNAME$0);
            }
            target.set(x509IssuerName);
        }
    }
    
    /**
     * Gets the "X509SerialNumber" element
     */
    public java.math.BigInteger getX509SerialNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509SERIALNUMBER$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getBigIntegerValue();
        }
    }
    
    /**
     * Gets (as xml) the "X509SerialNumber" element
     */
    public org.apache.xmlbeans.XmlInteger xgetX509SerialNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInteger target = null;
            target = (org.apache.xmlbeans.XmlInteger)get_store().find_element_user(X509SERIALNUMBER$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "X509SerialNumber" element
     */
    public void setX509SerialNumber(java.math.BigInteger x509SerialNumber)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509SERIALNUMBER$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(X509SERIALNUMBER$2);
            }
            target.setBigIntegerValue(x509SerialNumber);
        }
    }
    
    /**
     * Sets (as xml) the "X509SerialNumber" element
     */
    public void xsetX509SerialNumber(org.apache.xmlbeans.XmlInteger x509SerialNumber)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInteger target = null;
            target = (org.apache.xmlbeans.XmlInteger)get_store().find_element_user(X509SERIALNUMBER$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlInteger)get_store().add_element_user(X509SERIALNUMBER$2);
            }
            target.set(x509SerialNumber);
        }
    }
}
