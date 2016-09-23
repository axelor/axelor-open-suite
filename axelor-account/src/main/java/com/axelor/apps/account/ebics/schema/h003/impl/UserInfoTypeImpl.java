/*
 * XML Type:  UserInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: UserInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.NameType;
import com.axelor.apps.account.ebics.schema.h003.UserInfoType;
import com.axelor.apps.account.ebics.schema.h003.UserPermissionType;
import com.axelor.apps.account.ebics.schema.h003.UserStatusType;

/**
 * An XML UserInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class UserInfoTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements UserInfoType
{
    private static final long serialVersionUID = 1L;
    
    public UserInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName USERID$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "UserID");
    private static final javax.xml.namespace.QName NAME$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Name");
    private static final javax.xml.namespace.QName PERMISSION$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Permission");
    
    
    /**
     * Gets the "UserID" element
     */
    public UserInfoType.UserID getUserID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserInfoType.UserID target = null;
            target = (UserInfoType.UserID)get_store().find_element_user(USERID$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "UserID" element
     */
    public void setUserID(UserInfoType.UserID userID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserInfoType.UserID target = null;
            target = (UserInfoType.UserID)get_store().find_element_user(USERID$0, 0);
            if (target == null)
            {
                target = (UserInfoType.UserID)get_store().add_element_user(USERID$0);
            }
            target.set(userID);
        }
    }
    
    /**
     * Appends and returns a new empty "UserID" element
     */
    public UserInfoType.UserID addNewUserID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserInfoType.UserID target = null;
            target = (UserInfoType.UserID)get_store().add_element_user(USERID$0);
            return target;
        }
    }
    
    /**
     * Gets the "Name" element
     */
    public java.lang.String getName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NAME$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Name" element
     */
    public NameType xgetName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            NameType target = null;
            target = (NameType)get_store().find_element_user(NAME$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "Name" element
     */
    public boolean isSetName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(NAME$2) != 0;
        }
    }
    
    /**
     * Sets the "Name" element
     */
    public void setName(java.lang.String name)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NAME$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(NAME$2);
            }
            target.setStringValue(name);
        }
    }
    
    /**
     * Sets (as xml) the "Name" element
     */
    public void xsetName(NameType name)
    {
        synchronized (monitor())
        {
            check_orphaned();
            NameType target = null;
            target = (NameType)get_store().find_element_user(NAME$2, 0);
            if (target == null)
            {
                target = (NameType)get_store().add_element_user(NAME$2);
            }
            target.set(name);
        }
    }
    
    /**
     * Unsets the "Name" element
     */
    public void unsetName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(NAME$2, 0);
        }
    }
    
    /**
     * Gets array of all "Permission" elements
     */
    public UserPermissionType[] getPermissionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PERMISSION$4, targetList);
            UserPermissionType[] result = new UserPermissionType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "Permission" element
     */
    public UserPermissionType getPermissionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserPermissionType target = null;
            target = (UserPermissionType)get_store().find_element_user(PERMISSION$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "Permission" element
     */
    public int sizeOfPermissionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PERMISSION$4);
        }
    }
    
    /**
     * Sets array of all "Permission" element
     */
    public void setPermissionArray(UserPermissionType[] permissionArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(permissionArray, PERMISSION$4);
        }
    }
    
    /**
     * Sets ith "Permission" element
     */
    public void setPermissionArray(int i, UserPermissionType permission)
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserPermissionType target = null;
            target = (UserPermissionType)get_store().find_element_user(PERMISSION$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(permission);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "Permission" element
     */
    public UserPermissionType insertNewPermission(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserPermissionType target = null;
            target = (UserPermissionType)get_store().insert_element_user(PERMISSION$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "Permission" element
     */
    public UserPermissionType addNewPermission()
    {
        synchronized (monitor())
        {
            check_orphaned();
            UserPermissionType target = null;
            target = (UserPermissionType)get_store().add_element_user(PERMISSION$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "Permission" element
     */
    public void removePermission(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PERMISSION$4, i);
        }
    }
    /**
     * An XML UserID(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of UserInfoType$UserID.
     */
    public static class UserIDImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements UserInfoType.UserID
    {
        private static final long serialVersionUID = 1L;
        
        public UserIDImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected UserIDImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName STATUS$0 = 
            new javax.xml.namespace.QName("", "Status");
        
        
        /**
         * Gets the "Status" attribute
         */
        public int getStatus()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(STATUS$0);
                if (target == null)
                {
                    return 0;
                }
                return target.getIntValue();
            }
        }
        
        /**
         * Gets (as xml) the "Status" attribute
         */
        public UserStatusType xgetStatus()
        {
            synchronized (monitor())
            {
                check_orphaned();
                UserStatusType target = null;
                target = (UserStatusType)get_store().find_attribute_user(STATUS$0);
                return target;
            }
        }
        
        /**
         * Sets the "Status" attribute
         */
        public void setStatus(int status)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(STATUS$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(STATUS$0);
                }
                target.setIntValue(status);
            }
        }
        
        /**
         * Sets (as xml) the "Status" attribute
         */
        public void xsetStatus(UserStatusType status)
        {
            synchronized (monitor())
            {
                check_orphaned();
                UserStatusType target = null;
                target = (UserStatusType)get_store().find_attribute_user(STATUS$0);
                if (target == null)
                {
                    target = (UserStatusType)get_store().add_attribute_user(STATUS$0);
                }
                target.set(status);
            }
        }
    }
}
