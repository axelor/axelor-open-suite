/*
 * XML Type:  KeyInfoType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: KeyInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.KeyInfoType;
import com.axelor.apps.account.ebics.schema.xmldsig.KeyValueType;
import com.axelor.apps.account.ebics.schema.xmldsig.PGPDataType;
import com.axelor.apps.account.ebics.schema.xmldsig.RetrievalMethodType;
import com.axelor.apps.account.ebics.schema.xmldsig.SPKIDataType;
import com.axelor.apps.account.ebics.schema.xmldsig.X509DataType;

/**
 * An XML KeyInfoType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class KeyInfoTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements KeyInfoType
{
    private static final long serialVersionUID = 1L;
    
    public KeyInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName KEYNAME$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "KeyName");
    private static final javax.xml.namespace.QName KEYVALUE$2 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "KeyValue");
    private static final javax.xml.namespace.QName RETRIEVALMETHOD$4 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "RetrievalMethod");
    private static final javax.xml.namespace.QName X509DATA$6 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509Data");
    private static final javax.xml.namespace.QName PGPDATA$8 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "PGPData");
    private static final javax.xml.namespace.QName SPKIDATA$10 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SPKIData");
    private static final javax.xml.namespace.QName MGMTDATA$12 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "MgmtData");
    private static final javax.xml.namespace.QName ID$14 = 
        new javax.xml.namespace.QName("", "Id");
    
    
    /**
     * Gets array of all "KeyName" elements
     */
    public java.lang.String[] getKeyNameArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(KEYNAME$0, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "KeyName" element
     */
    public java.lang.String getKeyNameArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(KEYNAME$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "KeyName" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetKeyNameArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(KEYNAME$0, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "KeyName" element
     */
    public org.apache.xmlbeans.XmlString xgetKeyNameArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(KEYNAME$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlString)target;
        }
    }
    
    /**
     * Returns number of "KeyName" element
     */
    public int sizeOfKeyNameArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(KEYNAME$0);
        }
    }
    
    /**
     * Sets array of all "KeyName" element
     */
    public void setKeyNameArray(java.lang.String[] keyNameArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(keyNameArray, KEYNAME$0);
        }
    }
    
    /**
     * Sets ith "KeyName" element
     */
    public void setKeyNameArray(int i, java.lang.String keyName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(KEYNAME$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(keyName);
        }
    }
    
    /**
     * Sets (as xml) array of all "KeyName" element
     */
    public void xsetKeyNameArray(org.apache.xmlbeans.XmlString[]keyNameArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(keyNameArray, KEYNAME$0);
        }
    }
    
    /**
     * Sets (as xml) ith "KeyName" element
     */
    public void xsetKeyNameArray(int i, org.apache.xmlbeans.XmlString keyName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(KEYNAME$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(keyName);
        }
    }
    
    /**
     * Inserts the value as the ith "KeyName" element
     */
    public void insertKeyName(int i, java.lang.String keyName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(KEYNAME$0, i);
            target.setStringValue(keyName);
        }
    }
    
    /**
     * Appends the value as the last "KeyName" element
     */
    public void addKeyName(java.lang.String keyName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(KEYNAME$0);
            target.setStringValue(keyName);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "KeyName" element
     */
    public org.apache.xmlbeans.XmlString insertNewKeyName(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(KEYNAME$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "KeyName" element
     */
    public org.apache.xmlbeans.XmlString addNewKeyName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(KEYNAME$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "KeyName" element
     */
    public void removeKeyName(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(KEYNAME$0, i);
        }
    }
    
    /**
     * Gets array of all "KeyValue" elements
     */
    public KeyValueType[] getKeyValueArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(KEYVALUE$2, targetList);
            KeyValueType[] result = new KeyValueType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "KeyValue" element
     */
    public KeyValueType getKeyValueArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            KeyValueType target = null;
            target = (KeyValueType)get_store().find_element_user(KEYVALUE$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "KeyValue" element
     */
    public int sizeOfKeyValueArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(KEYVALUE$2);
        }
    }
    
    /**
     * Sets array of all "KeyValue" element
     */
    public void setKeyValueArray(KeyValueType[] keyValueArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(keyValueArray, KEYVALUE$2);
        }
    }
    
    /**
     * Sets ith "KeyValue" element
     */
    public void setKeyValueArray(int i, KeyValueType keyValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            KeyValueType target = null;
            target = (KeyValueType)get_store().find_element_user(KEYVALUE$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(keyValue);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "KeyValue" element
     */
    public KeyValueType insertNewKeyValue(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            KeyValueType target = null;
            target = (KeyValueType)get_store().insert_element_user(KEYVALUE$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "KeyValue" element
     */
    public KeyValueType addNewKeyValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            KeyValueType target = null;
            target = (KeyValueType)get_store().add_element_user(KEYVALUE$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "KeyValue" element
     */
    public void removeKeyValue(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(KEYVALUE$2, i);
        }
    }
    
    /**
     * Gets array of all "RetrievalMethod" elements
     */
    public RetrievalMethodType[] getRetrievalMethodArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RETRIEVALMETHOD$4, targetList);
            RetrievalMethodType[] result = new RetrievalMethodType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "RetrievalMethod" element
     */
    public RetrievalMethodType getRetrievalMethodArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            RetrievalMethodType target = null;
            target = (RetrievalMethodType)get_store().find_element_user(RETRIEVALMETHOD$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "RetrievalMethod" element
     */
    public int sizeOfRetrievalMethodArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RETRIEVALMETHOD$4);
        }
    }
    
    /**
     * Sets array of all "RetrievalMethod" element
     */
    public void setRetrievalMethodArray(RetrievalMethodType[] retrievalMethodArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(retrievalMethodArray, RETRIEVALMETHOD$4);
        }
    }
    
    /**
     * Sets ith "RetrievalMethod" element
     */
    public void setRetrievalMethodArray(int i, RetrievalMethodType retrievalMethod)
    {
        synchronized (monitor())
        {
            check_orphaned();
            RetrievalMethodType target = null;
            target = (RetrievalMethodType)get_store().find_element_user(RETRIEVALMETHOD$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(retrievalMethod);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "RetrievalMethod" element
     */
    public RetrievalMethodType insertNewRetrievalMethod(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            RetrievalMethodType target = null;
            target = (RetrievalMethodType)get_store().insert_element_user(RETRIEVALMETHOD$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "RetrievalMethod" element
     */
    public RetrievalMethodType addNewRetrievalMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            RetrievalMethodType target = null;
            target = (RetrievalMethodType)get_store().add_element_user(RETRIEVALMETHOD$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "RetrievalMethod" element
     */
    public void removeRetrievalMethod(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RETRIEVALMETHOD$4, i);
        }
    }
    
    /**
     * Gets array of all "X509Data" elements
     */
    public X509DataType[] getX509DataArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(X509DATA$6, targetList);
            X509DataType[] result = new X509DataType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "X509Data" element
     */
    public X509DataType getX509DataArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509DataType target = null;
            target = (X509DataType)get_store().find_element_user(X509DATA$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "X509Data" element
     */
    public int sizeOfX509DataArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(X509DATA$6);
        }
    }
    
    /**
     * Sets array of all "X509Data" element
     */
    public void setX509DataArray(X509DataType[] x509DataArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(x509DataArray, X509DATA$6);
        }
    }
    
    /**
     * Sets ith "X509Data" element
     */
    public void setX509DataArray(int i, X509DataType x509Data)
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509DataType target = null;
            target = (X509DataType)get_store().find_element_user(X509DATA$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(x509Data);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "X509Data" element
     */
    public X509DataType insertNewX509Data(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509DataType target = null;
            target = (X509DataType)get_store().insert_element_user(X509DATA$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "X509Data" element
     */
    public X509DataType addNewX509Data()
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509DataType target = null;
            target = (X509DataType)get_store().add_element_user(X509DATA$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "X509Data" element
     */
    public void removeX509Data(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(X509DATA$6, i);
        }
    }
    
    /**
     * Gets array of all "PGPData" elements
     */
    public PGPDataType[] getPGPDataArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PGPDATA$8, targetList);
            PGPDataType[] result = new PGPDataType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "PGPData" element
     */
    public PGPDataType getPGPDataArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            PGPDataType target = null;
            target = (PGPDataType)get_store().find_element_user(PGPDATA$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "PGPData" element
     */
    public int sizeOfPGPDataArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PGPDATA$8);
        }
    }
    
    /**
     * Sets array of all "PGPData" element
     */
    public void setPGPDataArray(PGPDataType[] pgpDataArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(pgpDataArray, PGPDATA$8);
        }
    }
    
    /**
     * Sets ith "PGPData" element
     */
    public void setPGPDataArray(int i, PGPDataType pgpData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            PGPDataType target = null;
            target = (PGPDataType)get_store().find_element_user(PGPDATA$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(pgpData);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "PGPData" element
     */
    public PGPDataType insertNewPGPData(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            PGPDataType target = null;
            target = (PGPDataType)get_store().insert_element_user(PGPDATA$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "PGPData" element
     */
    public PGPDataType addNewPGPData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            PGPDataType target = null;
            target = (PGPDataType)get_store().add_element_user(PGPDATA$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "PGPData" element
     */
    public void removePGPData(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PGPDATA$8, i);
        }
    }
    
    /**
     * Gets array of all "SPKIData" elements
     */
    public SPKIDataType[] getSPKIDataArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SPKIDATA$10, targetList);
            SPKIDataType[] result = new SPKIDataType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "SPKIData" element
     */
    public SPKIDataType getSPKIDataArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SPKIDataType target = null;
            target = (SPKIDataType)get_store().find_element_user(SPKIDATA$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "SPKIData" element
     */
    public int sizeOfSPKIDataArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SPKIDATA$10);
        }
    }
    
    /**
     * Sets array of all "SPKIData" element
     */
    public void setSPKIDataArray(SPKIDataType[] spkiDataArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(spkiDataArray, SPKIDATA$10);
        }
    }
    
    /**
     * Sets ith "SPKIData" element
     */
    public void setSPKIDataArray(int i, SPKIDataType spkiData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SPKIDataType target = null;
            target = (SPKIDataType)get_store().find_element_user(SPKIDATA$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(spkiData);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "SPKIData" element
     */
    public SPKIDataType insertNewSPKIData(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SPKIDataType target = null;
            target = (SPKIDataType)get_store().insert_element_user(SPKIDATA$10, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "SPKIData" element
     */
    public SPKIDataType addNewSPKIData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SPKIDataType target = null;
            target = (SPKIDataType)get_store().add_element_user(SPKIDATA$10);
            return target;
        }
    }
    
    /**
     * Removes the ith "SPKIData" element
     */
    public void removeSPKIData(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SPKIDATA$10, i);
        }
    }
    
    /**
     * Gets array of all "MgmtData" elements
     */
    public java.lang.String[] getMgmtDataArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(MGMTDATA$12, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "MgmtData" element
     */
    public java.lang.String getMgmtDataArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MGMTDATA$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "MgmtData" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetMgmtDataArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(MGMTDATA$12, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "MgmtData" element
     */
    public org.apache.xmlbeans.XmlString xgetMgmtDataArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MGMTDATA$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlString)target;
        }
    }
    
    /**
     * Returns number of "MgmtData" element
     */
    public int sizeOfMgmtDataArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(MGMTDATA$12);
        }
    }
    
    /**
     * Sets array of all "MgmtData" element
     */
    public void setMgmtDataArray(java.lang.String[] mgmtDataArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(mgmtDataArray, MGMTDATA$12);
        }
    }
    
    /**
     * Sets ith "MgmtData" element
     */
    public void setMgmtDataArray(int i, java.lang.String mgmtData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MGMTDATA$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(mgmtData);
        }
    }
    
    /**
     * Sets (as xml) array of all "MgmtData" element
     */
    public void xsetMgmtDataArray(org.apache.xmlbeans.XmlString[]mgmtDataArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(mgmtDataArray, MGMTDATA$12);
        }
    }
    
    /**
     * Sets (as xml) ith "MgmtData" element
     */
    public void xsetMgmtDataArray(int i, org.apache.xmlbeans.XmlString mgmtData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MGMTDATA$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(mgmtData);
        }
    }
    
    /**
     * Inserts the value as the ith "MgmtData" element
     */
    public void insertMgmtData(int i, java.lang.String mgmtData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(MGMTDATA$12, i);
            target.setStringValue(mgmtData);
        }
    }
    
    /**
     * Appends the value as the last "MgmtData" element
     */
    public void addMgmtData(java.lang.String mgmtData)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(MGMTDATA$12);
            target.setStringValue(mgmtData);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "MgmtData" element
     */
    public org.apache.xmlbeans.XmlString insertNewMgmtData(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(MGMTDATA$12, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "MgmtData" element
     */
    public org.apache.xmlbeans.XmlString addNewMgmtData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(MGMTDATA$12);
            return target;
        }
    }
    
    /**
     * Removes the ith "MgmtData" element
     */
    public void removeMgmtData(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(MGMTDATA$12, i);
        }
    }
    
    /**
     * Gets the "Id" attribute
     */
    public java.lang.String getId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$14);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "Id" attribute
     */
    public org.apache.xmlbeans.XmlID xgetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$14);
            return target;
        }
    }
    
    /**
     * True if has "Id" attribute
     */
    public boolean isSetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(ID$14) != null;
        }
    }
    
    /**
     * Sets the "Id" attribute
     */
    public void setId(java.lang.String id)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$14);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ID$14);
            }
            target.setStringValue(id);
        }
    }
    
    /**
     * Sets (as xml) the "Id" attribute
     */
    public void xsetId(org.apache.xmlbeans.XmlID id)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$14);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlID)get_store().add_attribute_user(ID$14);
            }
            target.set(id);
        }
    }
    
    /**
     * Unsets the "Id" attribute
     */
    public void unsetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(ID$14);
        }
    }
}
