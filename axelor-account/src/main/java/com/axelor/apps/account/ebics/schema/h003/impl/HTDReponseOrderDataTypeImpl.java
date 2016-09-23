/*
 * XML Type:  HTDReponseOrderDataType
 * Namespace: http://www.ebics.org/H003
 * Java type: HTDReponseOrderDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HTDReponseOrderDataType;
import com.axelor.apps.account.ebics.schema.h003.PartnerInfoType;
import com.axelor.apps.account.ebics.schema.h003.UserInfoType;

/**
 * An XML HTDReponseOrderDataType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class HTDReponseOrderDataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements HTDReponseOrderDataType
{
    private static final long serialVersionUID = 1L;
    
    public HTDReponseOrderDataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PARTNERINFO$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "PartnerInfo");
    private static final javax.xml.namespace.QName USERINFO$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "UserInfo");
    
    
    /**
     * Gets the "PartnerInfo" element
     */
    public PartnerInfoType getPartnerInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            PartnerInfoType target = null;
            target = (PartnerInfoType)get_store().find_element_user(PARTNERINFO$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "PartnerInfo" element
     */
    public void setPartnerInfo(PartnerInfoType partnerInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            PartnerInfoType target = null;
            target = (PartnerInfoType)get_store().find_element_user(PARTNERINFO$0, 0);
            if (target == null)
            {
                target = (PartnerInfoType)get_store().add_element_user(PARTNERINFO$0);
            }
            target.set(partnerInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "PartnerInfo" element
     */
    public PartnerInfoType addNewPartnerInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            PartnerInfoType target = null;
            target = (PartnerInfoType)get_store().add_element_user(PARTNERINFO$0);
            return target;
        }
    }
    
    /**
     * Gets the "UserInfo" element
     */
    public UserInfoType getUserInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserInfoType target = null;
            target = (UserInfoType)get_store().find_element_user(USERINFO$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "UserInfo" element
     */
    public void setUserInfo(UserInfoType userInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserInfoType target = null;
            target = (UserInfoType)get_store().find_element_user(USERINFO$2, 0);
            if (target == null)
            {
                target = (UserInfoType)get_store().add_element_user(USERINFO$2);
            }
            target.set(userInfo);
        }
    }
    
    /**
     * Appends and returns a new empty "UserInfo" element
     */
    public UserInfoType addNewUserInfo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserInfoType target = null;
            target = (UserInfoType)get_store().add_element_user(USERINFO$2);
            return target;
        }
    }
}
