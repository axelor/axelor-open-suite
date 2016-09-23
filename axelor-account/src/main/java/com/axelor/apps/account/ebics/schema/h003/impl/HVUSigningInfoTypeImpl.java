/*
 * XML Type:  HVUSigningInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: HVUSigningInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HVUSigningInfoType;

/**
 * An XML HVUSigningInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class HVUSigningInfoTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements HVUSigningInfoType
{
    private static final long serialVersionUID = 1L;
    
    public HVUSigningInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName READYTOBESIGNED$0 = 
        new javax.xml.namespace.QName("", "readyToBeSigned");
    private static final javax.xml.namespace.QName NUMSIGREQUIRED$2 = 
        new javax.xml.namespace.QName("", "NumSigRequired");
    private static final javax.xml.namespace.QName NUMSIGDONE$4 = 
        new javax.xml.namespace.QName("", "NumSigDone");
    
    
    /**
     * Gets the "readyToBeSigned" attribute
     */
    public boolean getReadyToBeSigned()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(READYTOBESIGNED$0);
            if (target == null)
            {
                return false;
            }
            return target.getBooleanValue();
        }
    }
    
    /**
     * Gets (as xml) the "readyToBeSigned" attribute
     */
    public org.apache.xmlbeans.XmlBoolean xgetReadyToBeSigned()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBoolean target = null;
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(READYTOBESIGNED$0);
            return target;
        }
    }
    
    /**
     * Sets the "readyToBeSigned" attribute
     */
    public void setReadyToBeSigned(boolean readyToBeSigned)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(READYTOBESIGNED$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(READYTOBESIGNED$0);
            }
            target.setBooleanValue(readyToBeSigned);
        }
    }
    
    /**
     * Sets (as xml) the "readyToBeSigned" attribute
     */
    public void xsetReadyToBeSigned(org.apache.xmlbeans.XmlBoolean readyToBeSigned)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBoolean target = null;
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(READYTOBESIGNED$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlBoolean)get_store().add_attribute_user(READYTOBESIGNED$0);
            }
            target.set(readyToBeSigned);
        }
    }
    
    /**
     * Gets the "NumSigRequired" attribute
     */
    public java.math.BigInteger getNumSigRequired()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NUMSIGREQUIRED$2);
            if (target == null)
            {
                return null;
            }
            return target.getBigIntegerValue();
        }
    }
    
    /**
     * Gets (as xml) the "NumSigRequired" attribute
     */
    public org.apache.xmlbeans.XmlPositiveInteger xgetNumSigRequired()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlPositiveInteger target = null;
            target = (org.apache.xmlbeans.XmlPositiveInteger)get_store().find_attribute_user(NUMSIGREQUIRED$2);
            return target;
        }
    }
    
    /**
     * Sets the "NumSigRequired" attribute
     */
    public void setNumSigRequired(java.math.BigInteger numSigRequired)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NUMSIGREQUIRED$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(NUMSIGREQUIRED$2);
            }
            target.setBigIntegerValue(numSigRequired);
        }
    }
    
    /**
     * Sets (as xml) the "NumSigRequired" attribute
     */
    public void xsetNumSigRequired(org.apache.xmlbeans.XmlPositiveInteger numSigRequired)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlPositiveInteger target = null;
            target = (org.apache.xmlbeans.XmlPositiveInteger)get_store().find_attribute_user(NUMSIGREQUIRED$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlPositiveInteger)get_store().add_attribute_user(NUMSIGREQUIRED$2);
            }
            target.set(numSigRequired);
        }
    }
    
    /**
     * Gets the "NumSigDone" attribute
     */
    public java.math.BigInteger getNumSigDone()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NUMSIGDONE$4);
            if (target == null)
            {
                return null;
            }
            return target.getBigIntegerValue();
        }
    }
    
    /**
     * Gets (as xml) the "NumSigDone" attribute
     */
    public org.apache.xmlbeans.XmlNonNegativeInteger xgetNumSigDone()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNonNegativeInteger target = null;
            target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().find_attribute_user(NUMSIGDONE$4);
            return target;
        }
    }
    
    /**
     * Sets the "NumSigDone" attribute
     */
    public void setNumSigDone(java.math.BigInteger numSigDone)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NUMSIGDONE$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(NUMSIGDONE$4);
            }
            target.setBigIntegerValue(numSigDone);
        }
    }
    
    /**
     * Sets (as xml) the "NumSigDone" attribute
     */
    public void xsetNumSigDone(org.apache.xmlbeans.XmlNonNegativeInteger numSigDone)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNonNegativeInteger target = null;
            target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().find_attribute_user(NUMSIGDONE$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().add_attribute_user(NUMSIGDONE$4);
            }
            target.set(numSigDone);
        }
    }
}
