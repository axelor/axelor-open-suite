/*
 * XML Type:  TransferReceiptResponseType
 * Namespace: http://www.ebics.org/H003
 * Java type: TransferReceiptResponseType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.ReturnCodeType;
import com.axelor.apps.account.ebics.schema.h003.TimestampType;
import com.axelor.apps.account.ebics.schema.h003.TransferReceiptResponseType;

/**
 * An XML TransferReceiptResponseType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class TransferReceiptResponseTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements TransferReceiptResponseType
{
    private static final long serialVersionUID = 1L;
    
    public TransferReceiptResponseTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RETURNCODERECEIPT$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "ReturnCodeReceipt");
    private static final javax.xml.namespace.QName TIMESTAMPBANKPARAMETER$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "TimestampBankParameter");
    
    
    /**
     * Gets the "ReturnCodeReceipt" element
     */
    public java.lang.String getReturnCodeReceipt()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RETURNCODERECEIPT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "ReturnCodeReceipt" element
     */
    public ReturnCodeType xgetReturnCodeReceipt()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReturnCodeType target = null;
            target = (ReturnCodeType)get_store().find_element_user(RETURNCODERECEIPT$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "ReturnCodeReceipt" element
     */
    public void setReturnCodeReceipt(java.lang.String returnCodeReceipt)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RETURNCODERECEIPT$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RETURNCODERECEIPT$0);
            }
            target.setStringValue(returnCodeReceipt);
        }
    }
    
    /**
     * Sets (as xml) the "ReturnCodeReceipt" element
     */
    public void xsetReturnCodeReceipt(ReturnCodeType returnCodeReceipt)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReturnCodeType target = null;
            target = (ReturnCodeType)get_store().find_element_user(RETURNCODERECEIPT$0, 0);
            if (target == null)
            {
                target = (ReturnCodeType)get_store().add_element_user(RETURNCODERECEIPT$0);
            }
            target.set(returnCodeReceipt);
        }
    }
    
    /**
     * Gets the "TimestampBankParameter" element
     */
    public java.util.Calendar getTimestampBankParameter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TIMESTAMPBANKPARAMETER$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getCalendarValue();
        }
    }
    
    /**
     * Gets (as xml) the "TimestampBankParameter" element
     */
    public TimestampType xgetTimestampBankParameter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            TimestampType target = null;
            target = (TimestampType)get_store().find_element_user(TIMESTAMPBANKPARAMETER$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "TimestampBankParameter" element
     */
    public boolean isSetTimestampBankParameter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(TIMESTAMPBANKPARAMETER$2) != 0;
        }
    }
    
    /**
     * Sets the "TimestampBankParameter" element
     */
    public void setTimestampBankParameter(java.util.Calendar timestampBankParameter)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TIMESTAMPBANKPARAMETER$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(TIMESTAMPBANKPARAMETER$2);
            }
            target.setCalendarValue(timestampBankParameter);
        }
    }
    
    /**
     * Sets (as xml) the "TimestampBankParameter" element
     */
    public void xsetTimestampBankParameter(TimestampType timestampBankParameter)
    {
        synchronized (monitor())
        {
            check_orphaned();
            TimestampType target = null;
            target = (TimestampType)get_store().find_element_user(TIMESTAMPBANKPARAMETER$2, 0);
            if (target == null)
            {
                target = (TimestampType)get_store().add_element_user(TIMESTAMPBANKPARAMETER$2);
            }
            target.set(timestampBankParameter);
        }
    }
    
    /**
     * Unsets the "TimestampBankParameter" element
     */
    public void unsetTimestampBankParameter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(TIMESTAMPBANKPARAMETER$2, 0);
        }
    }
}
