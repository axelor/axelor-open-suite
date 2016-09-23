/*
 * An XML document type.
 * Localname: ebicsNoPubKeyDigestsRequest
 * Namespace: http://www.ebics.org/H003
 * Java type: EbicsNoPubKeyDigestsRequestDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument;
import com.axelor.apps.account.ebics.schema.h003.EmptyMutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.NoPubKeyDigestsRequestStaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.ProtocolRevisionType;
import com.axelor.apps.account.ebics.schema.h003.ProtocolVersionType;
import com.axelor.apps.account.ebics.schema.h003.SignatureType;

/**
 * A document containing one ebicsNoPubKeyDigestsRequest(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class EbicsNoPubKeyDigestsRequestDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements EbicsNoPubKeyDigestsRequestDocument
{
    private static final long serialVersionUID = 1L;
    
    public EbicsNoPubKeyDigestsRequestDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EBICSNOPUBKEYDIGESTSREQUEST$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "ebicsNoPubKeyDigestsRequest");
    
    
    /**
     * Gets the "ebicsNoPubKeyDigestsRequest" element
     */
    public EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest getEbicsNoPubKeyDigestsRequest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest target = null;
            target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest)get_store().find_element_user(EBICSNOPUBKEYDIGESTSREQUEST$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "ebicsNoPubKeyDigestsRequest" element
     */
    public void setEbicsNoPubKeyDigestsRequest(EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest ebicsNoPubKeyDigestsRequest)
    {
        synchronized (monitor())
        {
            check_orphaned();
            EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest target = null;
            target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest)get_store().find_element_user(EBICSNOPUBKEYDIGESTSREQUEST$0, 0);
            if (target == null)
            {
                target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest)get_store().add_element_user(EBICSNOPUBKEYDIGESTSREQUEST$0);
            }
            target.set(ebicsNoPubKeyDigestsRequest);
        }
    }
    
    /**
     * Appends and returns a new empty "ebicsNoPubKeyDigestsRequest" element
     */
    public EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest addNewEbicsNoPubKeyDigestsRequest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest target = null;
            target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest)get_store().add_element_user(EBICSNOPUBKEYDIGESTSREQUEST$0);
            return target;
        }
    }
    /**
     * An XML ebicsNoPubKeyDigestsRequest(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public static class EbicsNoPubKeyDigestsRequestImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest
    {
        private static final long serialVersionUID = 1L;
        
        public EbicsNoPubKeyDigestsRequestImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName HEADER$0 = 
            new javax.xml.namespace.QName("http://www.ebics.org/H003", "header");
        private static final javax.xml.namespace.QName AUTHSIGNATURE$2 = 
            new javax.xml.namespace.QName("http://www.ebics.org/H003", "AuthSignature");
        private static final javax.xml.namespace.QName BODY$4 = 
            new javax.xml.namespace.QName("http://www.ebics.org/H003", "body");
        private static final javax.xml.namespace.QName VERSION$6 = 
            new javax.xml.namespace.QName("", "Version");
        private static final javax.xml.namespace.QName REVISION$8 = 
            new javax.xml.namespace.QName("", "Revision");
        
        
        /**
         * Gets the "header" element
         */
        public EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header getHeader()
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header target = null;
                target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header)get_store().find_element_user(HEADER$0, 0);
                if (target == null)
                {
                    return null;
                }
                return target;
            }
        }
        
        /**
         * Sets the "header" element
         */
        public void setHeader(EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header header)
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header target = null;
                target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header)get_store().find_element_user(HEADER$0, 0);
                if (target == null)
                {
                    target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header)get_store().add_element_user(HEADER$0);
                }
                target.set(header);
            }
        }
        
        /**
         * Appends and returns a new empty "header" element
         */
        public EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header addNewHeader()
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header target = null;
                target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header)get_store().add_element_user(HEADER$0);
                return target;
            }
        }
        
        /**
         * Gets the "AuthSignature" element
         */
        public SignatureType getAuthSignature()
        {
            synchronized (monitor())
            {
                check_orphaned();
                SignatureType target = null;
                target = (SignatureType)get_store().find_element_user(AUTHSIGNATURE$2, 0);
                if (target == null)
                {
                    return null;
                }
                return target;
            }
        }
        
        /**
         * Sets the "AuthSignature" element
         */
        public void setAuthSignature(SignatureType authSignature)
        {
            synchronized (monitor())
            {
                check_orphaned();
                SignatureType target = null;
                target = (SignatureType)get_store().find_element_user(AUTHSIGNATURE$2, 0);
                if (target == null)
                {
                    target = (SignatureType)get_store().add_element_user(AUTHSIGNATURE$2);
                }
                target.set(authSignature);
            }
        }
        
        /**
         * Appends and returns a new empty "AuthSignature" element
         */
        public SignatureType addNewAuthSignature()
        {
            synchronized (monitor())
            {
                check_orphaned();
                SignatureType target = null;
                target = (SignatureType)get_store().add_element_user(AUTHSIGNATURE$2);
                return target;
            }
        }
        
        /**
         * Gets the "body" element
         */
        public EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body getBody()
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body target = null;
                target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body)get_store().find_element_user(BODY$4, 0);
                if (target == null)
                {
                    return null;
                }
                return target;
            }
        }
        
        /**
         * Sets the "body" element
         */
        public void setBody(EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body body)
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body target = null;
                target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body)get_store().find_element_user(BODY$4, 0);
                if (target == null)
                {
                    target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body)get_store().add_element_user(BODY$4);
                }
                target.set(body);
            }
        }
        
        /**
         * Appends and returns a new empty "body" element
         */
        public EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body addNewBody()
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body target = null;
                target = (EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body)get_store().add_element_user(BODY$4);
                return target;
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
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$6);
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
        public ProtocolVersionType xgetVersion()
        {
            synchronized (monitor())
            {
                check_orphaned();
                ProtocolVersionType target = null;
                target = (ProtocolVersionType)get_store().find_attribute_user(VERSION$6);
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
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$6);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VERSION$6);
                }
                target.setStringValue(version);
            }
        }
        
        /**
         * Sets (as xml) the "Version" attribute
         */
        public void xsetVersion(ProtocolVersionType version)
        {
            synchronized (monitor())
            {
                check_orphaned();
                ProtocolVersionType target = null;
                target = (ProtocolVersionType)get_store().find_attribute_user(VERSION$6);
                if (target == null)
                {
                    target = (ProtocolVersionType)get_store().add_attribute_user(VERSION$6);
                }
                target.set(version);
            }
        }
        
        /**
         * Gets the "Revision" attribute
         */
        public int getRevision()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(REVISION$8);
                if (target == null)
                {
                    return 0;
                }
                return target.getIntValue();
            }
        }
        
        /**
         * Gets (as xml) the "Revision" attribute
         */
        public ProtocolRevisionType xgetRevision()
        {
            synchronized (monitor())
            {
                check_orphaned();
                ProtocolRevisionType target = null;
                target = (ProtocolRevisionType)get_store().find_attribute_user(REVISION$8);
                return target;
            }
        }
        
        /**
         * True if has "Revision" attribute
         */
        public boolean isSetRevision()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(REVISION$8) != null;
            }
        }
        
        /**
         * Sets the "Revision" attribute
         */
        public void setRevision(int revision)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(REVISION$8);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(REVISION$8);
                }
                target.setIntValue(revision);
            }
        }
        
        /**
         * Sets (as xml) the "Revision" attribute
         */
        public void xsetRevision(ProtocolRevisionType revision)
        {
            synchronized (monitor())
            {
                check_orphaned();
                ProtocolRevisionType target = null;
                target = (ProtocolRevisionType)get_store().find_attribute_user(REVISION$8);
                if (target == null)
                {
                    target = (ProtocolRevisionType)get_store().add_attribute_user(REVISION$8);
                }
                target.set(revision);
            }
        }
        
        /**
         * Unsets the "Revision" attribute
         */
        public void unsetRevision()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(REVISION$8);
            }
        }
        /**
         * An XML header(@http://www.ebics.org/H003).
         *
         * This is a complex type.
         */
        public static class HeaderImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header
        {
            private static final long serialVersionUID = 1L;
            
            public HeaderImpl(org.apache.xmlbeans.SchemaType sType)
            {
                super(sType);
            }
            
            private static final javax.xml.namespace.QName STATIC$0 = 
                new javax.xml.namespace.QName("http://www.ebics.org/H003", "static");
            private static final javax.xml.namespace.QName MUTABLE$2 = 
                new javax.xml.namespace.QName("http://www.ebics.org/H003", "mutable");
            private static final javax.xml.namespace.QName AUTHENTICATE$4 = 
                new javax.xml.namespace.QName("", "authenticate");
            
            
            /**
             * Gets the "static" element
             */
            public NoPubKeyDigestsRequestStaticHeaderType getStatic()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    NoPubKeyDigestsRequestStaticHeaderType target = null;
                    target = (NoPubKeyDigestsRequestStaticHeaderType)get_store().find_element_user(STATIC$0, 0);
                    if (target == null)
                    {
                      return null;
                    }
                    return target;
                }
            }
            
            /**
             * Sets the "static" element
             */
            public void setStatic(NoPubKeyDigestsRequestStaticHeaderType xstatic)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    NoPubKeyDigestsRequestStaticHeaderType target = null;
                    target = (NoPubKeyDigestsRequestStaticHeaderType)get_store().find_element_user(STATIC$0, 0);
                    if (target == null)
                    {
                      target = (NoPubKeyDigestsRequestStaticHeaderType)get_store().add_element_user(STATIC$0);
                    }
                    target.set(xstatic);
                }
            }
            
            /**
             * Appends and returns a new empty "static" element
             */
            public NoPubKeyDigestsRequestStaticHeaderType addNewStatic()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    NoPubKeyDigestsRequestStaticHeaderType target = null;
                    target = (NoPubKeyDigestsRequestStaticHeaderType)get_store().add_element_user(STATIC$0);
                    return target;
                }
            }
            
            /**
             * Gets the "mutable" element
             */
            public EmptyMutableHeaderType getMutable()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    EmptyMutableHeaderType target = null;
                    target = (EmptyMutableHeaderType)get_store().find_element_user(MUTABLE$2, 0);
                    if (target == null)
                    {
                      return null;
                    }
                    return target;
                }
            }
            
            /**
             * Sets the "mutable" element
             */
            public void setMutable(EmptyMutableHeaderType mutable)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    EmptyMutableHeaderType target = null;
                    target = (EmptyMutableHeaderType)get_store().find_element_user(MUTABLE$2, 0);
                    if (target == null)
                    {
                      target = (EmptyMutableHeaderType)get_store().add_element_user(MUTABLE$2);
                    }
                    target.set(mutable);
                }
            }
            
            /**
             * Appends and returns a new empty "mutable" element
             */
            public EmptyMutableHeaderType addNewMutable()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    EmptyMutableHeaderType target = null;
                    target = (EmptyMutableHeaderType)get_store().add_element_user(MUTABLE$2);
                    return target;
                }
            }
            
            /**
             * Gets the "authenticate" attribute
             */
            public boolean getAuthenticate()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.SimpleValue target = null;
                    target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHENTICATE$4);
                    if (target == null)
                    {
                      target = (org.apache.xmlbeans.SimpleValue)get_default_attribute_value(AUTHENTICATE$4);
                    }
                    if (target == null)
                    {
                      return false;
                    }
                    return target.getBooleanValue();
                }
            }
            
            /**
             * Gets (as xml) the "authenticate" attribute
             */
            public org.apache.xmlbeans.XmlBoolean xgetAuthenticate()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.XmlBoolean target = null;
                    target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(AUTHENTICATE$4);
                    if (target == null)
                    {
                      target = (org.apache.xmlbeans.XmlBoolean)get_default_attribute_value(AUTHENTICATE$4);
                    }
                    return target;
                }
            }
            
            /**
             * Sets the "authenticate" attribute
             */
            public void setAuthenticate(boolean authenticate)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.SimpleValue target = null;
                    target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHENTICATE$4);
                    if (target == null)
                    {
                      target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(AUTHENTICATE$4);
                    }
                    target.setBooleanValue(authenticate);
                }
            }
            
            /**
             * Sets (as xml) the "authenticate" attribute
             */
            public void xsetAuthenticate(org.apache.xmlbeans.XmlBoolean authenticate)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.XmlBoolean target = null;
                    target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(AUTHENTICATE$4);
                    if (target == null)
                    {
                      target = (org.apache.xmlbeans.XmlBoolean)get_store().add_attribute_user(AUTHENTICATE$4);
                    }
                    target.set(authenticate);
                }
            }
        }
        /**
         * An XML body(@http://www.ebics.org/H003).
         *
         * This is a complex type.
         */
        public static class BodyImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body
        {
            private static final long serialVersionUID = 1L;
            
            public BodyImpl(org.apache.xmlbeans.SchemaType sType)
            {
                super(sType);
            }
            
            
        }
    }
}
