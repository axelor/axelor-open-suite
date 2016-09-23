/*
 * XML Type:  CryptoVersionType
 * Namespace: http://www.ebics.org/H003
 * Java type: CryptoVersionType
 *
 * Automatically generated - do not modify.
 */
package com.axelor.apps.account.ebics.schema.h003.impl;

import com.axelor.apps.account.ebics.schema.h003.AuthenticationVersionType;
import com.axelor.apps.account.ebics.schema.h003.CryptoVersionType;
import com.axelor.apps.account.ebics.schema.h003.EncryptionVersionType;
import com.axelor.apps.account.ebics.schema.h003.SignatureVersionType;

/**
 * An XML CryptoVersionType(@http://www.ebics.org/H003).
 *
 * This is a union type. Instances are of one of the following types:
 *     EncryptionVersionType
 *     SignatureVersionType
 *     AuthenticationVersionType
 */
public class CryptoVersionTypeImpl extends org.apache.xmlbeans.impl.values.XmlUnionImpl implements CryptoVersionType, EncryptionVersionType, SignatureVersionType, AuthenticationVersionType
{
    private static final long serialVersionUID = 1L;
    
    public CryptoVersionTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType, false);
    }
    
    protected CryptoVersionTypeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
    {
        super(sType, b);
    }
}
