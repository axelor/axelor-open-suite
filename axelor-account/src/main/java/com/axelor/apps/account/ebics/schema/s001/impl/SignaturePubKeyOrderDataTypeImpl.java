/*
 * XML Type:  SignaturePubKeyOrderDataType
 * Namespace: http://www.ebics.org/S001
 * Java type: SignaturePubKeyOrderDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.s001.impl;

import com.axelor.apps.account.ebics.schema.s001.PartnerIDType;
import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyInfoType;
import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyOrderDataType;
import com.axelor.apps.account.ebics.schema.s001.UserIDType;

/**
 * An XML SignaturePubKeyOrderDataType(@http://www.ebics.org/S001).
 *
 * This is a complex type.
 */
public class SignaturePubKeyOrderDataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignaturePubKeyOrderDataType
{
    private static final long serialVersionUID = 1L;
    
    public SignaturePubKeyOrderDataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREPUBKEYINFO$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "SignaturePubKeyInfo");
    private static final javax.xml.namespace.QName PARTNERID$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "PartnerID");
    private static final javax.xml.namespace.QName USERID$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "UserID");
    
    
    /**
     * Gets the "SignaturePubKeyInfo" element
     */
    public SignaturePubKeyInfoType getSignaturePubKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePubKeyInfoType target = null;
            target = (SignaturePubKeyInfoType)get_store().find_element_user(SIGNATUREPUBKEYINFO$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "SignaturePubKeyInfo" element
     */
    public void setSignaturePubKeyInfo(SignaturePubKeyInfoType signaturePubKeyInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePubKeyInfoType target = null;
            target = (SignaturePubKeyInfoType)get_store().find_element_user(SIGNATUREPUBKEYINFO$0, 0);
            if (target == null)
            {
                target = (SignaturePubKeyInfoType)get_store().add_element_user(SIGNATUREPUBKEYINFO$0);
            }
            target.set(signaturePubKeyInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "SignaturePubKeyInfo" element
     */
    public SignaturePubKeyInfoType addNewSignaturePubKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePubKeyInfoType target = null;
            target = (SignaturePubKeyInfoType)get_store().add_element_user(SIGNATUREPUBKEYINFO$0);
            return target;
        }
    }
    
    /**
     * Gets the "PartnerID" element
     */
    public java.lang.String getPartnerID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTNERID$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "PartnerID" element
     */
    public PartnerIDType xgetPartnerID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            PartnerIDType target = null;
            target = (PartnerIDType)get_store().find_element_user(PARTNERID$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "PartnerID" element
     */
    public void setPartnerID(java.lang.String partnerID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTNERID$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PARTNERID$2);
            }
            target.setStringValue(partnerID);
        }
    }
    
    /**
     * Sets (as xml) the "PartnerID" element
     */
    public void xsetPartnerID(PartnerIDType partnerID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            PartnerIDType target = null;
            target = (PartnerIDType)get_store().find_element_user(PARTNERID$2, 0);
            if (target == null)
            {
                target = (PartnerIDType)get_store().add_element_user(PARTNERID$2);
            }
            target.set(partnerID);
        }
    }
    
    /**
     * Gets the "UserID" element
     */
    public java.lang.String getUserID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USERID$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "UserID" element
     */
    public UserIDType xgetUserID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserIDType target = null;
            target = (UserIDType)get_store().find_element_user(USERID$4, 0);
            return target;
        }
    }
    
    /**
     * Sets the "UserID" element
     */
    public void setUserID(java.lang.String userID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USERID$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(USERID$4);
            }
            target.setStringValue(userID);
        }
    }
    
    /**
     * Sets (as xml) the "UserID" element
     */
    public void xsetUserID(UserIDType userID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserIDType target = null;
            target = (UserIDType)get_store().find_element_user(USERID$4, 0);
            if (target == null)
            {
                target = (UserIDType)get_store().add_element_user(USERID$4);
            }
            target.set(userID);
        }
    }
}
