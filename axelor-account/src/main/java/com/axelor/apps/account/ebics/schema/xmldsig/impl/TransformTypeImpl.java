/*
 * XML Type:  TransformType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: TransformType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.TransformType;

/**
 * An XML TransformType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class TransformTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements TransformType
{
    private static final long serialVersionUID = 1L;
    
    public TransformTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName XPATH$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "XPath");
    private static final javax.xml.namespace.QName ALGORITHM$2 = 
        new javax.xml.namespace.QName("", "Algorithm");
    
    
    /**
     * Gets array of all "XPath" elements
     */
    public java.lang.String[] getXPathArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(XPATH$0, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "XPath" element
     */
    public java.lang.String getXPathArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(XPATH$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "XPath" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetXPathArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(XPATH$0, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "XPath" element
     */
    public org.apache.xmlbeans.XmlString xgetXPathArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(XPATH$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlString)target;
        }
    }
    
    /**
     * Returns number of "XPath" element
     */
    public int sizeOfXPathArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(XPATH$0);
        }
    }
    
    /**
     * Sets array of all "XPath" element
     */
    public void setXPathArray(java.lang.String[] xPathArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(xPathArray, XPATH$0);
        }
    }
    
    /**
     * Sets ith "XPath" element
     */
    public void setXPathArray(int i, java.lang.String xPath)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(XPATH$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(xPath);
        }
    }
    
    /**
     * Sets (as xml) array of all "XPath" element
     */
    public void xsetXPathArray(org.apache.xmlbeans.XmlString[]xPathArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(xPathArray, XPATH$0);
        }
    }
    
    /**
     * Sets (as xml) ith "XPath" element
     */
    public void xsetXPathArray(int i, org.apache.xmlbeans.XmlString xPath)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(XPATH$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(xPath);
        }
    }
    
    /**
     * Inserts the value as the ith "XPath" element
     */
    public void insertXPath(int i, java.lang.String xPath)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(XPATH$0, i);
            target.setStringValue(xPath);
        }
    }
    
    /**
     * Appends the value as the last "XPath" element
     */
    public void addXPath(java.lang.String xPath)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(XPATH$0);
            target.setStringValue(xPath);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "XPath" element
     */
    public org.apache.xmlbeans.XmlString insertNewXPath(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(XPATH$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "XPath" element
     */
    public org.apache.xmlbeans.XmlString addNewXPath()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(XPATH$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "XPath" element
     */
    public void removeXPath(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(XPATH$0, i);
        }
    }
    
    /**
     * Gets the "Algorithm" attribute
     */
    public java.lang.String getAlgorithm()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ALGORITHM$2);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Algorithm" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetAlgorithm()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ALGORITHM$2);
            return target;
        }
    }
    
    /**
     * Sets the "Algorithm" attribute
     */
    public void setAlgorithm(java.lang.String algorithm)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ALGORITHM$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ALGORITHM$2);
            }
            target.setStringValue(algorithm);
        }
    }
    
    /**
     * Sets (as xml) the "Algorithm" attribute
     */
    public void xsetAlgorithm(org.apache.xmlbeans.XmlAnyURI algorithm)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ALGORITHM$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(ALGORITHM$2);
            }
            target.set(algorithm);
        }
    }
}
