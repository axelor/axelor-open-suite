/*
 * XML Type:  AddressInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: AddressInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import org.apache.xmlbeans.impl.values.XmlComplexContentImpl;

import com.axelor.apps.account.ebics.schema.h003.AddressInfoType;
import com.axelor.apps.account.ebics.schema.h003.NameType;

/**
 * An XML AddressInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class AddressInfoTypeImpl extends XmlComplexContentImpl implements AddressInfoType
{
    private static final long serialVersionUID = 1L;
    
    public AddressInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName NAME$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Name");
    private static final javax.xml.namespace.QName STREET$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Street");
    private static final javax.xml.namespace.QName POSTCODE$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "PostCode");
    private static final javax.xml.namespace.QName CITY$6 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "City");
    private static final javax.xml.namespace.QName REGION$8 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Region");
    private static final javax.xml.namespace.QName COUNTRY$10 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Country");
    
    
    /**
     * Gets the "Name" element
     */
    public java.lang.String getName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NAME$0, 0);
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
            target = (NameType)get_store().find_element_user(NAME$0, 0);
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
            return get_store().count_elements(NAME$0) != 0;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NAME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(NAME$0);
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
            target = (NameType)get_store().find_element_user(NAME$0, 0);
            if (target == null)
            {
                target = (NameType)get_store().add_element_user(NAME$0);
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
            get_store().remove_element(NAME$0, 0);
        }
    }
    
    /**
     * Gets the "Street" element
     */
    public java.lang.String getStreet()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STREET$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Street" element
     */
    public NameType xgetStreet()
    {
        synchronized (monitor())
        {
            check_orphaned();
            NameType target = null;
            target = (NameType)get_store().find_element_user(STREET$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "Street" element
     */
    public boolean isSetStreet()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STREET$2) != 0;
        }
    }
    
    /**
     * Sets the "Street" element
     */
    public void setStreet(java.lang.String street)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STREET$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STREET$2);
            }
            target.setStringValue(street);
        }
    }
    
    /**
     * Sets (as xml) the "Street" element
     */
    public void xsetStreet(NameType street)
    {
        synchronized (monitor())
        {
            check_orphaned();
            NameType target = null;
            target = (NameType)get_store().find_element_user(STREET$2, 0);
            if (target == null)
            {
                target = (NameType)get_store().add_element_user(STREET$2);
            }
            target.set(street);
        }
    }
    
    /**
     * Unsets the "Street" element
     */
    public void unsetStreet()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STREET$2, 0);
        }
    }
    
    /**
     * Gets the "PostCode" element
     */
    public java.lang.String getPostCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(POSTCODE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "PostCode" element
     */
    public org.apache.xmlbeans.XmlToken xgetPostCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlToken target = null;
            target = (org.apache.xmlbeans.XmlToken)get_store().find_element_user(POSTCODE$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "PostCode" element
     */
    public boolean isSetPostCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(POSTCODE$4) != 0;
        }
    }
    
    /**
     * Sets the "PostCode" element
     */
    public void setPostCode(java.lang.String postCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(POSTCODE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(POSTCODE$4);
            }
            target.setStringValue(postCode);
        }
    }
    
    /**
     * Sets (as xml) the "PostCode" element
     */
    public void xsetPostCode(org.apache.xmlbeans.XmlToken postCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlToken target = null;
            target = (org.apache.xmlbeans.XmlToken)get_store().find_element_user(POSTCODE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlToken)get_store().add_element_user(POSTCODE$4);
            }
            target.set(postCode);
        }
    }
    
    /**
     * Unsets the "PostCode" element
     */
    public void unsetPostCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(POSTCODE$4, 0);
        }
    }
    
    /**
     * Gets the "City" element
     */
    public java.lang.String getCity()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CITY$6, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "City" element
     */
    public NameType xgetCity()
    {
        synchronized (monitor())
        {
            check_orphaned();
            NameType target = null;
            target = (NameType)get_store().find_element_user(CITY$6, 0);
            return target;
        }
    }
    
    /**
     * True if has "City" element
     */
    public boolean isSetCity()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CITY$6) != 0;
        }
    }
    
    /**
     * Sets the "City" element
     */
    public void setCity(java.lang.String city)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CITY$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(CITY$6);
            }
            target.setStringValue(city);
        }
    }
    
    /**
     * Sets (as xml) the "City" element
     */
    public void xsetCity(NameType city)
    {
        synchronized (monitor())
        {
            check_orphaned();
            NameType target = null;
            target = (NameType)get_store().find_element_user(CITY$6, 0);
            if (target == null)
            {
                target = (NameType)get_store().add_element_user(CITY$6);
            }
            target.set(city);
        }
    }
    
    /**
     * Unsets the "City" element
     */
    public void unsetCity()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CITY$6, 0);
        }
    }
    
    /**
     * Gets the "Region" element
     */
    public java.lang.String getRegion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(REGION$8, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Region" element
     */
    public NameType xgetRegion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            NameType target = null;
            target = (NameType)get_store().find_element_user(REGION$8, 0);
            return target;
        }
    }
    
    /**
     * True if has "Region" element
     */
    public boolean isSetRegion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(REGION$8) != 0;
        }
    }
    
    /**
     * Sets the "Region" element
     */
    public void setRegion(java.lang.String region)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(REGION$8, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(REGION$8);
            }
            target.setStringValue(region);
        }
    }
    
    /**
     * Sets (as xml) the "Region" element
     */
    public void xsetRegion(NameType region)
    {
        synchronized (monitor())
        {
            check_orphaned();
            NameType target = null;
            target = (NameType)get_store().find_element_user(REGION$8, 0);
            if (target == null)
            {
                target = (NameType)get_store().add_element_user(REGION$8);
            }
            target.set(region);
        }
    }
    
    /**
     * Unsets the "Region" element
     */
    public void unsetRegion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(REGION$8, 0);
        }
    }
    
    /**
     * Gets the "Country" element
     */
    public java.lang.String getCountry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COUNTRY$10, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Country" element
     */
    public NameType xgetCountry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            NameType target = null;
            target = (NameType)get_store().find_element_user(COUNTRY$10, 0);
            return target;
        }
    }
    
    /**
     * True if has "Country" element
     */
    public boolean isSetCountry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COUNTRY$10) != 0;
        }
    }
    
    /**
     * Sets the "Country" element
     */
    public void setCountry(java.lang.String country)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COUNTRY$10, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(COUNTRY$10);
            }
            target.setStringValue(country);
        }
    }
    
    /**
     * Sets (as xml) the "Country" element
     */
    public void xsetCountry(NameType country)
    {
        synchronized (monitor())
        {
            check_orphaned();
            NameType target = null;
            target = (NameType)get_store().find_element_user(COUNTRY$10, 0);
            if (target == null)
            {
                target = (NameType)get_store().add_element_user(COUNTRY$10);
            }
            target.set(country);
        }
    }
    
    /**
     * Unsets the "Country" element
     */
    public void unsetCountry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COUNTRY$10, 0);
        }
    }
}
