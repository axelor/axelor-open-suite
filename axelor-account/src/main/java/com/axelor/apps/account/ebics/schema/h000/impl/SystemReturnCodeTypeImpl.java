/*
 * XML Type:  SystemReturnCodeType
 * Namespace: http://www.ebics.org/H000
 * Java type: SystemReturnCodeType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h000.impl;

import com.axelor.apps.account.ebics.schema.h000.ReportTextType;
import com.axelor.apps.account.ebics.schema.h000.ReturnCodeType;
import com.axelor.apps.account.ebics.schema.h000.SystemReturnCodeType;

/**
 * An XML SystemReturnCodeType(@http://www.ebics.org/H000).
 *
 * This is a complex type.
 */
public class SystemReturnCodeTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SystemReturnCodeType
{
    private static final long serialVersionUID = 1L;
    
    public SystemReturnCodeTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RETURNCODE$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H000", "ReturnCode");
    private static final javax.xml.namespace.QName REPORTTEXT$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H000", "ReportText");
    
    
    /**
     * Gets the "ReturnCode" element
     */
    public java.lang.String getReturnCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RETURNCODE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "ReturnCode" element
     */
    public ReturnCodeType xgetReturnCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReturnCodeType target = null;
            target = (ReturnCodeType)get_store().find_element_user(RETURNCODE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "ReturnCode" element
     */
    public void setReturnCode(java.lang.String returnCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RETURNCODE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RETURNCODE$0);
            }
            target.setStringValue(returnCode);
        }
    }
    
    /**
     * Sets (as xml) the "ReturnCode" element
     */
    public void xsetReturnCode(ReturnCodeType returnCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReturnCodeType target = null;
            target = (ReturnCodeType)get_store().find_element_user(RETURNCODE$0, 0);
            if (target == null)
            {
                target = (ReturnCodeType)get_store().add_element_user(RETURNCODE$0);
            }
            target.set(returnCode);
        }
    }
    
    /**
     * Gets the "ReportText" element
     */
    public java.lang.String getReportText()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(REPORTTEXT$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "ReportText" element
     */
    public ReportTextType xgetReportText()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReportTextType target = null;
            target = (ReportTextType)get_store().find_element_user(REPORTTEXT$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "ReportText" element
     */
    public void setReportText(java.lang.String reportText)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(REPORTTEXT$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(REPORTTEXT$2);
            }
            target.setStringValue(reportText);
        }
    }
    
    /**
     * Sets (as xml) the "ReportText" element
     */
    public void xsetReportText(ReportTextType reportText)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReportTextType target = null;
            target = (ReportTextType)get_store().find_element_user(REPORTTEXT$2, 0);
            if (target == null)
            {
                target = (ReportTextType)get_store().add_element_user(REPORTTEXT$2);
            }
            target.set(reportText);
        }
    }
}
