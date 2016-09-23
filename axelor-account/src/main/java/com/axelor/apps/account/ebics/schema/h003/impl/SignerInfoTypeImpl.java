/*
 * XML Type:  SignerInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: SignerInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.AuthorisationLevelType;
import com.axelor.apps.account.ebics.schema.h003.NameType;
import com.axelor.apps.account.ebics.schema.h003.PartnerIDType;
import com.axelor.apps.account.ebics.schema.h003.SignerInfoType;
import com.axelor.apps.account.ebics.schema.h003.TimestampType;
import com.axelor.apps.account.ebics.schema.h003.UserIDType;

/**
 * An XML SignerInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class SignerInfoTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignerInfoType
{
    private static final long serialVersionUID = 1L;
    
    public SignerInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PARTNERID$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "PartnerID");
    private static final javax.xml.namespace.QName USERID$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "UserID");
    private static final javax.xml.namespace.QName NAME$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Name");
    private static final javax.xml.namespace.QName TIMESTAMP$6 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Timestamp");
    private static final javax.xml.namespace.QName PERMISSION$8 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Permission");
    
    
    /**
     * Gets the "PartnerID" element
     */
    public java.lang.String getPartnerID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTNERID$0, 0);
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
            target = (PartnerIDType)get_store().find_element_user(PARTNERID$0, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTNERID$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PARTNERID$0);
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
            target = (PartnerIDType)get_store().find_element_user(PARTNERID$0, 0);
            if (target == null)
            {
                target = (PartnerIDType)get_store().add_element_user(PARTNERID$0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USERID$2, 0);
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
            target = (UserIDType)get_store().find_element_user(USERID$2, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USERID$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(USERID$2);
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
            target = (UserIDType)get_store().find_element_user(USERID$2, 0);
            if (target == null)
            {
                target = (UserIDType)get_store().add_element_user(USERID$2);
            }
            target.set(userID);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NAME$4, 0);
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
            target = (NameType)get_store().find_element_user(NAME$4, 0);
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
            return get_store().count_elements(NAME$4) != 0;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NAME$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(NAME$4);
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
            target = (NameType)get_store().find_element_user(NAME$4, 0);
            if (target == null)
            {
                target = (NameType)get_store().add_element_user(NAME$4);
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
            get_store().remove_element(NAME$4, 0);
        }
    }
    
    /**
     * Gets the "Timestamp" element
     */
    public java.util.Calendar getTimestamp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TIMESTAMP$6, 0);
            if (target == null)
            {
                return null;
            }
            return target.getCalendarValue();
        }
    }
    
    /**
     * Gets (as xml) the "Timestamp" element
     */
    public TimestampType xgetTimestamp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            TimestampType target = null;
            target = (TimestampType)get_store().find_element_user(TIMESTAMP$6, 0);
            return target;
        }
    }
    
    /**
     * Sets the "Timestamp" element
     */
    public void setTimestamp(java.util.Calendar timestamp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TIMESTAMP$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(TIMESTAMP$6);
            }
            target.setCalendarValue(timestamp);
        }
    }
    
    /**
     * Sets (as xml) the "Timestamp" element
     */
    public void xsetTimestamp(TimestampType timestamp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            TimestampType target = null;
            target = (TimestampType)get_store().find_element_user(TIMESTAMP$6, 0);
            if (target == null)
            {
                target = (TimestampType)get_store().add_element_user(TIMESTAMP$6);
            }
            target.set(timestamp);
        }
    }
    
    /**
     * Gets the "Permission" element
     */
    public SignerInfoType.Permission getPermission()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignerInfoType.Permission target = null;
            target = (SignerInfoType.Permission)get_store().find_element_user(PERMISSION$8, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "Permission" element
     */
    public void setPermission(SignerInfoType.Permission permission)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignerInfoType.Permission target = null;
            target = (SignerInfoType.Permission)get_store().find_element_user(PERMISSION$8, 0);
            if (target == null)
            {
                target = (SignerInfoType.Permission)get_store().add_element_user(PERMISSION$8);
            }
            target.set(permission);
        }
    }
    
    /**
     * Appends and returns a new empty "Permission" element
     */
    public SignerInfoType.Permission addNewPermission()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignerInfoType.Permission target = null;
            target = (SignerInfoType.Permission)get_store().add_element_user(PERMISSION$8);
            return target;
        }
    }
    /**
     * An XML Permission(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public static class PermissionImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SignerInfoType.Permission
    {
        private static final long serialVersionUID = 1L;
        
        public PermissionImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName AUTHORISATIONLEVEL$0 = 
            new javax.xml.namespace.QName("", "AuthorisationLevel");
        
        
        /**
         * Gets the "AuthorisationLevel" attribute
         */
        public AuthorisationLevelType.Enum getAuthorisationLevel()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHORISATIONLEVEL$0);
                if (target == null)
                {
                    return null;
                }
                return (AuthorisationLevelType.Enum)target.getEnumValue();
            }
        }
        
        /**
         * Gets (as xml) the "AuthorisationLevel" attribute
         */
        public AuthorisationLevelType xgetAuthorisationLevel()
        {
            synchronized (monitor())
            {
                check_orphaned();
                AuthorisationLevelType target = null;
                target = (AuthorisationLevelType)get_store().find_attribute_user(AUTHORISATIONLEVEL$0);
                return target;
            }
        }
        
        /**
         * Sets the "AuthorisationLevel" attribute
         */
        public void setAuthorisationLevel(AuthorisationLevelType.Enum authorisationLevel)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHORISATIONLEVEL$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(AUTHORISATIONLEVEL$0);
                }
                target.setEnumValue(authorisationLevel);
            }
        }
        
        /**
         * Sets (as xml) the "AuthorisationLevel" attribute
         */
        public void xsetAuthorisationLevel(AuthorisationLevelType authorisationLevel)
        {
            synchronized (monitor())
            {
                check_orphaned();
                AuthorisationLevelType target = null;
                target = (AuthorisationLevelType)get_store().find_attribute_user(AUTHORISATIONLEVEL$0);
                if (target == null)
                {
                    target = (AuthorisationLevelType)get_store().add_attribute_user(AUTHORISATIONLEVEL$0);
                }
                target.set(authorisationLevel);
            }
        }
    }
}
