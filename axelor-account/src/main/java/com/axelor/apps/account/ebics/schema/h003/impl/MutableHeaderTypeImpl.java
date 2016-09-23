/*
 * XML Type:  MutableHeaderType
 * Namespace: http://www.ebics.org/H003
 * Java type: MutableHeaderType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.TransactionPhaseType;

/**
 * An XML MutableHeaderType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class MutableHeaderTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements MutableHeaderType
{
    private static final long serialVersionUID = 1L;
    
    public MutableHeaderTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName TRANSACTIONPHASE$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "TransactionPhase");
    private static final javax.xml.namespace.QName SEGMENTNUMBER$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "SegmentNumber");
    
    
    /**
     * Gets the "TransactionPhase" element
     */
    public TransactionPhaseType.Enum getTransactionPhase()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TRANSACTIONPHASE$0, 0);
            if (target == null)
            {
                return null;
            }
            return (TransactionPhaseType.Enum)target.getEnumValue();
        }
    }
    
    /**
     * Gets (as xml) the "TransactionPhase" element
     */
    public TransactionPhaseType xgetTransactionPhase()
    {
        synchronized (monitor())
        {
            check_orphaned();
            TransactionPhaseType target = null;
            target = (TransactionPhaseType)get_store().find_element_user(TRANSACTIONPHASE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "TransactionPhase" element
     */
    public void setTransactionPhase(TransactionPhaseType.Enum transactionPhase)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(TRANSACTIONPHASE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(TRANSACTIONPHASE$0);
            }
            target.setEnumValue(transactionPhase);
        }
    }
    
    /**
     * Sets (as xml) the "TransactionPhase" element
     */
    public void xsetTransactionPhase(TransactionPhaseType transactionPhase)
    {
        synchronized (monitor())
        {
            check_orphaned();
            TransactionPhaseType target = null;
            target = (TransactionPhaseType)get_store().find_element_user(TRANSACTIONPHASE$0, 0);
            if (target == null)
            {
                target = (TransactionPhaseType)get_store().add_element_user(TRANSACTIONPHASE$0);
            }
            target.set(transactionPhase);
        }
    }
    
    /**
     * Gets the "SegmentNumber" element
     */
    public MutableHeaderType.SegmentNumber getSegmentNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            MutableHeaderType.SegmentNumber target = null;
            target = (MutableHeaderType.SegmentNumber)get_store().find_element_user(SEGMENTNUMBER$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Tests for nil "SegmentNumber" element
     */
    public boolean isNilSegmentNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            MutableHeaderType.SegmentNumber target = null;
            target = (MutableHeaderType.SegmentNumber)get_store().find_element_user(SEGMENTNUMBER$2, 0);
            if (target == null) return false;
            return target.isNil();
        }
    }
    
    /**
     * True if has "SegmentNumber" element
     */
    public boolean isSetSegmentNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SEGMENTNUMBER$2) != 0;
        }
    }
    
    /**
     * Sets the "SegmentNumber" element
     */
    public void setSegmentNumber(MutableHeaderType.SegmentNumber segmentNumber)
    {
        synchronized (monitor())
        {
            check_orphaned();
            MutableHeaderType.SegmentNumber target = null;
            target = (MutableHeaderType.SegmentNumber)get_store().find_element_user(SEGMENTNUMBER$2, 0);
            if (target == null)
            {
                target = (MutableHeaderType.SegmentNumber)get_store().add_element_user(SEGMENTNUMBER$2);
            }
            target.set(segmentNumber);
        }
    }
    
    /**
     * Appends and returns a new empty "SegmentNumber" element
     */
    public MutableHeaderType.SegmentNumber addNewSegmentNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            MutableHeaderType.SegmentNumber target = null;
            target = (MutableHeaderType.SegmentNumber)get_store().add_element_user(SEGMENTNUMBER$2);
            return target;
        }
    }
    
    /**
     * Nils the "SegmentNumber" element
     */
    public void setNilSegmentNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            MutableHeaderType.SegmentNumber target = null;
            target = (MutableHeaderType.SegmentNumber)get_store().find_element_user(SEGMENTNUMBER$2, 0);
            if (target == null)
            {
                target = (MutableHeaderType.SegmentNumber)get_store().add_element_user(SEGMENTNUMBER$2);
            }
            target.setNil();
        }
    }
    
    /**
     * Unsets the "SegmentNumber" element
     */
    public void unsetSegmentNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SEGMENTNUMBER$2, 0);
        }
    }
    /**
     * An XML SegmentNumber(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of MutableHeaderType$SegmentNumber.
     */
    public static class SegmentNumberImpl extends org.apache.xmlbeans.impl.values.JavaLongHolderEx implements MutableHeaderType.SegmentNumber
    {
        private static final long serialVersionUID = 1L;
        
        public SegmentNumberImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected SegmentNumberImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName LASTSEGMENT$0 = 
            new javax.xml.namespace.QName("", "lastSegment");
        
        
        /**
         * Gets the "lastSegment" attribute
         */
        public boolean getLastSegment()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LASTSEGMENT$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_default_attribute_value(LASTSEGMENT$0);
                }
                if (target == null)
                {
                    return false;
                }
                return target.getBooleanValue();
            }
        }
        
        /**
         * Gets (as xml) the "lastSegment" attribute
         */
        public org.apache.xmlbeans.XmlBoolean xgetLastSegment()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlBoolean target = null;
                target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(LASTSEGMENT$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlBoolean)get_default_attribute_value(LASTSEGMENT$0);
                }
                return target;
            }
        }
        
        /**
         * True if has "lastSegment" attribute
         */
        public boolean isSetLastSegment()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(LASTSEGMENT$0) != null;
            }
        }
        
        /**
         * Sets the "lastSegment" attribute
         */
        public void setLastSegment(boolean lastSegment)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LASTSEGMENT$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(LASTSEGMENT$0);
                }
                target.setBooleanValue(lastSegment);
            }
        }
        
        /**
         * Sets (as xml) the "lastSegment" attribute
         */
        public void xsetLastSegment(org.apache.xmlbeans.XmlBoolean lastSegment)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlBoolean target = null;
                target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(LASTSEGMENT$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlBoolean)get_store().add_attribute_user(LASTSEGMENT$0);
                }
                target.set(lastSegment);
            }
        }
        
        /**
         * Unsets the "lastSegment" attribute
         */
        public void unsetLastSegment()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(LASTSEGMENT$0);
            }
        }
    }
}
