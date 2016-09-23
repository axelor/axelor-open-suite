/*
 * An XML document type.
 * Localname: UserSignatureData
 * Namespace: http://www.ebics.org/S001
 * Java type: UserSignatureDataDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.s001.impl;

import com.axelor.apps.account.ebics.schema.s001.UserSignatureDataDocument;
import com.axelor.apps.account.ebics.schema.s001.UserSignatureDataSigBookType;

/**
 * A document containing one UserSignatureData(@http://www.ebics.org/S001) element.
 *
 * This is a complex type.
 */
public class UserSignatureDataDocumentImpl extends EBICSSignatureDataDocumentImpl implements UserSignatureDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public UserSignatureDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName USERSIGNATUREDATA$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "UserSignatureData");
    
    
    /**
     * Gets the "UserSignatureData" element
     */
    public UserSignatureDataSigBookType getUserSignatureData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserSignatureDataSigBookType target = null;
            target = (UserSignatureDataSigBookType)get_store().find_element_user(USERSIGNATUREDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "UserSignatureData" element
     */
    public void setUserSignatureData(UserSignatureDataSigBookType userSignatureData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserSignatureDataSigBookType target = null;
            target = (UserSignatureDataSigBookType)get_store().find_element_user(USERSIGNATUREDATA$0, 0);
            if (target == null)
            {
                target = (UserSignatureDataSigBookType)get_store().add_element_user(USERSIGNATUREDATA$0);
            }
            target.set(userSignatureData);
        }
    }
    
    /**
     * Appends and returns a new empty "UserSignatureData" element
     */
    public UserSignatureDataSigBookType addNewUserSignatureData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserSignatureDataSigBookType target = null;
            target = (UserSignatureDataSigBookType)get_store().add_element_user(USERSIGNATUREDATA$0);
            return target;
        }
    }
}
