/*
 * XML Type:  SPKIDataType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: SPKIDataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.SPKIDataType;

/**
 * An XML SPKIDataType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class SPKIDataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements SPKIDataType
{
    private static final long serialVersionUID = 1L;
    
    public SPKIDataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SPKISEXP$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SPKISexp");
    
    
    /**
     * Gets array of all "SPKISexp" elements
     */
    public byte[][] getSPKISexpArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SPKISEXP$0, targetList);
            byte[][] result = new byte[targetList.size()][];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getByteArrayValue();
            return result;
        }
    }
    
    /**
     * Gets ith "SPKISexp" element
     */
    public byte[] getSPKISexpArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SPKISEXP$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "SPKISexp" elements
     */
    public org.apache.xmlbeans.XmlBase64Binary[] xgetSPKISexpArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SPKISEXP$0, targetList);
            org.apache.xmlbeans.XmlBase64Binary[] result = new org.apache.xmlbeans.XmlBase64Binary[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "SPKISexp" element
     */
    public org.apache.xmlbeans.XmlBase64Binary xgetSPKISexpArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(SPKISEXP$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlBase64Binary)target;
        }
    }
    
    /**
     * Returns number of "SPKISexp" element
     */
    public int sizeOfSPKISexpArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SPKISEXP$0);
        }
    }
    
    /**
     * Sets array of all "SPKISexp" element
     */
    public void setSPKISexpArray(byte[][] spkiSexpArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(spkiSexpArray, SPKISEXP$0);
        }
    }
    
    /**
     * Sets ith "SPKISexp" element
     */
    public void setSPKISexpArray(int i, byte[] spkiSexp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SPKISEXP$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setByteArrayValue(spkiSexp);
        }
    }
    
    /**
     * Sets (as xml) array of all "SPKISexp" element
     */
    public void xsetSPKISexpArray(org.apache.xmlbeans.XmlBase64Binary[]spkiSexpArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(spkiSexpArray, SPKISEXP$0);
        }
    }
    
    /**
     * Sets (as xml) ith "SPKISexp" element
     */
    public void xsetSPKISexpArray(int i, org.apache.xmlbeans.XmlBase64Binary spkiSexp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(SPKISEXP$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(spkiSexp);
        }
    }
    
    /**
     * Inserts the value as the ith "SPKISexp" element
     */
    public void insertSPKISexp(int i, byte[] spkiSexp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(SPKISEXP$0, i);
            target.setByteArrayValue(spkiSexp);
        }
    }
    
    /**
     * Appends the value as the last "SPKISexp" element
     */
    public void addSPKISexp(byte[] spkiSexp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SPKISEXP$0);
            target.setByteArrayValue(spkiSexp);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "SPKISexp" element
     */
    public org.apache.xmlbeans.XmlBase64Binary insertNewSPKISexp(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().insert_element_user(SPKISEXP$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "SPKISexp" element
     */
    public org.apache.xmlbeans.XmlBase64Binary addNewSPKISexp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().add_element_user(SPKISEXP$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "SPKISexp" element
     */
    public void removeSPKISexp(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SPKISEXP$0, i);
        }
    }
}
