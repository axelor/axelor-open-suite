/*
 * XML Type:  HCSRequestOrderDataType
 * Namespace: http://www.ebics.org/H003
 * Java type: HCSRequestOrderDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.values.XmlComplexContentImpl;

import com.axelor.apps.account.ebics.schema.h003.AuthenticationPubKeyInfoType;
import com.axelor.apps.account.ebics.schema.h003.EncryptionPubKeyInfoType;
import com.axelor.apps.account.ebics.schema.h003.HCSRequestOrderDataType;
import com.axelor.apps.account.ebics.schema.h003.PartnerIDType;
import com.axelor.apps.account.ebics.schema.h003.UserIDType;
import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyInfoType;

/**
 * An XML HCSRequestOrderDataType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class HCSRequestOrderDataTypeImpl extends XmlComplexContentImpl implements HCSRequestOrderDataType
{
    private static final long serialVersionUID = 1L;
    
    public HCSRequestOrderDataTypeImpl(SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AUTHENTICATIONPUBKEYINFO$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "AuthenticationPubKeyInfo");
    private static final javax.xml.namespace.QName ENCRYPTIONPUBKEYINFO$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "EncryptionPubKeyInfo");
    private static final javax.xml.namespace.QName SIGNATUREPUBKEYINFO$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "SignaturePubKeyInfo");
    private static final javax.xml.namespace.QName PARTNERID$6 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "PartnerID");
    private static final javax.xml.namespace.QName USERID$8 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "UserID");
    
    
    /**
     * Gets the "AuthenticationPubKeyInfo" element
     */
    public AuthenticationPubKeyInfoType getAuthenticationPubKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AuthenticationPubKeyInfoType target = null;
            target = (AuthenticationPubKeyInfoType)get_store().find_element_user(AUTHENTICATIONPUBKEYINFO$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "AuthenticationPubKeyInfo" element
     */
    public void setAuthenticationPubKeyInfo(AuthenticationPubKeyInfoType authenticationPubKeyInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            AuthenticationPubKeyInfoType target = null;
            target = (AuthenticationPubKeyInfoType)get_store().find_element_user(AUTHENTICATIONPUBKEYINFO$0, 0);
            if (target == null)
            {
                target = (AuthenticationPubKeyInfoType)get_store().add_element_user(AUTHENTICATIONPUBKEYINFO$0);
            }
            target.set(authenticationPubKeyInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "AuthenticationPubKeyInfo" element
     */
    public AuthenticationPubKeyInfoType addNewAuthenticationPubKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            AuthenticationPubKeyInfoType target = null;
            target = (AuthenticationPubKeyInfoType)get_store().add_element_user(AUTHENTICATIONPUBKEYINFO$0);
            return target;
        }
    }
    
    /**
     * Gets the "EncryptionPubKeyInfo" element
     */
    public EncryptionPubKeyInfoType getEncryptionPubKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            EncryptionPubKeyInfoType target = null;
            target = (EncryptionPubKeyInfoType)get_store().find_element_user(ENCRYPTIONPUBKEYINFO$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "EncryptionPubKeyInfo" element
     */
    public void setEncryptionPubKeyInfo(EncryptionPubKeyInfoType encryptionPubKeyInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            EncryptionPubKeyInfoType target = null;
            target = (EncryptionPubKeyInfoType)get_store().find_element_user(ENCRYPTIONPUBKEYINFO$2, 0);
            if (target == null)
            {
                target = (EncryptionPubKeyInfoType)get_store().add_element_user(ENCRYPTIONPUBKEYINFO$2);
            }
            target.set(encryptionPubKeyInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "EncryptionPubKeyInfo" element
     */
    public EncryptionPubKeyInfoType addNewEncryptionPubKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            EncryptionPubKeyInfoType target = null;
            target = (EncryptionPubKeyInfoType)get_store().add_element_user(ENCRYPTIONPUBKEYINFO$2);
            return target;
        }
    }
    
    /**
     * Gets the "SignaturePubKeyInfo" element
     */
    public SignaturePubKeyInfoType getSignaturePubKeyInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignaturePubKeyInfoType target = null;
            target = (SignaturePubKeyInfoType)get_store().find_element_user(SIGNATUREPUBKEYINFO$4, 0);
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
            target = (SignaturePubKeyInfoType)get_store().find_element_user(SIGNATUREPUBKEYINFO$4, 0);
            if (target == null)
            {
                target = (SignaturePubKeyInfoType)get_store().add_element_user(SIGNATUREPUBKEYINFO$4);
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
            target = (SignaturePubKeyInfoType)get_store().add_element_user(SIGNATUREPUBKEYINFO$4);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTNERID$6, 0);
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
            target = (PartnerIDType)get_store().find_element_user(PARTNERID$6, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTNERID$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PARTNERID$6);
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
            target = (PartnerIDType)get_store().find_element_user(PARTNERID$6, 0);
            if (target == null)
            {
                target = (PartnerIDType)get_store().add_element_user(PARTNERID$6);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USERID$8, 0);
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
            target = (UserIDType)get_store().find_element_user(USERID$8, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USERID$8, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(USERID$8);
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
            target = (UserIDType)get_store().find_element_user(USERID$8, 0);
            if (target == null)
            {
                target = (UserIDType)get_store().add_element_user(USERID$8);
            }
            target.set(userID);
        }
    }
}
