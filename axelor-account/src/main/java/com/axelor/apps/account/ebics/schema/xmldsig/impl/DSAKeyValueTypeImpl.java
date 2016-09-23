/*
 * XML Type:  DSAKeyValueType
 * Namespace: http://www.w3.org/2000/09/xmldsig#
 * Java type: DSAKeyValueType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.xmldsig.impl;

import com.axelor.apps.account.ebics.schema.xmldsig.CryptoBinary;
import com.axelor.apps.account.ebics.schema.xmldsig.DSAKeyValueType;

/**
 * An XML DSAKeyValueType(@http://www.w3.org/2000/09/xmldsig#).
 *
 * This is a complex type.
 */
public class DSAKeyValueTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements DSAKeyValueType
{
    private static final long serialVersionUID = 1L;
    
    public DSAKeyValueTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName P$0 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "P");
    private static final javax.xml.namespace.QName Q$2 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Q");
    private static final javax.xml.namespace.QName G$4 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "G");
    private static final javax.xml.namespace.QName Y$6 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Y");
    private static final javax.xml.namespace.QName J$8 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "J");
    private static final javax.xml.namespace.QName SEED$10 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Seed");
    private static final javax.xml.namespace.QName PGENCOUNTER$12 = 
        new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "PgenCounter");
    
    
    /**
     * Gets the "P" element
     */
    public byte[] getP()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(P$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "P" element
     */
    public CryptoBinary xgetP()
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(P$0, 0);
            return target;
        }
    }
    
    /**
     * True if has "P" element
     */
    public boolean isSetP()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(P$0) != 0;
        }
    }
    
    /**
     * Sets the "P" element
     */
    public void setP(byte[] p)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(P$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(P$0);
            }
            target.setByteArrayValue(p);
        }
    }
    
    /**
     * Sets (as xml) the "P" element
     */
    public void xsetP(CryptoBinary p)
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(P$0, 0);
            if (target == null)
            {
                target = (CryptoBinary)get_store().add_element_user(P$0);
            }
            target.set(p);
        }
    }
    
    /**
     * Unsets the "P" element
     */
    public void unsetP()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(P$0, 0);
        }
    }
    
    /**
     * Gets the "Q" element
     */
    public byte[] getQ()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(Q$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "Q" element
     */
    public CryptoBinary xgetQ()
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(Q$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "Q" element
     */
    public boolean isSetQ()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(Q$2) != 0;
        }
    }
    
    /**
     * Sets the "Q" element
     */
    public void setQ(byte[] q)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(Q$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(Q$2);
            }
            target.setByteArrayValue(q);
        }
    }
    
    /**
     * Sets (as xml) the "Q" element
     */
    public void xsetQ(CryptoBinary q)
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(Q$2, 0);
            if (target == null)
            {
                target = (CryptoBinary)get_store().add_element_user(Q$2);
            }
            target.set(q);
        }
    }
    
    /**
     * Unsets the "Q" element
     */
    public void unsetQ()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(Q$2, 0);
        }
    }
    
    /**
     * Gets the "G" element
     */
    public byte[] getG()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(G$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "G" element
     */
    public CryptoBinary xgetG()
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(G$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "G" element
     */
    public boolean isSetG()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(G$4) != 0;
        }
    }
    
    /**
     * Sets the "G" element
     */
    public void setG(byte[] g)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(G$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(G$4);
            }
            target.setByteArrayValue(g);
        }
    }
    
    /**
     * Sets (as xml) the "G" element
     */
    public void xsetG(CryptoBinary g)
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(G$4, 0);
            if (target == null)
            {
                target = (CryptoBinary)get_store().add_element_user(G$4);
            }
            target.set(g);
        }
    }
    
    /**
     * Unsets the "G" element
     */
    public void unsetG()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(G$4, 0);
        }
    }
    
    /**
     * Gets the "Y" element
     */
    public byte[] getY()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(Y$6, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "Y" element
     */
    public CryptoBinary xgetY()
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(Y$6, 0);
            return target;
        }
    }
    
    /**
     * Sets the "Y" element
     */
    public void setY(byte[] y)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(Y$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(Y$6);
            }
            target.setByteArrayValue(y);
        }
    }
    
    /**
     * Sets (as xml) the "Y" element
     */
    public void xsetY(CryptoBinary y)
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(Y$6, 0);
            if (target == null)
            {
                target = (CryptoBinary)get_store().add_element_user(Y$6);
            }
            target.set(y);
        }
    }
    
    /**
     * Gets the "J" element
     */
    public byte[] getJ()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(J$8, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "J" element
     */
    public CryptoBinary xgetJ()
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(J$8, 0);
            return target;
        }
    }
    
    /**
     * True if has "J" element
     */
    public boolean isSetJ()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(J$8) != 0;
        }
    }
    
    /**
     * Sets the "J" element
     */
    public void setJ(byte[] j)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(J$8, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(J$8);
            }
            target.setByteArrayValue(j);
        }
    }
    
    /**
     * Sets (as xml) the "J" element
     */
    public void xsetJ(CryptoBinary j)
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(J$8, 0);
            if (target == null)
            {
                target = (CryptoBinary)get_store().add_element_user(J$8);
            }
            target.set(j);
        }
    }
    
    /**
     * Unsets the "J" element
     */
    public void unsetJ()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(J$8, 0);
        }
    }
    
    /**
     * Gets the "Seed" element
     */
    public byte[] getSeed()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SEED$10, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "Seed" element
     */
    public CryptoBinary xgetSeed()
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(SEED$10, 0);
            return target;
        }
    }
    
    /**
     * True if has "Seed" element
     */
    public boolean isSetSeed()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SEED$10) != 0;
        }
    }
    
    /**
     * Sets the "Seed" element
     */
    public void setSeed(byte[] seed)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SEED$10, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SEED$10);
            }
            target.setByteArrayValue(seed);
        }
    }
    
    /**
     * Sets (as xml) the "Seed" element
     */
    public void xsetSeed(CryptoBinary seed)
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(SEED$10, 0);
            if (target == null)
            {
                target = (CryptoBinary)get_store().add_element_user(SEED$10);
            }
            target.set(seed);
        }
    }
    
    /**
     * Unsets the "Seed" element
     */
    public void unsetSeed()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SEED$10, 0);
        }
    }
    
    /**
     * Gets the "PgenCounter" element
     */
    public byte[] getPgenCounter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PGENCOUNTER$12, 0);
            if (target == null)
            {
                return null;
            }
            return target.getByteArrayValue();
        }
    }
    
    /**
     * Gets (as xml) the "PgenCounter" element
     */
    public CryptoBinary xgetPgenCounter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(PGENCOUNTER$12, 0);
            return target;
        }
    }
    
    /**
     * True if has "PgenCounter" element
     */
    public boolean isSetPgenCounter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PGENCOUNTER$12) != 0;
        }
    }
    
    /**
     * Sets the "PgenCounter" element
     */
    public void setPgenCounter(byte[] pgenCounter)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PGENCOUNTER$12, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PGENCOUNTER$12);
            }
            target.setByteArrayValue(pgenCounter);
        }
    }
    
    /**
     * Sets (as xml) the "PgenCounter" element
     */
    public void xsetPgenCounter(CryptoBinary pgenCounter)
    {
        synchronized (monitor())
        {
            check_orphaned();
            CryptoBinary target = null;
            target = (CryptoBinary)get_store().find_element_user(PGENCOUNTER$12, 0);
            if (target == null)
            {
                target = (CryptoBinary)get_store().add_element_user(PGENCOUNTER$12);
            }
            target.set(pgenCounter);
        }
    }
    
    /**
     * Unsets the "PgenCounter" element
     */
    public void unsetPgenCounter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PGENCOUNTER$12, 0);
        }
    }
}
