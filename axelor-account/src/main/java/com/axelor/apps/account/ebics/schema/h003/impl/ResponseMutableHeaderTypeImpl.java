/*
 * XML Type:  ResponseMutableHeaderType
 * Namespace: http://www.ebics.org/H003
 * Java type: ResponseMutableHeaderType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.ReportTextType;
import com.axelor.apps.account.ebics.schema.h003.ResponseMutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.ReturnCodeType;
import com.axelor.apps.account.ebics.schema.h003.TransactionPhaseType;

/**
 * An XML ResponseMutableHeaderType(@http://www.ebics.org/H003).
 *
 * This is a complex type.
 */
public class ResponseMutableHeaderTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements ResponseMutableHeaderType
{
    private static final long serialVersionUID = 1L;
    
    public ResponseMutableHeaderTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName TRANSACTIONPHASE$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "TransactionPhase");
    private static final javax.xml.namespace.QName SEGMENTNUMBER$2 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "SegmentNumber");
    private static final javax.xml.namespace.QName RETURNCODE$4 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "ReturnCode");
    private static final javax.xml.namespace.QName REPORTTEXT$6 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "ReportText");
    
    
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
    public ResponseMutableHeaderType.SegmentNumber getSegmentNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ResponseMutableHeaderType.SegmentNumber target = null;
            target = (ResponseMutableHeaderType.SegmentNumber)get_store().find_element_user(SEGMENTNUMBER$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
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
    public void setSegmentNumber(ResponseMutableHeaderType.SegmentNumber segmentNumber)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ResponseMutableHeaderType.SegmentNumber target = null;
            target = (ResponseMutableHeaderType.SegmentNumber)get_store().find_element_user(SEGMENTNUMBER$2, 0);
            if (target == null)
            {
                target = (ResponseMutableHeaderType.SegmentNumber)get_store().add_element_user(SEGMENTNUMBER$2);
            }
            target.set(segmentNumber);
        }
    }
    
    /**
     * Appends and returns a new empty "SegmentNumber" element
     */
    public ResponseMutableHeaderType.SegmentNumber addNewSegmentNumber()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ResponseMutableHeaderType.SegmentNumber target = null;
            target = (ResponseMutableHeaderType.SegmentNumber)get_store().add_element_user(SEGMENTNUMBER$2);
            return target;
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
     * Gets the "ReturnCode" element
     */
    public java.lang.String getReturnCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RETURNCODE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "ReturnCode" element
     */
    public ReturnCodeType xgetReturnCode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReturnCodeType target = null;
            target = (ReturnCodeType)get_store().find_element_user(RETURNCODE$4, 0);
            return target;
        }
    }
    
    /**
     * Sets the "ReturnCode" element
     */
    public void setReturnCode(java.lang.String returnCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RETURNCODE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RETURNCODE$4);
            }
            target.setStringValue(returnCode);
        }
    }
    
    /**
     * Sets (as xml) the "ReturnCode" element
     */
    public void xsetReturnCode(ReturnCodeType returnCode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReturnCodeType target = null;
            target = (ReturnCodeType)get_store().find_element_user(RETURNCODE$4, 0);
            if (target == null)
            {
                target = (ReturnCodeType)get_store().add_element_user(RETURNCODE$4);
            }
            target.set(returnCode);
        }
    }
    
    /**
     * Gets the "ReportText" element
     */
    public java.lang.String getReportText()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(REPORTTEXT$6, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "ReportText" element
     */
    public ReportTextType xgetReportText()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReportTextType target = null;
            target = (ReportTextType)get_store().find_element_user(REPORTTEXT$6, 0);
            return target;
        }
    }
    
    /**
     * Sets the "ReportText" element
     */
    public void setReportText(java.lang.String reportText)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(REPORTTEXT$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(REPORTTEXT$6);
            }
            target.setStringValue(reportText);
        }
    }
    
    /**
     * Sets (as xml) the "ReportText" element
     */
    public void xsetReportText(ReportTextType reportText)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ReportTextType target = null;
            target = (ReportTextType)get_store().find_element_user(REPORTTEXT$6, 0);
            if (target == null)
            {
                target = (ReportTextType)get_store().add_element_user(REPORTTEXT$6);
            }
            target.set(reportText);
        }
    }
    /**
     * An XML SegmentNumber(@http://www.ebics.org/H003).
     *
     * This is an atomic type that is a restriction of ResponseMutableHeaderType$SegmentNumber.
     */
    public static class SegmentNumberImpl extends org.apache.xmlbeans.impl.values.JavaLongHolderEx implements ResponseMutableHeaderType.SegmentNumber
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
