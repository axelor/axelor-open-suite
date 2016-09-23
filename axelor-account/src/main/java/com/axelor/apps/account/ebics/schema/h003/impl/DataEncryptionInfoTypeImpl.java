/*
 * XML Type:  DataEncryptionInfoType
 * Namespace: http://www.ebics.org/H003
 * Java type: DataEncryptionInfoType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.DataEncryptionInfoType;
import com.axelor.apps.account.ebics.schema.h003.EncryptionVersionType;
import com.axelor.apps.account.ebics.schema.h003.SymmetricKeyType;

/**
 * An XML DataEncryptionInfoType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class DataEncryptionInfoTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements DataEncryptionInfoType
{
    private static final long serialVersionUID = 1L;
    
    public DataEncryptionInfoTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENCRYPTIONPUBKEYDIGEST$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "EncryptionPubKeyDigest");
    private static final javax.xml.namespace.QName TRANSACTIONKEY$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "TransactionKey");
    
    
    /**
     * Gets the "EncryptionPubKeyDigest" element
     */
    public DataEncryptionInfoType.EncryptionPubKeyDigest getEncryptionPubKeyDigest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataEncryptionInfoType.EncryptionPubKeyDigest target = null;
            target = (DataEncryptionInfoType.EncryptionPubKeyDigest)get_store().find_element_user(ENCRYPTIONPUBKEYDIGEST$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "EncryptionPubKeyDigest" element
     */
    public void setEncryptionPubKeyDigest(DataEncryptionInfoType.EncryptionPubKeyDigest encryptionPubKeyDigest)
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataEncryptionInfoType.EncryptionPubKeyDigest target = null;
            target = (DataEncryptionInfoType.EncryptionPubKeyDigest)get_store().find_element_user(ENCRYPTIONPUBKEYDIGEST$0, 0);
            if (target == null)
            {
                target = (DataEncryptionInfoType.EncryptionPubKeyDigest)get_store().add_element_user(ENCRYPTIONPUBKEYDIGEST$0);
            }
            target.set(encryptionPubKeyDigest);
        }
    }
    
    /**
     * Appends and returns a new empty "EncryptionPubKeyDigest" element
     */
    public DataEncryptionInfoType.EncryptionPubKeyDigest addNewEncryptionPubKeyDigest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            DataEncryptionInfoType.EncryptionPubKeyDigest target = null;
            target = (DataEncryptionInfoType.EncryptionPubKeyDigest)get_store().add_element_user(ENCRYPTIONPUBKEYDIGEST$0);
            return target;
        }
    }
    
    /**
     * Gets the "TransactionKey" element
     */
    public byte[] getTransactionKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TRANSACTIONKEY$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "TransactionKey" element
     */
    public SymmetricKeyType xgetTransactionKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            SymmetricKeyType target = null;
            target = (SymmetricKeyType)get_store().find_element_user(TRANSACTIONKEY$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "TransactionKey" element
     */
    public void setTransactionKey(byte[] transactionKey)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TRANSACTIONKEY$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(TRANSACTIONKEY$2);
            }
            target.setByteArrayValue(transactionKey);
        }
    }
    
    /**
     * Sets (as xml) the "TransactionKey" element
     */
    public void xsetTransactionKey(SymmetricKeyType transactionKey)
    {
        synchronized (monitor())
        {
            check_orphaned();
            SymmetricKeyType target = null;
            target = (SymmetricKeyType)get_store().find_element_user(TRANSACTIONKEY$2, 0);
            if (target == null)
            {
                target = (SymmetricKeyType)get_store().add_element_user(TRANSACTIONKEY$2);
            }
            target.set(transactionKey);
        }
    }
    /**
     * An XML EncryptionPubKeyDigest(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of DataEncryptionInfoType$EncryptionPubKeyDigest.
     */
    public static class EncryptionPubKeyDigestImpl extends org.apache.xmlbeans.impl.values.JavaBase64HolderEx implements DataEncryptionInfoType.EncryptionPubKeyDigest
    {
        private static final long serialVersionUID = 1L;
        
        public EncryptionPubKeyDigestImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected EncryptionPubKeyDigestImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName ALGORITHM$0 = 
            new javax.xml.namespace.QName("", "Algorithm");
        private static final javax.xml.namespace.QName VERSION$2 = 
            new javax.xml.namespace.QName("", "Version");
        
        
        /**
         * Gets the "Algorithm" attribute
         */
        public java.lang.String getAlgorithm()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ALGORITHM$0);
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
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ALGORITHM$0);
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
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ALGORITHM$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ALGORITHM$0);
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
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(ALGORITHM$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(ALGORITHM$0);
                }
                target.set(algorithm);
            }
        }
        
        /**
         * Gets the "Version" attribute
         */
        public java.lang.String getVersion()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$2);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "Version" attribute
         */
        public EncryptionVersionType xgetVersion()
        {
            synchronized (monitor())
            {
                check_orphaned();
                EncryptionVersionType target = null;
                target = (EncryptionVersionType)get_store().find_attribute_user(VERSION$2);
                return target;
            }
        }
        
        /**
         * Sets the "Version" attribute
         */
        public void setVersion(java.lang.String version)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VERSION$2);
                }
                target.setStringValue(version);
            }
        }
        
        /**
         * Sets (as xml) the "Version" attribute
         */
        public void xsetVersion(EncryptionVersionType version)
        {
            synchronized (monitor())
            {
                check_orphaned();
                EncryptionVersionType target = null;
                target = (EncryptionVersionType)get_store().find_attribute_user(VERSION$2);
                if (target == null)
                {
                    target = (EncryptionVersionType)get_store().add_attribute_user(VERSION$2);
                }
                target.set(version);
            }
        }
    }
}
