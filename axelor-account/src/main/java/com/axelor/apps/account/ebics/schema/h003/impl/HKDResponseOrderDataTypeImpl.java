/*
 * XML Type:  HKDResponseOrderDataType
 * Namespace: http://www.ebics.org/H003
 * Java type: HKDResponseOrderDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HKDResponseOrderDataType;
import com.axelor.apps.account.ebics.schema.h003.PartnerInfoType;
import com.axelor.apps.account.ebics.schema.h003.UserInfoType;

/**
 * An XML HKDResponseOrderDataType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class HKDResponseOrderDataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements HKDResponseOrderDataType
{
    private static final long serialVersionUID = 1L;
    
    public HKDResponseOrderDataTypeImpl(org.apache.xmlbeans.SchemaType sType)
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
     * Gets array of all "UserInfo" elements
     */
    public UserInfoType[] getUserInfoArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(USERINFO$2, targetList);
            UserInfoType[] result = new UserInfoType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "UserInfo" element
     */
    public UserInfoType getUserInfoArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserInfoType target = null;
            target = (UserInfoType)get_store().find_element_user(USERINFO$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "UserInfo" element
     */
    public int sizeOfUserInfoArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(USERINFO$2);
        }
    }
    
    /**
     * Sets array of all "UserInfo" element
     */
    public void setUserInfoArray(UserInfoType[] userInfoArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(userInfoArray, USERINFO$2);
        }
    }
    
    /**
     * Sets ith "UserInfo" element
     */
    public void setUserInfoArray(int i, UserInfoType userInfo)
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserInfoType target = null;
            target = (UserInfoType)get_store().find_element_user(USERINFO$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(userInfo);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "UserInfo" element
     */
    public UserInfoType insertNewUserInfo(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserInfoType target = null;
            target = (UserInfoType)get_store().insert_element_user(USERINFO$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "UserInfo" element
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
    
    /**
     * Removes the ith "UserInfo" element
     */
    public void removeUserInfo(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(USERINFO$2, i);
        }
    }
}
