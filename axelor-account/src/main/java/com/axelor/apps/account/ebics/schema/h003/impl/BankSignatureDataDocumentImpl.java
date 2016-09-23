/*
 * An XML document type.
 * Localname: BankSignatureData
 * Namespace: http://www.ebics.org/H003
 * Java type: BankSignatureDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.BankSignatureDataDocument;
import com.axelor.apps.account.ebics.schema.h003.BankSignatureDataSigBookType;

/**
 * A document containing one BankSignatureData(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class BankSignatureDataDocumentImpl extends EBICSSignatureDataDocumentImpl implements BankSignatureDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public BankSignatureDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName BANKSIGNATUREDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "BankSignatureData");
    
    
    /**
     * Gets the "BankSignatureData" element
     */
    public BankSignatureDataSigBookType getBankSignatureData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            BankSignatureDataSigBookType target = null;
            target = (BankSignatureDataSigBookType)get_store().find_element_user(BANKSIGNATUREDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "BankSignatureData" element
     */
    public void setBankSignatureData(BankSignatureDataSigBookType bankSignatureData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            BankSignatureDataSigBookType target = null;
            target = (BankSignatureDataSigBookType)get_store().find_element_user(BANKSIGNATUREDATA$0, 0);
            if (target == null)
            {
                target = (BankSignatureDataSigBookType)get_store().add_element_user(BANKSIGNATUREDATA$0);
            }
            target.set(bankSignatureData);
        }
    }
    
    /**
     * Appends and returns a new empty "BankSignatureData" element
     */
    public BankSignatureDataSigBookType addNewBankSignatureData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            BankSignatureDataSigBookType target = null;
            target = (BankSignatureDataSigBookType)get_store().add_element_user(BANKSIGNATUREDATA$0);
            return target;
        }
    }
}
