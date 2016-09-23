/*
 * XML Type:  OrderSignatureDataType
 * Namespace: http://www.ebics.org/S001
 * Java type: OrderSignatureDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.s001.impl;

import com.axelor.apps.account.ebics.schema.s001.OrderSignatureDataType;
import com.axelor.apps.account.ebics.schema.s001.PartnerIDType;
import com.axelor.apps.account.ebics.schema.s001.SignatureVersionType;
import com.axelor.apps.account.ebics.schema.s001.UserIDType;
import com.axelor.apps.account.ebics.schema.xmldsig.X509DataType;

/**
 * An XML OrderSignatureDataType(@http://www.ebics.org/S001).
 *
 * This is a complex type.
 */
public class OrderSignatureDataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements OrderSignatureDataType
{
    private static final long serialVersionUID = 1L;
    
    public OrderSignatureDataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREVERSION$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "SignatureVersion");
    private static final javax.xml.namespace.QName SIGNATUREVALUE$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "SignatureValue");
    private static final javax.xml.namespace.QName PARTNERID$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "PartnerID");
    private static final javax.xml.namespace.QName USERID$6 = 
        new javax.xml.namespace.QName("http://www.ebics.org/S001", "UserID");
    private static final javax.xml.namespace.QName X509DATA$8 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509Data");
    
    
    /**
     * Gets the "SignatureVersion" element
     */
    public java.lang.String getSignatureVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREVERSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "SignatureVersion" element
     */
    public SignatureVersionType xgetSignatureVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureVersionType target = null;
            target = (SignatureVersionType)get_store().find_element_user(SIGNATUREVERSION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "SignatureVersion" element
     */
    public void setSignatureVersion(java.lang.String signatureVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SIGNATUREVERSION$0);
            }
            target.setStringValue(signatureVersion);
        }
    }
    
    /**
     * Sets (as xml) the "SignatureVersion" element
     */
    public void xsetSignatureVersion(SignatureVersionType signatureVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SignatureVersionType target = null;
            target = (SignatureVersionType)get_store().find_element_user(SIGNATUREVERSION$0, 0);
            if (target == null)
            {
                target = (SignatureVersionType)get_store().add_element_user(SIGNATUREVERSION$0);
            }
            target.set(signatureVersion);
        }
    }
    
    /**
     * Gets the "SignatureValue" element
     */
    public byte[] getSignatureValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "SignatureValue" element
     */
    public org.apache.xmlbeans.XmlBase64Binary xgetSignatureValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(SIGNATUREVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "SignatureValue" element
     */
    public void setSignatureValue(byte[] signatureValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SIGNATUREVALUE$2);
            }
            target.setByteArrayValue(signatureValue);
        }
    }
    
    /**
     * Sets (as xml) the "SignatureValue" element
     */
    public void xsetSignatureValue(org.apache.xmlbeans.XmlBase64Binary signatureValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(SIGNATUREVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlBase64Binary)get_store().add_element_user(SIGNATUREVALUE$2);
            }
            target.set(signatureValue);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTNERID$4, 0);
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
            target = (PartnerIDType)get_store().find_element_user(PARTNERID$4, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTNERID$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PARTNERID$4);
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
            target = (PartnerIDType)get_store().find_element_user(PARTNERID$4, 0);
            if (target == null)
            {
                target = (PartnerIDType)get_store().add_element_user(PARTNERID$4);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USERID$6, 0);
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
            target = (UserIDType)get_store().find_element_user(USERID$6, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USERID$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(USERID$6);
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
            target = (UserIDType)get_store().find_element_user(USERID$6, 0);
            if (target == null)
            {
                target = (UserIDType)get_store().add_element_user(USERID$6);
            }
            target.set(userID);
        }
    }
    
    /**
     * Gets the "X509Data" element
     */
    public X509DataType getX509Data()
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509DataType target = null;
            target = (X509DataType)get_store().find_element_user(X509DATA$8, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "X509Data" element
     */
    public boolean isSetX509Data()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(X509DATA$8) != 0;
        }
    }
    
    /**
     * Sets the "X509Data" element
     */
    public void setX509Data(X509DataType x509Data)
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509DataType target = null;
            target = (X509DataType)get_store().find_element_user(X509DATA$8, 0);
            if (target == null)
            {
                target = (X509DataType)get_store().add_element_user(X509DATA$8);
            }
            target.set(x509Data);
        }
    }
    
    /**
     * Appends and returns a new empty "X509Data" element
     */
    public X509DataType addNewX509Data()
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509DataType target = null;
            target = (X509DataType)get_store().add_element_user(X509DATA$8);
            return target;
        }
    }
    
    /**
     * Unsets the "X509Data" element
     */
    public void unsetX509Data()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(X509DATA$8, 0);
        }
    }
}
