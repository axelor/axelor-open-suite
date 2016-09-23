/*
 * An XML document type.
 * Localname: ebicsResponse
 * Namespace: http://www.ebics.org/H003
 * Java type: EbicsResponseDocument
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.DataTransferResponseType;
import com.axelor.apps.account.ebics.schema.h003.EbicsResponseDocument;
import com.axelor.apps.account.ebics.schema.h003.ProtocolRevisionType;
import com.axelor.apps.account.ebics.schema.h003.ProtocolVersionType;
import com.axelor.apps.account.ebics.schema.h003.ResponseMutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.ResponseStaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.SignatureType;

/**
 * A document containing one ebicsResponse(@http://www.ebics.org/H003) element.
 *
 * This is a complex type.
 */
public class EbicsResponseDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements EbicsResponseDocument
{
    private static final long serialVersionUID = 1L;
    
    public EbicsResponseDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EBICSRESPONSE$0 = 
        new javax.xml.namespace.QName("http://www.ebics.org/H003", "ebicsResponse");
    
    
    /**
     * Gets the "ebicsResponse" element
     */
    public EbicsResponseDocument.EbicsResponse getEbicsResponse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            EbicsResponseDocument.EbicsResponse target = null;
            target = (EbicsResponseDocument.EbicsResponse)get_store().find_element_user(EBICSRESPONSE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "ebicsResponse" element
     */
    public void setEbicsResponse(EbicsResponseDocument.EbicsResponse ebicsResponse)
    {
        synchronized (monitor())
        {
            check_orphaned();
            EbicsResponseDocument.EbicsResponse target = null;
            target = (EbicsResponseDocument.EbicsResponse)get_store().find_element_user(EBICSRESPONSE$0, 0);
            if (target == null)
            {
                target = (EbicsResponseDocument.EbicsResponse)get_store().add_element_user(EBICSRESPONSE$0);
            }
            target.set(ebicsResponse);
        }
    }
    
    /**
     * Appends and returns a new empty "ebicsResponse" element
     */
    public EbicsResponseDocument.EbicsResponse addNewEbicsResponse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            EbicsResponseDocument.EbicsResponse target = null;
            target = (EbicsResponseDocument.EbicsResponse)get_store().add_element_user(EBICSRESPONSE$0);
            return target;
        }
    }
    /**
     * An XML ebicsResponse(@http://www.ebics.org/H003).
     *
     * This is a complex type.
     */
    public static class EbicsResponseImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements EbicsResponseDocument.EbicsResponse
    {
        private static final long serialVersionUID = 1L;
        
        public EbicsResponseImpl(org.apache.xmlbeans.SchemaType sType)
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
        public EbicsResponseDocument.EbicsResponse.Header getHeader()
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsResponseDocument.EbicsResponse.Header target = null;
                target = (EbicsResponseDocument.EbicsResponse.Header)get_store().find_element_user(HEADER$0, 0);
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
        public void setHeader(EbicsResponseDocument.EbicsResponse.Header header)
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsResponseDocument.EbicsResponse.Header target = null;
                target = (EbicsResponseDocument.EbicsResponse.Header)get_store().find_element_user(HEADER$0, 0);
                if (target == null)
                {
                    target = (EbicsResponseDocument.EbicsResponse.Header)get_store().add_element_user(HEADER$0);
                }
                target.set(header);
            }
        }
        
        /**
         * Appends and returns a new empty "header" element
         */
        public EbicsResponseDocument.EbicsResponse.Header addNewHeader()
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsResponseDocument.EbicsResponse.Header target = null;
                target = (EbicsResponseDocument.EbicsResponse.Header)get_store().add_element_user(HEADER$0);
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
        public EbicsResponseDocument.EbicsResponse.Body getBody()
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsResponseDocument.EbicsResponse.Body target = null;
                target = (EbicsResponseDocument.EbicsResponse.Body)get_store().find_element_user(BODY$4, 0);
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
        public void setBody(EbicsResponseDocument.EbicsResponse.Body body)
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsResponseDocument.EbicsResponse.Body target = null;
                target = (EbicsResponseDocument.EbicsResponse.Body)get_store().find_element_user(BODY$4, 0);
                if (target == null)
                {
                    target = (EbicsResponseDocument.EbicsResponse.Body)get_store().add_element_user(BODY$4);
                }
                target.set(body);
            }
        }
        
        /**
         * Appends and returns a new empty "body" element
         */
        public EbicsResponseDocument.EbicsResponse.Body addNewBody()
        {
            synchronized (monitor())
            {
                check_orphaned();
                EbicsResponseDocument.EbicsResponse.Body target = null;
                target = (EbicsResponseDocument.EbicsResponse.Body)get_store().add_element_user(BODY$4);
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
        public static class HeaderImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements EbicsResponseDocument.EbicsResponse.Header
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
            public ResponseStaticHeaderType getStatic()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    ResponseStaticHeaderType target = null;
                    target = (ResponseStaticHeaderType)get_store().find_element_user(STATIC$0, 0);
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
            public void setStatic(ResponseStaticHeaderType xstatic)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    ResponseStaticHeaderType target = null;
                    target = (ResponseStaticHeaderType)get_store().find_element_user(STATIC$0, 0);
                    if (target == null)
                    {
                      target = (ResponseStaticHeaderType)get_store().add_element_user(STATIC$0);
                    }
                    target.set(xstatic);
                }
            }
            
            /**
             * Appends and returns a new empty "static" element
             */
            public ResponseStaticHeaderType addNewStatic()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    ResponseStaticHeaderType target = null;
                    target = (ResponseStaticHeaderType)get_store().add_element_user(STATIC$0);
                    return target;
                }
            }
            
            /**
             * Gets the "mutable" element
             */
            public ResponseMutableHeaderType getMutable()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    ResponseMutableHeaderType target = null;
                    target = (ResponseMutableHeaderType)get_store().find_element_user(MUTABLE$2, 0);
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
            public void setMutable(ResponseMutableHeaderType mutable)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    ResponseMutableHeaderType target = null;
                    target = (ResponseMutableHeaderType)get_store().find_element_user(MUTABLE$2, 0);
                    if (target == null)
                    {
                      target = (ResponseMutableHeaderType)get_store().add_element_user(MUTABLE$2);
                    }
                    target.set(mutable);
                }
            }
            
            /**
             * Appends and returns a new empty "mutable" element
             */
            public ResponseMutableHeaderType addNewMutable()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    ResponseMutableHeaderType target = null;
                    target = (ResponseMutableHeaderType)get_store().add_element_user(MUTABLE$2);
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
        public static class BodyImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements EbicsResponseDocument.EbicsResponse.Body
        {
            private static final long serialVersionUID = 1L;
            
            public BodyImpl(org.apache.xmlbeans.SchemaType sType)
            {
                super(sType);
            }
            
            private static final javax.xml.namespace.QName DATATRANSFER$0 = 
                new javax.xml.namespace.QName("http://www.ebics.org/H003", "DataTransfer");
            private static final javax.xml.namespace.QName RETURNCODE$2 = 
                new javax.xml.namespace.QName("http://www.ebics.org/H003", "ReturnCode");
            private static final javax.xml.namespace.QName TIMESTAMPBANKPARAMETER$4 = 
                new javax.xml.namespace.QName("http://www.ebics.org/H003", "TimestampBankParameter");
            
            
            /**
             * Gets the "DataTransfer" element
             */
            public DataTransferResponseType getDataTransfer()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    DataTransferResponseType target = null;
                    target = (DataTransferResponseType)get_store().find_element_user(DATATRANSFER$0, 0);
                    if (target == null)
                    {
                      return null;
                    }
                    return target;
                }
            }
            
            /**
             * True if has "DataTransfer" element
             */
            public boolean isSetDataTransfer()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    return get_store().count_elements(DATATRANSFER$0) != 0;
                }
            }
            
            /**
             * Sets the "DataTransfer" element
             */
            public void setDataTransfer(DataTransferResponseType dataTransfer)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    DataTransferResponseType target = null;
                    target = (DataTransferResponseType)get_store().find_element_user(DATATRANSFER$0, 0);
                    if (target == null)
                    {
                      target = (DataTransferResponseType)get_store().add_element_user(DATATRANSFER$0);
                    }
                    target.set(dataTransfer);
                }
            }
            
            /**
             * Appends and returns a new empty "DataTransfer" element
             */
            public DataTransferResponseType addNewDataTransfer()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    DataTransferResponseType target = null;
                    target = (DataTransferResponseType)get_store().add_element_user(DATATRANSFER$0);
                    return target;
                }
            }
            
            /**
             * Unsets the "DataTransfer" element
             */
            public void unsetDataTransfer()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    get_store().remove_element(DATATRANSFER$0, 0);
                }
            }
            
            /**
             * Gets the "ReturnCode" element
             */
            public EbicsResponseDocument.EbicsResponse.Body.ReturnCode getReturnCode()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    EbicsResponseDocument.EbicsResponse.Body.ReturnCode target = null;
                    target = (EbicsResponseDocument.EbicsResponse.Body.ReturnCode)get_store().find_element_user(RETURNCODE$2, 0);
                    if (target == null)
                    {
                      return null;
                    }
                    return target;
                }
            }
            
            /**
             * Sets the "ReturnCode" element
             */
            public void setReturnCode(EbicsResponseDocument.EbicsResponse.Body.ReturnCode returnCode)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    EbicsResponseDocument.EbicsResponse.Body.ReturnCode target = null;
                    target = (EbicsResponseDocument.EbicsResponse.Body.ReturnCode)get_store().find_element_user(RETURNCODE$2, 0);
                    if (target == null)
                    {
                      target = (EbicsResponseDocument.EbicsResponse.Body.ReturnCode)get_store().add_element_user(RETURNCODE$2);
                    }
                    target.set(returnCode);
                }
            }
            
            /**
             * Appends and returns a new empty "ReturnCode" element
             */
            public EbicsResponseDocument.EbicsResponse.Body.ReturnCode addNewReturnCode()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    EbicsResponseDocument.EbicsResponse.Body.ReturnCode target = null;
                    target = (EbicsResponseDocument.EbicsResponse.Body.ReturnCode)get_store().add_element_user(RETURNCODE$2);
                    return target;
                }
            }
            
            /**
             * Gets the "TimestampBankParameter" element
             */
            public EbicsResponseDocument.EbicsResponse.Body.TimestampBankParameter getTimestampBankParameter()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    EbicsResponseDocument.EbicsResponse.Body.TimestampBankParameter target = null;
                    target = (EbicsResponseDocument.EbicsResponse.Body.TimestampBankParameter)get_store().find_element_user(TIMESTAMPBANKPARAMETER$4, 0);
                    if (target == null)
                    {
                      return null;
                    }
                    return target;
                }
            }
            
            /**
             * True if has "TimestampBankParameter" element
             */
            public boolean isSetTimestampBankParameter()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    return get_store().count_elements(TIMESTAMPBANKPARAMETER$4) != 0;
                }
            }
            
            /**
             * Sets the "TimestampBankParameter" element
             */
            public void setTimestampBankParameter(EbicsResponseDocument.EbicsResponse.Body.TimestampBankParameter timestampBankParameter)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    EbicsResponseDocument.EbicsResponse.Body.TimestampBankParameter target = null;
                    target = (EbicsResponseDocument.EbicsResponse.Body.TimestampBankParameter)get_store().find_element_user(TIMESTAMPBANKPARAMETER$4, 0);
                    if (target == null)
                    {
                      target = (EbicsResponseDocument.EbicsResponse.Body.TimestampBankParameter)get_store().add_element_user(TIMESTAMPBANKPARAMETER$4);
                    }
                    target.set(timestampBankParameter);
                }
            }
            
            /**
             * Appends and returns a new empty "TimestampBankParameter" element
             */
            public EbicsResponseDocument.EbicsResponse.Body.TimestampBankParameter addNewTimestampBankParameter()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    EbicsResponseDocument.EbicsResponse.Body.TimestampBankParameter target = null;
                    target = (EbicsResponseDocument.EbicsResponse.Body.TimestampBankParameter)get_store().add_element_user(TIMESTAMPBANKPARAMETER$4);
                    return target;
                }
            }
            
            /**
             * Unsets the "TimestampBankParameter" element
             */
            public void unsetTimestampBankParameter()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    get_store().remove_element(TIMESTAMPBANKPARAMETER$4, 0);
                }
            }
            /**
             * An XML ReturnCode(@http://www.ebics.org/H003).
             *
             * This is an atomic type that is a restriction of EbicsResponseDocument$EbicsResponse$Body$ReturnCode.
             */
            public static class ReturnCodeImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements EbicsResponseDocument.EbicsResponse.Body.ReturnCode
            {
                private static final long serialVersionUID = 1L;
                
                public ReturnCodeImpl(org.apache.xmlbeans.SchemaType sType)
                {
                    super(sType, true);
                }
                
                protected ReturnCodeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
                {
                    super(sType, b);
                }
                
                private static final javax.xml.namespace.QName AUTHENTICATE$0 = 
                    new javax.xml.namespace.QName("", "authenticate");
                
                
                /**
                 * Gets the "authenticate" attribute
                 */
                public boolean getAuthenticate()
                {
                    synchronized (monitor())
                    {
                      check_orphaned();
                      org.apache.xmlbeans.SimpleValue target = null;
                      target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHENTICATE$0);
                      if (target == null)
                      {
                        target = (org.apache.xmlbeans.SimpleValue)get_default_attribute_value(AUTHENTICATE$0);
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
                      target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(AUTHENTICATE$0);
                      if (target == null)
                      {
                        target = (org.apache.xmlbeans.XmlBoolean)get_default_attribute_value(AUTHENTICATE$0);
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
                      target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHENTICATE$0);
                      if (target == null)
                      {
                        target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(AUTHENTICATE$0);
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
                      target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(AUTHENTICATE$0);
                      if (target == null)
                      {
                        target = (org.apache.xmlbeans.XmlBoolean)get_store().add_attribute_user(AUTHENTICATE$0);
                      }
                      target.set(authenticate);
                    }
                }
            }
            /**
             * An XML TimestampBankParameter(@http://www.ebics.org/H003).
             *
             * This is an atomic type that is a restriction of EbicsResponseDocument$EbicsResponse$Body$TimestampBankParameter.
             */
            public static class TimestampBankParameterImpl extends org.apache.xmlbeans.impl.values.JavaGDateHolderEx implements EbicsResponseDocument.EbicsResponse.Body.TimestampBankParameter
            {
                private static final long serialVersionUID = 1L;
                
                public TimestampBankParameterImpl(org.apache.xmlbeans.SchemaType sType)
                {
                    super(sType, true);
                }
                
                protected TimestampBankParameterImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
                {
                    super(sType, b);
                }
                
                private static final javax.xml.namespace.QName AUTHENTICATE$0 = 
                    new javax.xml.namespace.QName("", "authenticate");
                
                
                /**
                 * Gets the "authenticate" attribute
                 */
                public boolean getAuthenticate()
                {
                    synchronized (monitor())
                    {
                      check_orphaned();
                      org.apache.xmlbeans.SimpleValue target = null;
                      target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHENTICATE$0);
                      if (target == null)
                      {
                        target = (org.apache.xmlbeans.SimpleValue)get_default_attribute_value(AUTHENTICATE$0);
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
                      target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(AUTHENTICATE$0);
                      if (target == null)
                      {
                        target = (org.apache.xmlbeans.XmlBoolean)get_default_attribute_value(AUTHENTICATE$0);
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
                      target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHENTICATE$0);
                      if (target == null)
                      {
                        target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(AUTHENTICATE$0);
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
                      target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(AUTHENTICATE$0);
                      if (target == null)
                      {
                        target = (org.apache.xmlbeans.XmlBoolean)get_store().add_attribute_user(AUTHENTICATE$0);
                      }
                      target.set(authenticate);
                    }
                }
            }
        }
    }
}
