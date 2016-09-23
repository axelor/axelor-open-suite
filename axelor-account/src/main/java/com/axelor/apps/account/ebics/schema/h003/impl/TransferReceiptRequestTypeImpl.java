/*
 * XML Type:  TransferReceiptRequestType
 * Namespace: http://www.ebics.org/H003
 * Java type: TransferReceiptRequestType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.ReceiptCodeType;
import com.axelor.apps.account.ebics.schema.h003.TransferReceiptRequestType;

/**
 * An XML TransferReceiptRequestType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class TransferReceiptRequestTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements TransferReceiptRequestType
{
    private static final long serialVersionUID = 1L;
    
    public TransferReceiptRequestTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RECEIPTCODE$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "ReceiptCode");
    
    
    /**
     * Gets the "ReceiptCode" element
     */
    public int getReceiptCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RECEIPTCODE$0, 0);
            if (target == null)
            {
                return 0;
            }
            return target.getIntValue();
        }
    }
    
    /**
     * Gets (as xml) the "ReceiptCode" element
     */
    public ReceiptCodeType xgetReceiptCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReceiptCodeType target = null;
            target = (ReceiptCodeType)get_store().find_element_user(RECEIPTCODE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "ReceiptCode" element
     */
    public void setReceiptCode(int receiptCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RECEIPTCODE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RECEIPTCODE$0);
            }
            target.setIntValue(receiptCode);
        }
    }
    
    /**
     * Sets (as xml) the "ReceiptCode" element
     */
    public void xsetReceiptCode(ReceiptCodeType receiptCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReceiptCodeType target = null;
            target = (ReceiptCodeType)get_store().find_element_user(RECEIPTCODE$0, 0);
            if (target == null)
            {
                target = (ReceiptCodeType)get_store().add_element_user(RECEIPTCODE$0);
            }
            target.set(receiptCode);
        }
    }
}
