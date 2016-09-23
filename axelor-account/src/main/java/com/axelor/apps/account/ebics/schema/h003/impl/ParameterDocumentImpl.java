/*
 * An XML document type.
 * Localname: Parameter
 * Namespace: http://www.ebics.org/H003
 * Java type: ParameterDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.ParameterDocument;

/**
 * A document containing one Parameter(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class ParameterDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements ParameterDocument
{
    private static final long serialVersionUID = 1L;
    
    public ParameterDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PARAMETER$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "Parameter");
    
    
    /**
     * Gets the "Parameter" element
     */
    public ParameterDocument.Parameter getParameter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ParameterDocument.Parameter target = null;
            target = (ParameterDocument.Parameter)get_store().find_element_user(PARAMETER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "Parameter" element
     */
    public void setParameter(ParameterDocument.Parameter parameter)
    {
        synchronized (monitor())
        {
            check_orphaned();
            ParameterDocument.Parameter target = null;
            target = (ParameterDocument.Parameter)get_store().find_element_user(PARAMETER$0, 0);
            if (target == null)
            {
                target = (ParameterDocument.Parameter)get_store().add_element_user(PARAMETER$0);
            }
            target.set(parameter);
        }
    }
    
    /**
     * Appends and returns a new empty "Parameter" element
     */
    public ParameterDocument.Parameter addNewParameter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            ParameterDocument.Parameter target = null;
            target = (ParameterDocument.Parameter)get_store().add_element_user(PARAMETER$0);
            return target;
        }
    }
    /**
     * An XML Parameter(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public static class ParameterImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements ParameterDocument.Parameter
    {
        private static final long serialVersionUID = 1L;
        
        public ParameterImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName NAME$0 = 
            new javax.xml.namespace.QName("http://www.ebics.org/H003", "Name");
        private static final javax.xml.namespace.QName VALUE$2 = 
            new javax.xml.namespace.QName("http://www.ebics.org/H003", "Value");
        
        
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
        public org.apache.xmlbeans.XmlToken xgetName()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlToken target = null;
                target = (org.apache.xmlbeans.XmlToken)get_store().find_element_user(NAME$0, 0);
                return target;
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
        public void xsetName(org.apache.xmlbeans.XmlToken name)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlToken target = null;
                target = (org.apache.xmlbeans.XmlToken)get_store().find_element_user(NAME$0, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlToken)get_store().add_element_user(NAME$0);
                }
                target.set(name);
            }
        }
        
        /**
         * Gets the "Value" element
         */
        public ParameterDocument.Parameter.Value getValue()
        {
            synchronized (monitor())
            {
                check_orphaned();
                ParameterDocument.Parameter.Value target = null;
                target = (ParameterDocument.Parameter.Value)get_store().find_element_user(VALUE$2, 0);
                if (target == null)
                {
                    return null;
                }
                return target;
            }
        }
        
        /**
         * Sets the "Value" element
         */
        public void setValue(ParameterDocument.Parameter.Value value)
        {
            synchronized (monitor())
            {
                check_orphaned();
                ParameterDocument.Parameter.Value target = null;
                target = (ParameterDocument.Parameter.Value)get_store().find_element_user(VALUE$2, 0);
                if (target == null)
                {
                    target = (ParameterDocument.Parameter.Value)get_store().add_element_user(VALUE$2);
                }
                target.set(value);
            }
        }
        
        /**
         * Appends and returns a new empty "Value" element
         */
        public ParameterDocument.Parameter.Value addNewValue()
        {
            synchronized (monitor())
            {
                check_orphaned();
                ParameterDocument.Parameter.Value target = null;
                target = (ParameterDocument.Parameter.Value)get_store().add_element_user(VALUE$2);
                return target;
            }
        }
        /**
         * An XML Value(@http://www.ebics.org/H003).
         *
         * This is an atomic type that is a restriction of ParameterDocument$Parameter$Value.
         */
        public static class ValueImpl extends org.apache.xmlbeans.impl.values.XmlAnySimpleTypeImpl implements ParameterDocument.Parameter.Value
        {
            private static final long serialVersionUID = 1L;
            
            public ValueImpl(org.apache.xmlbeans.SchemaType sType)
            {
                super(sType, true);
            }
            
            protected ValueImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
            {
                super(sType, b);
            }
            
            private static final javax.xml.namespace.QName TYPE$0 = 
                new javax.xml.namespace.QName("", "Type");
            
            
            /**
             * Gets the "Type" attribute
             */
            public java.lang.String getType()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.SimpleValue target = null;
                    target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$0);
                    if (target == null)
                    {
                      target = (org.apache.xmlbeans.SimpleValue)get_default_attribute_value(TYPE$0);
                    }
                    if (target == null)
                    {
                      return null;
                    }
                    return target.getStringValue();
                }
            }
            
            /**
             * Gets (as xml) the "Type" attribute
             */
            public org.apache.xmlbeans.XmlNCName xgetType()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.XmlNCName target = null;
                    target = (org.apache.xmlbeans.XmlNCName)get_store().find_attribute_user(TYPE$0);
                    if (target == null)
                    {
                      target = (org.apache.xmlbeans.XmlNCName)get_default_attribute_value(TYPE$0);
                    }
                    return target;
                }
            }
            
            /**
             * True if has "Type" attribute
             */
            public boolean isSetType()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    return get_store().find_attribute_user(TYPE$0) != null;
                }
            }
            
            /**
             * Sets the "Type" attribute
             */
            public void setType(java.lang.String type)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.SimpleValue target = null;
                    target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$0);
                    if (target == null)
                    {
                      target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TYPE$0);
                    }
                    target.setStringValue(type);
                }
            }
            
            /**
             * Sets (as xml) the "Type" attribute
             */
            public void xsetType(org.apache.xmlbeans.XmlNCName type)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.XmlNCName target = null;
                    target = (org.apache.xmlbeans.XmlNCName)get_store().find_attribute_user(TYPE$0);
                    if (target == null)
                    {
                      target = (org.apache.xmlbeans.XmlNCName)get_store().add_attribute_user(TYPE$0);
                    }
                    target.set(type);
                }
            }
            
            /**
             * Unsets the "Type" attribute
             */
            public void unsetType()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    get_store().remove_attribute(TYPE$0);
                }
            }
        }
    }
}
