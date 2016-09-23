/*
 * XML Type:  HPDAccessParamsType
 * Namespace: http://www.ebics.org/H003
 * Java type: HPDAccessParamsType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.HPDAccessParamsType;
import com.axelor.apps.account.ebics.schema.h003.HostIDType;
import com.axelor.apps.account.ebics.schema.h003.TimestampType;

/**
 * An XML HPDAccessParamsType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class HPDAccessParamsTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements HPDAccessParamsType
{
    private static final long serialVersionUID = 1L;
    
    public HPDAccessParamsTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName URL$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "URL");
    private static final javax.xml.namespace.QName INSTITUTE$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Institute");
    private static final javax.xml.namespace.QName HOSTID$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "HostID");
    
    
    /**
     * Gets array of all "URL" elements
     */
    public HPDAccessParamsType.URL[] getURLArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(URL$0, targetList);
            HPDAccessParamsType.URL[] result = new HPDAccessParamsType.URL[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "URL" element
     */
    public HPDAccessParamsType.URL getURLArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HPDAccessParamsType.URL target = null;
            target = (HPDAccessParamsType.URL)get_store().find_element_user(URL$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "URL" element
     */
    public int sizeOfURLArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(URL$0);
        }
    }
    
    /**
     * Sets array of all "URL" element
     */
    public void setURLArray(HPDAccessParamsType.URL[] urlArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(urlArray, URL$0);
        }
    }
    
    /**
     * Sets ith "URL" element
     */
    public void setURLArray(int i, HPDAccessParamsType.URL url)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HPDAccessParamsType.URL target = null;
            target = (HPDAccessParamsType.URL)get_store().find_element_user(URL$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(url);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "URL" element
     */
    public HPDAccessParamsType.URL insertNewURL(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HPDAccessParamsType.URL target = null;
            target = (HPDAccessParamsType.URL)get_store().insert_element_user(URL$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "URL" element
     */
    public HPDAccessParamsType.URL addNewURL()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HPDAccessParamsType.URL target = null;
            target = (HPDAccessParamsType.URL)get_store().add_element_user(URL$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "URL" element
     */
    public void removeURL(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(URL$0, i);
        }
    }
    
    /**
     * Gets the "Institute" element
     */
    public java.lang.String getInstitute()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(INSTITUTE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Institute" element
     */
    public HPDAccessParamsType.Institute xgetInstitute()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HPDAccessParamsType.Institute target = null;
            target = (HPDAccessParamsType.Institute)get_store().find_element_user(INSTITUTE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "Institute" element
     */
    public void setInstitute(java.lang.String institute)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(INSTITUTE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(INSTITUTE$2);
            }
            target.setStringValue(institute);
        }
    }
    
    /**
     * Sets (as xml) the "Institute" element
     */
    public void xsetInstitute(HPDAccessParamsType.Institute institute)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HPDAccessParamsType.Institute target = null;
            target = (HPDAccessParamsType.Institute)get_store().find_element_user(INSTITUTE$2, 0);
            if (target == null)
            {
                target = (HPDAccessParamsType.Institute)get_store().add_element_user(INSTITUTE$2);
            }
            target.set(institute);
        }
    }
    
    /**
     * Gets the "HostID" element
     */
    public java.lang.String getHostID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(HOSTID$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "HostID" element
     */
    public HostIDType xgetHostID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            HostIDType target = null;
            target = (HostIDType)get_store().find_element_user(HOSTID$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "HostID" element
     */
    public boolean isSetHostID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(HOSTID$4) != 0;
        }
    }
    
    /**
     * Sets the "HostID" element
     */
    public void setHostID(java.lang.String hostID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(HOSTID$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(HOSTID$4);
            }
            target.setStringValue(hostID);
        }
    }
    
    /**
     * Sets (as xml) the "HostID" element
     */
    public void xsetHostID(HostIDType hostID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            HostIDType target = null;
            target = (HostIDType)get_store().find_element_user(HOSTID$4, 0);
            if (target == null)
            {
                target = (HostIDType)get_store().add_element_user(HOSTID$4);
            }
            target.set(hostID);
        }
    }
    
    /**
     * Unsets the "HostID" element
     */
    public void unsetHostID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(HOSTID$4, 0);
        }
    }
    /**
     * An XML URL(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of HPDAccessParamsType$URL.
     */
    public static class URLImpl extends org.apache.xmlbeans.impl.values.JavaUriHolderEx implements HPDAccessParamsType.URL
    {
        private static final long serialVersionUID = 1L;
        
        public URLImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected URLImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName VALIDFROM$0 = 
            new javax.xml.namespace.QName("", "valid_from");
        
        
        /**
         * Gets the "valid_from" attribute
         */
        public java.util.Calendar getValidFrom()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VALIDFROM$0);
                if (target == null)
                {
                    return null;
                }
                return target.getCalendarValue();
            }
        }
        
        /**
         * Gets (as xml) the "valid_from" attribute
         */
        public TimestampType xgetValidFrom()
        {
            synchronized (monitor())
            {
                check_orphaned();
                TimestampType target = null;
                target = (TimestampType)get_store().find_attribute_user(VALIDFROM$0);
                return target;
            }
        }
        
        /**
         * True if has "valid_from" attribute
         */
        public boolean isSetValidFrom()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(VALIDFROM$0) != null;
            }
        }
        
        /**
         * Sets the "valid_from" attribute
         */
        public void setValidFrom(java.util.Calendar validFrom)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VALIDFROM$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VALIDFROM$0);
                }
                target.setCalendarValue(validFrom);
            }
        }
        
        /**
         * Sets (as xml) the "valid_from" attribute
         */
        public void xsetValidFrom(TimestampType validFrom)
        {
            synchronized (monitor())
            {
                check_orphaned();
                TimestampType target = null;
                target = (TimestampType)get_store().find_attribute_user(VALIDFROM$0);
                if (target == null)
                {
                    target = (TimestampType)get_store().add_attribute_user(VALIDFROM$0);
                }
                target.set(validFrom);
            }
        }
        
        /**
         * Unsets the "valid_from" attribute
         */
        public void unsetValidFrom()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(VALIDFROM$0);
            }
        }
    }
    /**
     * An XML Institute(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of HPDAccessParamsType$Institute.
     */
    public static class InstituteImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements HPDAccessParamsType.Institute
    {
        private static final long serialVersionUID = 1L;
        
        public InstituteImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, false);
        }
        
        protected InstituteImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
    }
}
