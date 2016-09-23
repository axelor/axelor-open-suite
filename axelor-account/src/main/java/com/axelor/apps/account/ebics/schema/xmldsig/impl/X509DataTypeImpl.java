/*
 * XML Type:  X509DataType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: X509DataType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.X509DataType;
import com.axelor.apps.account.ebics.schema.xmldsig.X509IssuerSerialType;

/**
 * An XML X509DataType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class X509DataTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements X509DataType
{
    private static final long serialVersionUID = 1L;
    
    public X509DataTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName X509ISSUERSERIAL$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509IssuerSerial");
    private static final javax.xml.namespace.QName X509SKI$2 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509SKI");
    private static final javax.xml.namespace.QName X509SUBJECTNAME$4 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509SubjectName");
    private static final javax.xml.namespace.QName X509CERTIFICATE$6 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509Certificate");
    private static final javax.xml.namespace.QName X509CRL$8 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509CRL");
    
    
    /**
     * Gets array of all "X509IssuerSerial" elements
     */
    public X509IssuerSerialType[] getX509IssuerSerialArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(X509ISSUERSERIAL$0, targetList);
            X509IssuerSerialType[] result = new X509IssuerSerialType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "X509IssuerSerial" element
     */
    public X509IssuerSerialType getX509IssuerSerialArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509IssuerSerialType target = null;
            target = (X509IssuerSerialType)get_store().find_element_user(X509ISSUERSERIAL$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "X509IssuerSerial" element
     */
    public int sizeOfX509IssuerSerialArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(X509ISSUERSERIAL$0);
        }
    }
    
    /**
     * Sets array of all "X509IssuerSerial" element
     */
    public void setX509IssuerSerialArray(X509IssuerSerialType[] x509IssuerSerialArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(x509IssuerSerialArray, X509ISSUERSERIAL$0);
        }
    }
    
    /**
     * Sets ith "X509IssuerSerial" element
     */
    public void setX509IssuerSerialArray(int i, X509IssuerSerialType x509IssuerSerial)
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509IssuerSerialType target = null;
            target = (X509IssuerSerialType)get_store().find_element_user(X509ISSUERSERIAL$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(x509IssuerSerial);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "X509IssuerSerial" element
     */
    public X509IssuerSerialType insertNewX509IssuerSerial(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509IssuerSerialType target = null;
            target = (X509IssuerSerialType)get_store().insert_element_user(X509ISSUERSERIAL$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "X509IssuerSerial" element
     */
    public X509IssuerSerialType addNewX509IssuerSerial()
    {
        synchronized (monitor())
        {
            check_orphaned();
            X509IssuerSerialType target = null;
            target = (X509IssuerSerialType)get_store().add_element_user(X509ISSUERSERIAL$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "X509IssuerSerial" element
     */
    public void removeX509IssuerSerial(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(X509ISSUERSERIAL$0, i);
        }
    }
    
    /**
     * Gets array of all "X509SKI" elements
     */
    public byte[][] getX509SKIArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(X509SKI$2, targetList);
            byte[][] result = new byte[targetList.size()][];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getByteArrayValue();
            return result;
        }
    }
    
    /**
     * Gets ith "X509SKI" element
     */
    public byte[] getX509SKIArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509SKI$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "X509SKI" elements
     */
    public org.apache.xmlbeans.XmlBase64Binary[] xgetX509SKIArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(X509SKI$2, targetList);
            org.apache.xmlbeans.XmlBase64Binary[] result = new org.apache.xmlbeans.XmlBase64Binary[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "X509SKI" element
     */
    public org.apache.xmlbeans.XmlBase64Binary xgetX509SKIArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(X509SKI$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlBase64Binary)target;
        }
    }
    
    /**
     * Returns number of "X509SKI" element
     */
    public int sizeOfX509SKIArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(X509SKI$2);
        }
    }
    
    /**
     * Sets array of all "X509SKI" element
     */
    public void setX509SKIArray(byte[][] x509SKIArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(x509SKIArray, X509SKI$2);
        }
    }
    
    /**
     * Sets ith "X509SKI" element
     */
    public void setX509SKIArray(int i, byte[] x509SKI)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509SKI$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setByteArrayValue(x509SKI);
        }
    }
    
    /**
     * Sets (as xml) array of all "X509SKI" element
     */
    public void xsetX509SKIArray(org.apache.xmlbeans.XmlBase64Binary[]x509SKIArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(x509SKIArray, X509SKI$2);
        }
    }
    
    /**
     * Sets (as xml) ith "X509SKI" element
     */
    public void xsetX509SKIArray(int i, org.apache.xmlbeans.XmlBase64Binary x509SKI)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(X509SKI$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(x509SKI);
        }
    }
    
    /**
     * Inserts the value as the ith "X509SKI" element
     */
    public void insertX509SKI(int i, byte[] x509SKI)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(X509SKI$2, i);
            target.setByteArrayValue(x509SKI);
        }
    }
    
    /**
     * Appends the value as the last "X509SKI" element
     */
    public void addX509SKI(byte[] x509SKI)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(X509SKI$2);
            target.setByteArrayValue(x509SKI);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "X509SKI" element
     */
    public org.apache.xmlbeans.XmlBase64Binary insertNewX509SKI(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().insert_element_user(X509SKI$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "X509SKI" element
     */
    public org.apache.xmlbeans.XmlBase64Binary addNewX509SKI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().add_element_user(X509SKI$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "X509SKI" element
     */
    public void removeX509SKI(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(X509SKI$2, i);
        }
    }
    
    /**
     * Gets array of all "X509SubjectName" elements
     */
    public java.lang.String[] getX509SubjectNameArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(X509SUBJECTNAME$4, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "X509SubjectName" element
     */
    public java.lang.String getX509SubjectNameArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509SUBJECTNAME$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "X509SubjectName" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetX509SubjectNameArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(X509SUBJECTNAME$4, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "X509SubjectName" element
     */
    public org.apache.xmlbeans.XmlString xgetX509SubjectNameArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(X509SUBJECTNAME$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlString)target;
        }
    }
    
    /**
     * Returns number of "X509SubjectName" element
     */
    public int sizeOfX509SubjectNameArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(X509SUBJECTNAME$4);
        }
    }
    
    /**
     * Sets array of all "X509SubjectName" element
     */
    public void setX509SubjectNameArray(java.lang.String[] x509SubjectNameArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(x509SubjectNameArray, X509SUBJECTNAME$4);
        }
    }
    
    /**
     * Sets ith "X509SubjectName" element
     */
    public void setX509SubjectNameArray(int i, java.lang.String x509SubjectName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509SUBJECTNAME$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(x509SubjectName);
        }
    }
    
    /**
     * Sets (as xml) array of all "X509SubjectName" element
     */
    public void xsetX509SubjectNameArray(org.apache.xmlbeans.XmlString[]x509SubjectNameArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(x509SubjectNameArray, X509SUBJECTNAME$4);
        }
    }
    
    /**
     * Sets (as xml) ith "X509SubjectName" element
     */
    public void xsetX509SubjectNameArray(int i, org.apache.xmlbeans.XmlString x509SubjectName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(X509SUBJECTNAME$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(x509SubjectName);
        }
    }
    
    /**
     * Inserts the value as the ith "X509SubjectName" element
     */
    public void insertX509SubjectName(int i, java.lang.String x509SubjectName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(X509SUBJECTNAME$4, i);
            target.setStringValue(x509SubjectName);
        }
    }
    
    /**
     * Appends the value as the last "X509SubjectName" element
     */
    public void addX509SubjectName(java.lang.String x509SubjectName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(X509SUBJECTNAME$4);
            target.setStringValue(x509SubjectName);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "X509SubjectName" element
     */
    public org.apache.xmlbeans.XmlString insertNewX509SubjectName(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(X509SUBJECTNAME$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "X509SubjectName" element
     */
    public org.apache.xmlbeans.XmlString addNewX509SubjectName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(X509SUBJECTNAME$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "X509SubjectName" element
     */
    public void removeX509SubjectName(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(X509SUBJECTNAME$4, i);
        }
    }
    
    /**
     * Gets array of all "X509Certificate" elements
     */
    public byte[][] getX509CertificateArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(X509CERTIFICATE$6, targetList);
            byte[][] result = new byte[targetList.size()][];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getByteArrayValue();
            return result;
        }
    }
    
    /**
     * Gets ith "X509Certificate" element
     */
    public byte[] getX509CertificateArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509CERTIFICATE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "X509Certificate" elements
     */
    public org.apache.xmlbeans.XmlBase64Binary[] xgetX509CertificateArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(X509CERTIFICATE$6, targetList);
            org.apache.xmlbeans.XmlBase64Binary[] result = new org.apache.xmlbeans.XmlBase64Binary[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "X509Certificate" element
     */
    public org.apache.xmlbeans.XmlBase64Binary xgetX509CertificateArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(X509CERTIFICATE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlBase64Binary)target;
        }
    }
    
    /**
     * Returns number of "X509Certificate" element
     */
    public int sizeOfX509CertificateArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(X509CERTIFICATE$6);
        }
    }
    
    /**
     * Sets array of all "X509Certificate" element
     */
    public void setX509CertificateArray(byte[][] x509CertificateArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(x509CertificateArray, X509CERTIFICATE$6);
        }
    }
    
    /**
     * Sets ith "X509Certificate" element
     */
    public void setX509CertificateArray(int i, byte[] x509Certificate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509CERTIFICATE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setByteArrayValue(x509Certificate);
        }
    }
    
    /**
     * Sets (as xml) array of all "X509Certificate" element
     */
    public void xsetX509CertificateArray(org.apache.xmlbeans.XmlBase64Binary[]x509CertificateArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(x509CertificateArray, X509CERTIFICATE$6);
        }
    }
    
    /**
     * Sets (as xml) ith "X509Certificate" element
     */
    public void xsetX509CertificateArray(int i, org.apache.xmlbeans.XmlBase64Binary x509Certificate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(X509CERTIFICATE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(x509Certificate);
        }
    }
    
    /**
     * Inserts the value as the ith "X509Certificate" element
     */
    public void insertX509Certificate(int i, byte[] x509Certificate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(X509CERTIFICATE$6, i);
            target.setByteArrayValue(x509Certificate);
        }
    }
    
    /**
     * Appends the value as the last "X509Certificate" element
     */
    public void addX509Certificate(byte[] x509Certificate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(X509CERTIFICATE$6);
            target.setByteArrayValue(x509Certificate);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "X509Certificate" element
     */
    public org.apache.xmlbeans.XmlBase64Binary insertNewX509Certificate(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().insert_element_user(X509CERTIFICATE$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "X509Certificate" element
     */
    public org.apache.xmlbeans.XmlBase64Binary addNewX509Certificate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().add_element_user(X509CERTIFICATE$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "X509Certificate" element
     */
    public void removeX509Certificate(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(X509CERTIFICATE$6, i);
        }
    }
    
    /**
     * Gets array of all "X509CRL" elements
     */
    public byte[][] getX509CRLArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(X509CRL$8, targetList);
            byte[][] result = new byte[targetList.size()][];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getByteArrayValue();
            return result;
        }
    }
    
    /**
     * Gets ith "X509CRL" element
     */
    public byte[] getX509CRLArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509CRL$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "X509CRL" elements
     */
    public org.apache.xmlbeans.XmlBase64Binary[] xgetX509CRLArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(X509CRL$8, targetList);
            org.apache.xmlbeans.XmlBase64Binary[] result = new org.apache.xmlbeans.XmlBase64Binary[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "X509CRL" element
     */
    public org.apache.xmlbeans.XmlBase64Binary xgetX509CRLArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(X509CRL$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlBase64Binary)target;
        }
    }
    
    /**
     * Returns number of "X509CRL" element
     */
    public int sizeOfX509CRLArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(X509CRL$8);
        }
    }
    
    /**
     * Sets array of all "X509CRL" element
     */
    public void setX509CRLArray(byte[][] x509CRLArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(x509CRLArray, X509CRL$8);
        }
    }
    
    /**
     * Sets ith "X509CRL" element
     */
    public void setX509CRLArray(int i, byte[] x509CRL)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(X509CRL$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setByteArrayValue(x509CRL);
        }
    }
    
    /**
     * Sets (as xml) array of all "X509CRL" element
     */
    public void xsetX509CRLArray(org.apache.xmlbeans.XmlBase64Binary[]x509CRLArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(x509CRLArray, X509CRL$8);
        }
    }
    
    /**
     * Sets (as xml) ith "X509CRL" element
     */
    public void xsetX509CRLArray(int i, org.apache.xmlbeans.XmlBase64Binary x509CRL)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().find_element_user(X509CRL$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(x509CRL);
        }
    }
    
    /**
     * Inserts the value as the ith "X509CRL" element
     */
    public void insertX509CRL(int i, byte[] x509CRL)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(X509CRL$8, i);
            target.setByteArrayValue(x509CRL);
        }
    }
    
    /**
     * Appends the value as the last "X509CRL" element
     */
    public void addX509CRL(byte[] x509CRL)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(X509CRL$8);
            target.setByteArrayValue(x509CRL);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "X509CRL" element
     */
    public org.apache.xmlbeans.XmlBase64Binary insertNewX509CRL(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().insert_element_user(X509CRL$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "X509CRL" element
     */
    public org.apache.xmlbeans.XmlBase64Binary addNewX509CRL()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBase64Binary target = null;
            target = (org.apache.xmlbeans.XmlBase64Binary)get_store().add_element_user(X509CRL$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "X509CRL" element
     */
    public void removeX509CRL(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(X509CRL$8, i);
        }
    }
}
