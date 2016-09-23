/*
 * XML Type:  HEVRequestDataType
 * Namespace: http://www.ebics.org/H000
 * Java type: HEVRequestDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h000.impl;

import com.axelor.apps.account.ebics.schema.h000.HEVRequestDataType;
import com.axelor.apps.account.ebics.schema.h000.HostIDType;

/**
 * An XML HEVRequestDataType(@http://www.ebics.org/H000).
 *
 * This is a complex type.
 */
public class HEVRequestDataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements HEVRequestDataType
{
    private static final long serialVersionUID = 1L;
    
    public HEVRequestDataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HOSTID$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H000", "HostID");
    
    
    /**
     * Gets the "HostID" element
     */
    public java.lang.String getHostID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(HOSTID$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "HostID" element
     */
    public HostIDType xgetHostID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HostIDType target = null;
            target = (HostIDType)get_store().find_element_user(HOSTID$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "HostID" element
     */
    public void setHostID(java.lang.String hostID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(HOSTID$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(HOSTID$0);
            }
            target.setStringValue(hostID);
        }
    }
    
    /**
     * Sets (as xml) the "HostID" element
     */
    public void xsetHostID(HostIDType hostID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HostIDType target = null;
            target = (HostIDType)get_store().find_element_user(HOSTID$0, 0);
            if (target == null)
            {
                target = (HostIDType)get_store().add_element_user(HOSTID$0);
            }
            target.set(hostID);
        }
    }
}
