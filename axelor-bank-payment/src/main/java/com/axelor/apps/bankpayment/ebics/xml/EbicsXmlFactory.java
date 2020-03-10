/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.ebics.xml;

import com.axelor.apps.account.ebics.schema.h003.AuthenticationPubKeyInfoType;
import com.axelor.apps.account.ebics.schema.h003.DataEncryptionInfoType.EncryptionPubKeyDigest;
import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType;
import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.DataEncryptionInfo;
import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.SignatureData;
import com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument;
import com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body.PreValidation;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body.TransferReceipt;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.EmptyMutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.EncryptionPubKeyInfoType;
import com.axelor.apps.account.ebics.schema.h003.FDLOrderParamsDocument;
import com.axelor.apps.account.ebics.schema.h003.FDLOrderParamsType;
import com.axelor.apps.account.ebics.schema.h003.FDLOrderParamsType.DateRange;
import com.axelor.apps.account.ebics.schema.h003.FULOrderParamsDocument;
import com.axelor.apps.account.ebics.schema.h003.FULOrderParamsType;
import com.axelor.apps.account.ebics.schema.h003.FileFormatType;
import com.axelor.apps.account.ebics.schema.h003.HIARequestOrderDataDocument;
import com.axelor.apps.account.ebics.schema.h003.HIARequestOrderDataType;
import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType.SegmentNumber;
import com.axelor.apps.account.ebics.schema.h003.NoPubKeyDigestsRequestStaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.OrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.ParameterDocument.Parameter;
import com.axelor.apps.account.ebics.schema.h003.ParameterDocument.Parameter.Value;
import com.axelor.apps.account.ebics.schema.h003.ProductElementType;
import com.axelor.apps.account.ebics.schema.h003.StandardOrderParamsDocument;
import com.axelor.apps.account.ebics.schema.h003.StandardOrderParamsType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderOrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderOrderDetailsType.OrderType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests.Authentication;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests.Encryption;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.Product;
import com.axelor.apps.account.ebics.schema.h003.TransactionPhaseType.Enum;
import com.axelor.apps.account.ebics.schema.h003.UnsecuredRequestStaticHeaderType;
import com.axelor.apps.account.ebics.schema.s001.OrderSignatureDataType;
import com.axelor.apps.account.ebics.schema.s001.PubKeyValueType;
import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyInfoType;
import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyOrderDataDocument;
import com.axelor.apps.account.ebics.schema.s001.SignaturePubKeyOrderDataType;
import com.axelor.apps.account.ebics.schema.s001.UserSignatureDataDocument;
import com.axelor.apps.account.ebics.schema.s001.UserSignatureDataSigBookType;
import com.axelor.apps.account.ebics.schema.xmldsig.CanonicalizationMethodType;
import com.axelor.apps.account.ebics.schema.xmldsig.DigestMethodType;
import com.axelor.apps.account.ebics.schema.xmldsig.RSAKeyValueType;
import com.axelor.apps.account.ebics.schema.xmldsig.ReferenceType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureMethodType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureValueType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignedInfoDocument;
import com.axelor.apps.account.ebics.schema.xmldsig.SignedInfoType;
import com.axelor.apps.account.ebics.schema.xmldsig.TransformType;
import com.axelor.apps.account.ebics.schema.xmldsig.TransformsType;
import com.axelor.apps.account.ebics.schema.xmldsig.X509DataType;
import com.axelor.apps.account.ebics.schema.xmldsig.X509IssuerSerialType;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * A factory to produce XML object for EBICS requests. This factory is based on xmlbeans object
 * generated from EBICS XML schemas.
 *
 * @see XmlObject
 * @author hachani
 */
public class EbicsXmlFactory {

  /**
   * Creates a new <code>SignedInfoDocument</code> XML object
   *
   * @param signedInfo the <code>SignedInfoType</code> element
   * @return the <code>SignedInfoDocument</code> XML object
   */
  public static SignedInfoDocument createSignedInfoDocument(SignedInfoType signedInfo) {
    SignedInfoDocument newSignedInfoDocument = SignedInfoDocument.Factory.newInstance();
    newSignedInfoDocument.setSignedInfo(signedInfo);

    return newSignedInfoDocument;
  }

  /**
   * Creates a new <code>SignatureType</code> XML object
   *
   * @param signedInfo the <code>SignedInfoType</code> element
   * @return the <code>SignatureType</code> XML object
   */
  public static SignatureType createSignatureType(SignedInfoType signedInfo) {
    SignatureType newSignatureType = SignatureType.Factory.newInstance();
    newSignatureType.setSignedInfo(signedInfo);

    return newSignatureType;
  }

  /**
   * Creates a new <code>SignedInfoType</code> XML object
   *
   * @param canonicalizationMethod the <code>CanonicalizationMethod</code> element
   * @param signatureMethod the <code>SignatureMethod</code> element
   * @param referenceArray the <code>ReferenceType</code> array element
   * @return the <code>SignedInfoType</code> XML object
   */
  public static SignedInfoType createSignedInfoType(
      CanonicalizationMethodType canonicalizationMethod,
      SignatureMethodType signatureMethod,
      ReferenceType[] referenceArray) {
    SignedInfoType newSignedInfoType = SignedInfoType.Factory.newInstance();
    newSignedInfoType.setSignatureMethod(signatureMethod);
    newSignedInfoType.setCanonicalizationMethod(canonicalizationMethod);
    newSignedInfoType.setReferenceArray(referenceArray);

    return newSignedInfoType;
  }

  /**
   * Creates a new <code>SignatureValueType</code> XML object
   *
   * @param signatureValue the <code>SignatureMethod</code> element
   * @return the <code>SignatureValueType</code> XML object
   */
  public static SignatureValueType createSignatureValueType(byte[] signatureValue) {
    SignatureValueType newSignatureValueType = SignatureValueType.Factory.newInstance();
    newSignatureValueType.setByteArrayValue(signatureValue);

    return newSignatureValueType;
  }

  /**
   * Creates a new <code>SignatureValueType</code> XML object
   *
   * @param algorithm the signature algorithm
   * @return the <code>SignatureValueType</code> XML object
   */
  public static SignatureMethodType createSignatureMethodType(String algorithm) {
    SignatureMethodType newSignatureMethodType = SignatureMethodType.Factory.newInstance();
    newSignatureMethodType.setAlgorithm(algorithm);

    return newSignatureMethodType;
  }

  /**
   * Creates a new <code>CanonicalizationMethodType</code> XML object
   *
   * @param algorithm the canonicalization algorithm
   * @return the <code>CanonicalizationMethodType</code> XML object
   */
  public static CanonicalizationMethodType createCanonicalizationMethodType(String algorithm) {
    CanonicalizationMethodType newCanonicalizationMethodType =
        CanonicalizationMethodType.Factory.newInstance();
    newCanonicalizationMethodType.setAlgorithm(algorithm);

    return newCanonicalizationMethodType;
  }

  /**
   * Creates a new <code>ReferenceType</code> XML object
   *
   * @param uri the reference uri
   * @param transforms the <code>TransformsType</code> element
   * @param digestMethod the <code>DigestMethodType</code> element
   * @param digestValue the digest value
   * @return the <code>ReferenceType</code> XML object
   */
  public static ReferenceType createReferenceType(
      String uri, TransformsType transforms, DigestMethodType digestMethod, byte[] digestValue) {
    ReferenceType newReferenceType = ReferenceType.Factory.newInstance();
    newReferenceType.setURI(uri);
    newReferenceType.setTransforms(transforms);
    newReferenceType.setDigestMethod(digestMethod);
    newReferenceType.setDigestValue(digestValue);

    return newReferenceType;
  }

  /**
   * Creates a new <code>TransformsType</code> XML object
   *
   * @param transformArray the <code>TransformsType</code> array element
   * @return the <code>TransformsType</code> XML object
   */
  public static TransformsType createTransformsType(TransformType[] transformArray) {
    TransformsType newTransformsType = TransformsType.Factory.newInstance();
    newTransformsType.setTransformArray(transformArray);

    return newTransformsType;
  }

  /**
   * Creates a new <code>TransformType</code> XML object
   *
   * @param algorithm the transformation algorithm
   * @return the <code>TransformType</code> XML object
   */
  public static TransformType createTransformType(String algorithm) {
    TransformType newTransformType = TransformType.Factory.newInstance();
    newTransformType.setAlgorithm(algorithm);

    return newTransformType;
  }

  /**
   * Creates a new <code>DigestMethodType</code> XML object
   *
   * @param algorithm the digest method algorithm
   * @return the <code>DigestMethodType</code> XML object
   */
  public static DigestMethodType createDigestMethodType(String algorithm) {
    DigestMethodType newDigestMethodType = DigestMethodType.Factory.newInstance();
    newDigestMethodType.setAlgorithm(algorithm);

    return newDigestMethodType;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------

  /**
   * Creates a new <code>UserSignatureDataDocument</code> XML object
   *
   * @param userSignatureData the <code>UserSignatureDataSigBookType</code> element
   * @return the <code>UserSignatureDataDocument</code> XML object
   */
  public static UserSignatureDataDocument createUserSignatureDataDocument(
      UserSignatureDataSigBookType userSignatureData) {
    UserSignatureDataDocument newUserSignatureDataDocument =
        UserSignatureDataDocument.Factory.newInstance();
    newUserSignatureDataDocument.setUserSignatureData(userSignatureData);

    return newUserSignatureDataDocument;
  }

  /**
   * Creates a new <code>UserSignatureDataSigBookType</code> XML object
   *
   * @param orderSignatureDataArray the <code>OrderSignatureDataType</code> array element
   * @return the <code>UserSignatureDataSigBookType</code> XML object
   */
  public static UserSignatureDataSigBookType createUserSignatureDataSigBookType(
      OrderSignatureDataType[] orderSignatureDataArray) {
    UserSignatureDataSigBookType newUserSignatureDataSigBookType =
        UserSignatureDataSigBookType.Factory.newInstance();
    newUserSignatureDataSigBookType.setOrderSignatureDataArray(orderSignatureDataArray);

    return newUserSignatureDataSigBookType;
  }

  /**
   * Creates a new <code>OrderSignatureDataType</code> XML object
   *
   * @param signatureVersion the signature version
   * @param partnerID the partner id
   * @param userID the user id
   * @param signatureValue the signature value
   * @return the <code>OrderSignatureDataType</code> XML object
   */
  public static OrderSignatureDataType createOrderSignatureDataType(
      String signatureVersion, String partnerID, String userID, byte[] signatureValue) {
    OrderSignatureDataType newOrderSignatureDataType = OrderSignatureDataType.Factory.newInstance();
    newOrderSignatureDataType.setSignatureVersion(signatureVersion);
    newOrderSignatureDataType.setPartnerID(partnerID);
    newOrderSignatureDataType.setUserID(userID);
    newOrderSignatureDataType.setSignatureValue(signatureValue);

    System.out.println(
        "signature value : " + new String(newOrderSignatureDataType.getSignatureValue()));

    return newOrderSignatureDataType;
  }

  /**
   * Creates a new <code>OrderSignatureDataType</code> XML object
   *
   * @param signatureVersion the signature version
   * @param partnerID the partner id
   * @param userID the user id
   * @param signatureValue the signature value
   * @return the <code>OrderSignatureDataType</code> XML object
   */
  public static OrderSignatureDataType createOrderSignatureDataType(
      String signatureVersion,
      String partnerID,
      String userID,
      byte[] signatureValue,
      X509DataType x509Data) {
    OrderSignatureDataType newOrderSignatureDataType = OrderSignatureDataType.Factory.newInstance();
    newOrderSignatureDataType.setSignatureVersion(signatureVersion);
    newOrderSignatureDataType.setPartnerID(partnerID);
    newOrderSignatureDataType.setUserID(userID);
    newOrderSignatureDataType.setSignatureValue(signatureValue);
    newOrderSignatureDataType.setX509Data(x509Data);

    System.out.println(
        "signature value : " + new String(newOrderSignatureDataType.getSignatureValue()));

    return newOrderSignatureDataType;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------

  /**
   * Creates a new <code>SignaturePubKeyOrderDataDocument</code> XML object
   *
   * @param signaturePubKeyOrderData the <code>SignaturePubKeyOrderDataType</code> element
   * @return
   */
  public static SignaturePubKeyOrderDataDocument createSignaturePubKeyOrderDataDocument(
      SignaturePubKeyOrderDataType signaturePubKeyOrderData) {
    SignaturePubKeyOrderDataDocument newSignaturePubKeyOrderDataDocument =
        SignaturePubKeyOrderDataDocument.Factory.newInstance();
    newSignaturePubKeyOrderDataDocument.setSignaturePubKeyOrderData(signaturePubKeyOrderData);

    return newSignaturePubKeyOrderDataDocument;
  }

  /**
   * Creates a new <code>SignaturePubKeyOrderDataType</code> XML object
   *
   * @param signaturePubKeyInfo the <code>SignaturePubKeyInfoType</code> element
   * @param partnerId the partner ID
   * @param userId the user ID
   * @return the <code>SignaturePubKeyOrderDataType</code> XML object
   */
  public static SignaturePubKeyOrderDataType createSignaturePubKeyOrderData(
      SignaturePubKeyInfoType signaturePubKeyInfo, String partnerId, String userId) {
    SignaturePubKeyOrderDataType newSignaturePubKeyOrderDataType =
        SignaturePubKeyOrderDataType.Factory.newInstance();
    newSignaturePubKeyOrderDataType.setSignaturePubKeyInfo(signaturePubKeyInfo);
    newSignaturePubKeyOrderDataType.setPartnerID(partnerId);
    newSignaturePubKeyOrderDataType.setUserID(userId);

    return newSignaturePubKeyOrderDataType;
  }

  /**
   * Creates a new <code>SignaturePubKeyInfoType</code> XML object
   *
   * @param x509Data the <code>X509DataType</code> element
   * @param pubKeyValue <code>PubKeyValueType</code> element
   * @param signatureVersion the signature version
   * @return the <code>SignaturePubKeyInfoType</code> XML object
   */
  public static SignaturePubKeyInfoType createSignaturePubKeyInfoType(
      X509DataType x509Data, PubKeyValueType pubKeyValue, String signatureVersion) {
    SignaturePubKeyInfoType newSignaturePubKeyInfoType =
        SignaturePubKeyInfoType.Factory.newInstance();
    newSignaturePubKeyInfoType.setX509Data(x509Data);
    newSignaturePubKeyInfoType.setPubKeyValue(pubKeyValue);
    newSignaturePubKeyInfoType.setSignatureVersion(signatureVersion);

    return newSignaturePubKeyInfoType;
  }

  /**
   * Creates a new <code>X509DataType</code> XML object
   *
   * @param x509SubjectName the subject name
   * @param x509Certificate the certificate
   * @return the <code>X509DataType</code> XML object
   */
  public static X509DataType createX509DataType(
      String x509SubjectName,
      byte[] x509Certificate,
      String x509IssuerName,
      BigInteger x509SerialNumber) {
    X509DataType newX509DataType = X509DataType.Factory.newInstance();
    newX509DataType.setX509SubjectNameArray(new String[] {x509SubjectName});
    newX509DataType.setX509CertificateArray(new byte[][] {x509Certificate});
    X509IssuerSerialType x509IssuerSerialType = X509IssuerSerialType.Factory.newInstance();
    x509IssuerSerialType.setX509IssuerName(x509IssuerName);
    x509IssuerSerialType.setX509SerialNumber(x509SerialNumber);

    X509IssuerSerialType[] x509IssuerSerialTypes = {x509IssuerSerialType};

    newX509DataType.setX509IssuerSerialArray(x509IssuerSerialTypes);

    return newX509DataType;
  }

  /**
   * Creates a new <code>X509DataType</code> XML object
   *
   * @param x509SubjectName the subject name
   * @param x509Certificate the certificate
   * @return the <code>X509DataType</code> XML object
   */
  public static X509DataType createX509DataType(String x509SubjectName, byte[] x509Certificate) {
    X509DataType newX509DataType = X509DataType.Factory.newInstance();
    newX509DataType.setX509SubjectNameArray(new String[] {x509SubjectName});
    newX509DataType.setX509CertificateArray(new byte[][] {x509Certificate});

    return newX509DataType;
  }

  /**
   * Creates a new <code>PubKeyValueType</code> XML object
   *
   * @param rsaKeyValue the <code>rsaKeyValue</code> element
   * @param timeStamp the current time stamp
   * @return the <code>PubKeyValueType</code> XML object
   */
  public static PubKeyValueType createPubKeyValueType(
      RSAKeyValueType rsaKeyValue, Calendar timeStamp) {
    PubKeyValueType newPubKeyValueType = PubKeyValueType.Factory.newInstance();
    newPubKeyValueType.setRSAKeyValue(rsaKeyValue);
    newPubKeyValueType.setTimeStamp(timeStamp);

    return newPubKeyValueType;
  }

  /**
   * Creates a new <code>RSAKeyValueType</code> XML object
   *
   * @param exponent the public exponent of the public key
   * @param modulus the modulus of the public key
   * @return the <code>RSAKeyValueType</code> XML object
   */
  public static RSAKeyValueType createRSAKeyValueType(byte[] exponent, byte[] modulus) {
    RSAKeyValueType newRSAKeyValueType = RSAKeyValueType.Factory.newInstance();
    newRSAKeyValueType.setExponent(exponent);
    newRSAKeyValueType.setModulus(modulus);

    return newRSAKeyValueType;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------

  /**
   * Creates a new <code>EbicsUnsecuredRequestDocument</code> XML object
   *
   * @param ebicsUnsecuredRequest the <code>EbicsUnsecuredRequest</code> element
   * @return the <code>EbicsUnsecuredRequestDocument</code> XML object
   */
  public static EbicsUnsecuredRequestDocument createEbicsUnsecuredRequestDocument(
      EbicsUnsecuredRequest ebicsUnsecuredRequest) {
    EbicsUnsecuredRequestDocument newEbicsUnsecuredRequestDocument =
        EbicsUnsecuredRequestDocument.Factory.newInstance();
    newEbicsUnsecuredRequestDocument.setEbicsUnsecuredRequest(ebicsUnsecuredRequest);

    return newEbicsUnsecuredRequestDocument;
  }

  /**
   * Creates a new <code>EbicsUnsecuredRequest</code> XML object
   *
   * @param header the <code>Header</code> element
   * @param body the <code>Body</code> element
   * @param revision the current revision
   * @param version the current version
   * @return the <code>EbicsUnsecuredRequest</code> XML object
   */
  public static EbicsUnsecuredRequest createEbicsUnsecuredRequest(
      Header header, Body body, int revision, String version) {
    EbicsUnsecuredRequest newEbicsUnsecuredRequest = EbicsUnsecuredRequest.Factory.newInstance();
    newEbicsUnsecuredRequest.setHeader(header);
    newEbicsUnsecuredRequest.setBody(body);
    newEbicsUnsecuredRequest.setRevision(revision);
    newEbicsUnsecuredRequest.setVersion(version);

    return newEbicsUnsecuredRequest;
  }

  /**
   * Creates a new <code>Header</code> XML object
   *
   * @param authenticate should authenticate?
   * @param mutable the <code>EmptyMutableHeaderType</code> element
   * @param xstatic the <code>UnsecuredRequestStaticHeaderType</code> element
   * @return the <code>Header</code> XML object
   */
  public static Header createHeader(
      boolean authenticate,
      EmptyMutableHeaderType mutable,
      UnsecuredRequestStaticHeaderType xstatic) {
    Header newHeader = Header.Factory.newInstance();
    newHeader.setAuthenticate(authenticate);
    newHeader.setMutable(mutable);
    newHeader.setStatic(xstatic);

    return newHeader;
  }

  /**
   * Creates a new <code>EmptyMutableHeaderType</code> XML object
   *
   * @return the <code>EmptyMutableHeaderType</code> XML object
   */
  public static EmptyMutableHeaderType createEmptyMutableHeaderType() {
    EmptyMutableHeaderType newEmptyMutableHeaderType = EmptyMutableHeaderType.Factory.newInstance();

    return newEmptyMutableHeaderType;
  }

  /**
   * Creates a new <code>EmptyMutableHeaderType</code> XML object
   *
   * @param hostId the host ID
   * @param partnerId the partner ID
   * @param userId the user ID
   * @param product the <code>ProductElementType</code> element
   * @param orderDetails the <code>OrderDetailsType</code> element
   * @param securityMedium the security medium
   * @return the <code>EmptyMutableHeaderType</code> XML object
   */
  public static UnsecuredRequestStaticHeaderType createUnsecuredRequestStaticHeaderType(
      String hostId,
      String partnerId,
      String userId,
      ProductElementType product,
      OrderDetailsType orderDetails,
      String securityMedium) {
    UnsecuredRequestStaticHeaderType newUnsecuredRequestStaticHeaderType =
        UnsecuredRequestStaticHeaderType.Factory.newInstance();
    newUnsecuredRequestStaticHeaderType.setHostID(hostId);
    newUnsecuredRequestStaticHeaderType.setPartnerID(partnerId);
    newUnsecuredRequestStaticHeaderType.setUserID(userId);
    newUnsecuredRequestStaticHeaderType.setProduct(product);
    newUnsecuredRequestStaticHeaderType.setOrderDetails(orderDetails);
    newUnsecuredRequestStaticHeaderType.setSecurityMedium(securityMedium);

    return newUnsecuredRequestStaticHeaderType;
  }

  /**
   * Creates a new <code>ProductElementType</code> XML object
   *
   * @param language the language
   * @param product the product name
   * @return the <code>ProductElementType</code> XML object
   */
  public static ProductElementType creatProductElementType(String language, String product) {
    ProductElementType newProductElementType = ProductElementType.Factory.newInstance();
    newProductElementType.setLanguage(language);
    newProductElementType.setStringValue(product);

    return newProductElementType;
  }

  /**
   * Creates a new <code>OrderDetailsType</code> XML object
   *
   * @param orderAttribute the order attribute
   * @param orderId the order ID
   * @param orderType the order type
   * @return the <code>OrderDetailsType</code> XML object
   */
  @SuppressWarnings("deprecation")
  public static OrderDetailsType createOrderDetailsType(
      String orderAttribute, String orderId, String orderType) {
    OrderDetailsType newOrderDetailsType = OrderDetailsType.Factory.newInstance();
    newOrderDetailsType.setOrderAttribute(orderAttribute);
    if (orderId != null) {
      newOrderDetailsType.setOrderID(orderId);
    }
    newOrderDetailsType.setOrderType(orderType);

    return newOrderDetailsType;
  }

  /**
   * Creates a new <code>Body</code> XML object
   *
   * @param dataTransfer the <code>DataTransfer</code> element
   * @return the <code>Body</code> XML object
   */
  public static Body createBody(DataTransfer dataTransfer) {
    Body newBody = Body.Factory.newInstance();
    newBody.setDataTransfer(dataTransfer);

    return newBody;
  }

  /**
   * Creates a new <code>DataTransfer</code> XML object
   *
   * @param orderData the <code>OrderData</code> element
   * @return the <code>DataTransfer</code> XML object
   */
  public static DataTransfer createDataTransfer(OrderData orderData) {
    DataTransfer newDataTransfer = DataTransfer.Factory.newInstance();
    newDataTransfer.setOrderData(orderData);

    return newDataTransfer;
  }

  /**
   * Creates a new <code>OrderData</code> XML object
   *
   * @param orderData the order data as byte array
   * @return the <code>OrderData</code> XML object
   */
  public static OrderData createOrderData(byte[] orderData) {
    OrderData newOrderData = OrderData.Factory.newInstance();
    newOrderData.setByteArrayValue(orderData);

    return newOrderData;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------

  /**
   * Creates a new <code>HIARequestOrderDataDocument</code> XML object
   *
   * @param hiaRequestOrderData the <code>HIARequestOrderDataType</code> element
   * @return the <code>HIARequestOrderDataDocument</code> XML object
   */
  public static HIARequestOrderDataDocument createHIARequestOrderDataDocument(
      HIARequestOrderDataType hiaRequestOrderData) {
    HIARequestOrderDataDocument newHIARequestOrderDataDocument =
        HIARequestOrderDataDocument.Factory.newInstance();
    newHIARequestOrderDataDocument.setHIARequestOrderData(hiaRequestOrderData);

    return newHIARequestOrderDataDocument;
  }

  /**
   * Creates a new <code>HIARequestOrderDataType</code> XML object
   *
   * @param authenticationPubKeyInfo the <code>AuthenticationPubKeyInfoType</code> element
   * @param encryptionPubKeyInfo the <code>EncryptionPubKeyInfoType</code> element
   * @param partnerId the partner ID
   * @param userId the user ID
   * @return the <code>HIARequestOrderDataType</code> XML object
   */
  public static HIARequestOrderDataType createHIARequestOrderDataType(
      AuthenticationPubKeyInfoType authenticationPubKeyInfo,
      EncryptionPubKeyInfoType encryptionPubKeyInfo,
      String partnerId,
      String userId) {
    HIARequestOrderDataType newHIARequestOrderDataType =
        HIARequestOrderDataType.Factory.newInstance();
    newHIARequestOrderDataType.setAuthenticationPubKeyInfo(authenticationPubKeyInfo);
    newHIARequestOrderDataType.setEncryptionPubKeyInfo(encryptionPubKeyInfo);
    newHIARequestOrderDataType.setPartnerID(partnerId);
    newHIARequestOrderDataType.setUserID(userId);

    return newHIARequestOrderDataType;
  }

  /**
   * Creates a new <code>AuthenticationPubKeyInfoType</code> XML object
   *
   * @param authenticationVersion the authentication version
   * @param pubKeyValue the <code>com.axelor.apps.account.ebics.schema.h003.PubKeyValueType</code>
   *     element
   * @param x509Data the <code>X509DataType</code> element
   * @return the <code>AuthenticationPubKeyInfoType</code> XML object
   */
  public static AuthenticationPubKeyInfoType createAuthenticationPubKeyInfoType(
      String authenticationVersion,
      com.axelor.apps.account.ebics.schema.h003.PubKeyValueType pubKeyValue,
      X509DataType x509Data) {
    AuthenticationPubKeyInfoType newAuthenticationPubKeyInfoType =
        AuthenticationPubKeyInfoType.Factory.newInstance();
    newAuthenticationPubKeyInfoType.setAuthenticationVersion(authenticationVersion);
    newAuthenticationPubKeyInfoType.setX509Data(x509Data);
    newAuthenticationPubKeyInfoType.setPubKeyValue(pubKeyValue);

    return newAuthenticationPubKeyInfoType;
  }

  /**
   * Creates a new <code>EncryptionPubKeyInfoType</code> XML object
   *
   * @param encryptionVersion the encryption version
   * @param pubKeyValue the <code>com.axelor.apps.account.ebics.schema.h003.PubKeyValueType</code>
   *     element
   * @param x509Data the <code>X509DataType</code> element
   * @return the <code>EncryptionPubKeyInfoType</code> XML object
   */
  public static EncryptionPubKeyInfoType createEncryptionPubKeyInfoType(
      String encryptionVersion,
      com.axelor.apps.account.ebics.schema.h003.PubKeyValueType pubKeyValue,
      X509DataType x509Data) {
    EncryptionPubKeyInfoType newEncryptionPubKeyInfoType =
        EncryptionPubKeyInfoType.Factory.newInstance();
    newEncryptionPubKeyInfoType.setEncryptionVersion(encryptionVersion);
    newEncryptionPubKeyInfoType.setX509Data(x509Data);
    newEncryptionPubKeyInfoType.setPubKeyValue(pubKeyValue);

    return newEncryptionPubKeyInfoType;
  }

  /**
   * Creates a new <code>com.axelor.apps.account.ebics.schema.h003.PubKeyValueType</code> XML object
   *
   * @param rsaKeyValue the <code>RSAKeyValueType</code> element
   * @param timeStamp the current time stamp
   * @return the <code>com.axelor.apps.account.ebics.schema.h003.PubKeyValueType</code> XML object
   */
  public static com.axelor.apps.account.ebics.schema.h003.PubKeyValueType createH003PubKeyValueType(
      RSAKeyValueType rsaKeyValue, Calendar timeStamp) {
    com.axelor.apps.account.ebics.schema.h003.PubKeyValueType newPubKeyValueType =
        com.axelor.apps.account.ebics.schema.h003.PubKeyValueType.Factory.newInstance();
    newPubKeyValueType.setRSAKeyValue(rsaKeyValue);
    newPubKeyValueType.setTimeStamp(timeStamp);

    return newPubKeyValueType;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------

  /**
   * Creates a new <code>EbicsNoPubKeyDigestsRequest</code> XML object
   *
   * @param revision the default revision
   * @param version the default version
   * @param header the <code>
   *     com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header
   *     </code> element
   * @param body the <code>
   *     com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body
   *     </code> element
   * @return the <code>EbicsNoPubKeyDigestsRequest</code> XML object
   */
  public static EbicsNoPubKeyDigestsRequest createEbicsNoPubKeyDigestsRequest(
      int revision,
      String version,
      com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument
              .EbicsNoPubKeyDigestsRequest.Header
          header,
      com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument
              .EbicsNoPubKeyDigestsRequest.Body
          body) {
    EbicsNoPubKeyDigestsRequest newEbicsNoPubKeyDigestsRequest =
        EbicsNoPubKeyDigestsRequest.Factory.newInstance();
    newEbicsNoPubKeyDigestsRequest.setRevision(revision);
    newEbicsNoPubKeyDigestsRequest.setVersion(version);
    newEbicsNoPubKeyDigestsRequest.setHeader(header);
    newEbicsNoPubKeyDigestsRequest.setBody(body);

    return newEbicsNoPubKeyDigestsRequest;
  }

  /**
   * Creates a new <code>
   * com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header
   * </code> XML object
   *
   * @param authenticate should authenticate?
   * @param mutable the <code>EmptyMutableHeaderType</code> element
   * @param xstatic the <code>NoPubKeyDigestsRequestStaticHeaderType</code> element
   * @return the <code>
   *     com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header
   *     </code> XML object
   */
  public static com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument
          .EbicsNoPubKeyDigestsRequest.Header
      createDigestsRequestHeader(
          boolean authenticate,
          EmptyMutableHeaderType mutable,
          NoPubKeyDigestsRequestStaticHeaderType xstatic) {
    com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument
            .EbicsNoPubKeyDigestsRequest.Header
        newHeader =
            com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument
                .EbicsNoPubKeyDigestsRequest.Header.Factory.newInstance();
    newHeader.setAuthenticate(authenticate);
    newHeader.setMutable(mutable);
    newHeader.setStatic(xstatic);

    return newHeader;
  }

  /**
   * Creates a new <code>NoPubKeyDigestsRequestStaticHeaderType</code> XML object
   *
   * @param hostId the host ID
   * @param nonce a random nonce
   * @param timestamp the current time stamp
   * @param partnerId the partner ID
   * @param userId the user ID
   * @param product the <code>ProductElementType</code> element
   * @param orderDetails the <code>OrderDetailsType</code> element
   * @param securityMedium the user security medium
   * @return
   */
  public static NoPubKeyDigestsRequestStaticHeaderType createNoPubKeyDigestsRequestStaticHeaderType(
      String hostId,
      byte[] nonce,
      Calendar timestamp,
      String partnerId,
      String userId,
      ProductElementType product,
      OrderDetailsType orderDetails,
      String securityMedium) {
    NoPubKeyDigestsRequestStaticHeaderType newNoPubKeyDigestsRequestStaticHeaderType =
        NoPubKeyDigestsRequestStaticHeaderType.Factory.newInstance();
    newNoPubKeyDigestsRequestStaticHeaderType.setHostID(hostId);
    newNoPubKeyDigestsRequestStaticHeaderType.setNonce(nonce);
    newNoPubKeyDigestsRequestStaticHeaderType.setTimestamp(timestamp);
    newNoPubKeyDigestsRequestStaticHeaderType.setPartnerID(partnerId);
    newNoPubKeyDigestsRequestStaticHeaderType.setUserID(userId);
    newNoPubKeyDigestsRequestStaticHeaderType.setProduct(product);
    newNoPubKeyDigestsRequestStaticHeaderType.setOrderDetails(orderDetails);
    newNoPubKeyDigestsRequestStaticHeaderType.setSecurityMedium(securityMedium);

    return newNoPubKeyDigestsRequestStaticHeaderType;
  }

  /**
   * Creates a new <code>
   * com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body
   * </code> XML object
   *
   * @return the <code>
   *     com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body
   *     </code> XML object
   */
  public static com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument
          .EbicsNoPubKeyDigestsRequest.Body
      createDigestsRequestBody() {
    com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument
            .EbicsNoPubKeyDigestsRequest.Body
        newBody =
            com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument
                .EbicsNoPubKeyDigestsRequest.Body.Factory.newInstance();

    return newBody;
  }

  /**
   * Creates a new <code>EbicsNoPubKeyDigestsRequestDocument</code> XML object
   *
   * @param ebicsNoPubKeyDigestsRequest the <code>EbicsNoPubKeyDigestsRequest</code> element
   * @return the <code>EbicsNoPubKeyDigestsRequestDocument</code> XML object
   */
  public static EbicsNoPubKeyDigestsRequestDocument createEbicsNoPubKeyDigestsRequestDocument(
      EbicsNoPubKeyDigestsRequest ebicsNoPubKeyDigestsRequest) {
    EbicsNoPubKeyDigestsRequestDocument newEbicsNoPubKeyDigestsRequestDocument =
        EbicsNoPubKeyDigestsRequestDocument.Factory.newInstance();
    newEbicsNoPubKeyDigestsRequestDocument.setEbicsNoPubKeyDigestsRequest(
        ebicsNoPubKeyDigestsRequest);

    return newEbicsNoPubKeyDigestsRequestDocument;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------

  /**
   * Creates a new <code>EbicsRequestDocument</code> XML object
   *
   * @param ebicsRequest the <code>EbicsRequest</code> element
   * @return the <code>EbicsRequestDocument</code> XML object
   */
  public static EbicsRequestDocument createEbicsRequestDocument(EbicsRequest ebicsRequest) {
    EbicsRequestDocument newEbicsRequestDocument = EbicsRequestDocument.Factory.newInstance();
    newEbicsRequestDocument.setEbicsRequest(ebicsRequest);

    return newEbicsRequestDocument;
  }

  /**
   * Creates a new <code>EbicsRequest</code> XML object
   *
   * @param revision the default revision
   * @param version the default version
   * @param header the <code>
   *     com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header</code>
   *     element
   * @param body the <code>
   *     com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body</code>
   *     element
   * @return the <code>EbicsRequest</code> XML object
   */
  public static EbicsRequest createEbicsRequest(
      int revision,
      String version,
      com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header header,
      com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body body) {
    EbicsRequest newEbicsRequest = EbicsRequest.Factory.newInstance();
    newEbicsRequest.setRevision(revision);
    newEbicsRequest.setVersion(version);
    newEbicsRequest.setHeader(header);
    newEbicsRequest.setBody(body);

    return newEbicsRequest;
  }

  /**
   * Creates a new <code>
   * com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header</code> XML
   * object
   *
   * @param authenticate should authenticate?
   * @param mutable the <code>MutableHeaderType</code> element
   * @param xstatic the <code>StaticHeaderType</code> element
   * @return the <code>
   *     com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header</code>
   *     XML object
   */
  public static com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header
      createEbicsRequestHeader(
          boolean authenticate, MutableHeaderType mutable, StaticHeaderType xstatic) {
    com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header newHeader =
        com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header.Factory
            .newInstance();
    newHeader.setAuthenticate(authenticate);
    newHeader.setMutable(mutable);
    newHeader.setStatic(xstatic);

    return newHeader;
  }

  /**
   * Creates a new <code>MutableHeaderType</code> XML object
   *
   * @param transactionPhase the transaction phase
   * @param segmentNumber the <code>SegmentNumber</code> element
   * @return the <code>MutableHeaderType</code> XML object
   */
  public static MutableHeaderType createMutableHeaderType(
      String transactionPhase, SegmentNumber segmentNumber) {
    MutableHeaderType newMutableHeaderType = MutableHeaderType.Factory.newInstance();
    newMutableHeaderType.setTransactionPhase(Enum.forString(transactionPhase));
    if (segmentNumber != null) {
      newMutableHeaderType.setSegmentNumber(segmentNumber);
    }

    return newMutableHeaderType;
  }

  /**
   * Creates a new <code>SegmentNumber</code> XML object
   *
   * @param segmentNumber the segment number
   * @param lastSegment is the last segment?
   * @return the <code>SegmentNumber</code> XML object
   */
  public static SegmentNumber createSegmentNumber(long segmentNumber, boolean lastSegment) {
    SegmentNumber newSegmentNumber = SegmentNumber.Factory.newInstance();
    newSegmentNumber.setLongValue(segmentNumber);
    newSegmentNumber.setLastSegment(lastSegment);

    return newSegmentNumber;
  }

  /**
   * Creates a new <code>StaticHeaderType</code> XML object
   *
   * @param hostId the host ID
   * @param nonce the random nonce
   * @param numSegments the segments number
   * @param partnerId the partner ID
   * @param product the <code>Product</code> element
   * @param securityMedium the security medium
   * @param userId the user Id
   * @param timestamp the current time stamp
   * @param orderDetails the <code>StaticHeaderOrderDetailsType</code> element
   * @param bankPubKeyDigests the <code>BankPubKeyDigests</code> element
   * @return the <code>StaticHeaderType</code> XML object
   */
  public static StaticHeaderType createStaticHeaderType(
      String hostId,
      byte[] nonce,
      int numSegments,
      String partnerId,
      Product product,
      String securityMedium,
      String userId,
      Calendar timestamp,
      StaticHeaderOrderDetailsType orderDetails,
      BankPubKeyDigests bankPubKeyDigests) {
    StaticHeaderType newStaticHeaderType = StaticHeaderType.Factory.newInstance();
    newStaticHeaderType.setHostID(hostId);
    newStaticHeaderType.setNonce(nonce);
    newStaticHeaderType.setNumSegments(numSegments);
    newStaticHeaderType.setPartnerID(partnerId);
    newStaticHeaderType.setProduct(product);
    newStaticHeaderType.setSecurityMedium(securityMedium);
    newStaticHeaderType.setUserID(userId);
    newStaticHeaderType.setTimestamp(timestamp);
    newStaticHeaderType.setOrderDetails(orderDetails);
    newStaticHeaderType.setBankPubKeyDigests(bankPubKeyDigests);

    return newStaticHeaderType;
  }

  /**
   * Creates a new <code>StaticHeaderType</code> XML object
   *
   * @param hostId the host ID
   * @param nonce the random nonce
   * @param numSegments the segments number
   * @param partnerId the partner ID
   * @param product the <code>Product</code> element
   * @param securityMedium the security medium
   * @param userId the user Id
   * @param timestamp the current time stamp
   * @param orderDetails the <code>StaticHeaderOrderDetailsType</code> element
   * @param bankPubKeyDigests the <code>BankPubKeyDigests</code> element
   * @return the <code>StaticHeaderType</code> XML object
   */
  public static StaticHeaderType createStaticHeaderType(
      String hostId,
      byte[] nonce,
      String partnerId,
      Product product,
      String securityMedium,
      String userId,
      Calendar timestamp,
      StaticHeaderOrderDetailsType orderDetails,
      BankPubKeyDigests bankPubKeyDigests) {
    StaticHeaderType newStaticHeaderType = StaticHeaderType.Factory.newInstance();
    newStaticHeaderType.setHostID(hostId);
    newStaticHeaderType.setNonce(nonce);
    newStaticHeaderType.setPartnerID(partnerId);
    newStaticHeaderType.setProduct(product);
    newStaticHeaderType.setSecurityMedium(securityMedium);
    newStaticHeaderType.setUserID(userId);
    newStaticHeaderType.setTimestamp(timestamp);
    newStaticHeaderType.setOrderDetails(orderDetails);
    newStaticHeaderType.setBankPubKeyDigests(bankPubKeyDigests);

    return newStaticHeaderType;
  }

  /**
   * Creates a new <code>StaticHeaderOrderDetailsType</code> XML object
   *
   * @param orderId the order ID
   * @param orderAttribute the order attribute
   * @param orderType the order type
   * @param orderParams the <code>FULOrderParamsType</code> element
   * @return the <code>StaticHeaderOrderDetailsType</code> XML object
   */
  public static StaticHeaderOrderDetailsType createStaticHeaderOrderDetailsType(
      String orderId, String orderAttribute, OrderType orderType, FULOrderParamsType orderParams) {
    StaticHeaderOrderDetailsType newStaticHeaderOrderDetailsType =
        StaticHeaderOrderDetailsType.Factory.newInstance();
    if (orderId != null) {
      newStaticHeaderOrderDetailsType.setOrderID(orderId);
    }
    newStaticHeaderOrderDetailsType.setOrderAttribute(
        com.axelor.apps.account.ebics.schema.h003.OrderAttributeType.Enum.forString(
            orderAttribute));
    newStaticHeaderOrderDetailsType.setOrderType(orderType);
    newStaticHeaderOrderDetailsType.setOrderParams(orderParams);
    qualifySubstitutionGroup(
        newStaticHeaderOrderDetailsType.getOrderParams(),
        FULOrderParamsDocument.type.getDocumentElementName(),
        null);

    return newStaticHeaderOrderDetailsType;
  }

  /**
   * Creates a new <code>StaticHeaderOrderDetailsType</code> XML object
   *
   * @param orderId the order ID
   * @param orderAttribute the order attribute
   * @param orderType the order type
   * @param orderParams the <code>FDLOrderParamsType</code> element
   * @return the <code>StaticHeaderOrderDetailsType</code> XML object
   */
  public static StaticHeaderOrderDetailsType createStaticHeaderOrderDetailsType(
      String orderId, String orderAttribute, OrderType orderType, FDLOrderParamsType orderParams) {
    StaticHeaderOrderDetailsType newStaticHeaderOrderDetailsType =
        StaticHeaderOrderDetailsType.Factory.newInstance();

    if (orderId != null) {
      // newStaticHeaderOrderDetailsType.setOrderID(orderId);
    }
    newStaticHeaderOrderDetailsType.setOrderAttribute(
        com.axelor.apps.account.ebics.schema.h003.OrderAttributeType.Enum.forString(
            orderAttribute));
    newStaticHeaderOrderDetailsType.setOrderType(orderType);
    newStaticHeaderOrderDetailsType.setOrderParams(orderParams);
    qualifySubstitutionGroup(
        newStaticHeaderOrderDetailsType.getOrderParams(),
        FDLOrderParamsDocument.type.getDocumentElementName(),
        null);

    return newStaticHeaderOrderDetailsType;
  }

  /**
   * Creates a new <code>StaticHeaderOrderDetailsType</code> XML object
   *
   * @param orderId the order ID
   * @param orderAttribute the order attribute
   * @param orderType the order type
   * @param orderParams the <code>StandardOrderParamsType</code> element
   * @return the <code>StaticHeaderOrderDetailsType</code> XML object
   */
  public static StaticHeaderOrderDetailsType createStaticHeaderOrderDetailsType(
      String orderId,
      String orderAttribute,
      OrderType orderType,
      StandardOrderParamsType orderParams) {
    StaticHeaderOrderDetailsType newStaticHeaderOrderDetailsType =
        StaticHeaderOrderDetailsType.Factory.newInstance();
    newStaticHeaderOrderDetailsType.setOrderID(orderId);
    newStaticHeaderOrderDetailsType.setOrderAttribute(
        com.axelor.apps.account.ebics.schema.h003.OrderAttributeType.Enum.forString(
            orderAttribute));
    newStaticHeaderOrderDetailsType.setOrderType(orderType);
    newStaticHeaderOrderDetailsType.setOrderParams(orderParams);
    qualifySubstitutionGroup(
        newStaticHeaderOrderDetailsType.getOrderParams(),
        StandardOrderParamsDocument.type.getDocumentElementName(),
        null);

    return newStaticHeaderOrderDetailsType;
  }

  /**
   * Creates a new <code>FULOrderParamsType</code> XML object
   *
   * @param fileFormat the <code>FileFormatType</code> element
   * @return the <code>FULOrderParamsType</code> XML object
   */
  public static FULOrderParamsType createFULOrderParamsType(FileFormatType fileFormat) {
    FULOrderParamsType newFULOrderParamsType = FULOrderParamsType.Factory.newInstance();
    newFULOrderParamsType.setFileFormat(fileFormat);

    return newFULOrderParamsType;
  }

  /**
   * Creates a new <code>FDLOrderParamsType</code> XML object
   *
   * @param fileFormat the <code>FileFormatType</code> element
   * @return the <code>FDLOrderParamsType</code> XML object
   */
  public static FDLOrderParamsType createFDLOrderParamsType(FileFormatType fileFormat) {
    FDLOrderParamsType newFDLOrderParamsType = FDLOrderParamsType.Factory.newInstance();
    newFDLOrderParamsType.setFileFormat(fileFormat);

    return newFDLOrderParamsType;
  }

  /**
   * Creates a new <code>StandardOrderParamsType</code> XML object
   *
   * @param fileFormat the <code>FileFormatType</code> element
   * @return the <code>StandardOrderParamsType</code> XML object
   */
  public static StandardOrderParamsType createStandardOrderParamsType() {
    StandardOrderParamsType newStandardOrderParamsType =
        StandardOrderParamsType.Factory.newInstance();

    return newStandardOrderParamsType;
  }

  /**
   * Creates a new <code>DateRange</code> XML object
   *
   * @param start the start range
   * @param end the end range
   * @return the <code>DateRange</code> XML object
   */
  public static DateRange createDateRange(Date start, Date end) {
    DateRange newDateRange = DateRange.Factory.newInstance();
    Calendar startRange = Calendar.getInstance();
    Calendar endRange = Calendar.getInstance();

    startRange.setTime(start);
    endRange.setTime(end);
    newDateRange.setStart(startRange);
    newDateRange.setEnd(endRange);

    return newDateRange;
  }

  /**
   * Creates a new <code>FileFormatType</code> XML object
   *
   * @param countryCode the country code
   * @param value the file format value
   * @return the <code>FileFormatType</code> XML object
   */
  public static FileFormatType createFileFormatType(String countryCode, String value) {
    FileFormatType newFileFormatType = FileFormatType.Factory.newInstance();
    newFileFormatType.setCountryCode(countryCode);
    newFileFormatType.setStringValue(value);

    return newFileFormatType;
  }

  /**
   * Creates a new <code>Parameter</code> XML object
   *
   * @param name the parameter name
   * @param value the parameter value
   * @return the <code>Parameter</code> XML object
   */
  public static Parameter createParameter(String name, Value value) {
    Parameter newParameter = Parameter.Factory.newInstance();
    newParameter.setName(name);
    newParameter.setValue(value);

    return newParameter;
  }

  /**
   * Creates a new <code>Value</code> XML object
   *
   * @param type the value type
   * @param value the value
   * @return the <code>Value</code> XML object
   */
  public static Value createValue(String type, String value) {
    Value newValue = Value.Factory.newInstance();
    newValue.setType(type);
    newValue.setStringValue(value);

    return newValue;
  }

  /**
   * Create the <code>OrderType</code> XML object
   *
   * @param orderType the order type
   * @return the <code>OrderType</code> XML object
   */
  public static OrderType createOrderType(String orderType) {
    OrderType newOrderType = OrderType.Factory.newInstance();
    newOrderType.setStringValue(orderType);

    return newOrderType;
  }

  /**
   * Create the <code>Product</code> XML object
   *
   * @param language the product language
   * @param value the product value
   * @return the <code>Product</code> XML object
   */
  public static Product createProduct(String language, String value) {
    Product newProduct = Product.Factory.newInstance();
    newProduct.setLanguage(language);
    newProduct.setStringValue(value);

    return newProduct;
  }

  /**
   * Create the <code>BankPubKeyDigests</code> XML object
   *
   * @param authentication the <code>Authentication</code> element
   * @param encryption the <code>Encryption</code> element
   * @return the <code>BankPubKeyDigests</code> XML object
   */
  public static BankPubKeyDigests createBankPubKeyDigests(
      Authentication authentication, Encryption encryption) {
    BankPubKeyDigests newBankPubKeyDigests = BankPubKeyDigests.Factory.newInstance();
    newBankPubKeyDigests.setAuthentication(authentication);
    newBankPubKeyDigests.setEncryption(encryption);

    return newBankPubKeyDigests;
  }

  /**
   * Create the <code>Authentication</code> XML object
   *
   * @param version the authentication version
   * @param algorithm the authentication algorithm
   * @param value the authentication value
   * @return the <code>Authentication</code> XML object
   */
  public static Authentication createAuthentication(
      String version, String algorithm, byte[] value) {
    Authentication newAuthentication = Authentication.Factory.newInstance();
    newAuthentication.setVersion(version);
    newAuthentication.setAlgorithm(algorithm);
    newAuthentication.setByteArrayValue(value);

    return newAuthentication;
  }

  /**
   * Create the <code>Encryption</code> XML object
   *
   * @param version the encryption version
   * @param algorithm the encryption algorithm
   * @param value the encryption value
   * @return the <code>Encryption</code> XML object
   */
  public static Encryption createEncryption(String version, String algorithm, byte[] value) {
    Encryption newEncryption = Encryption.Factory.newInstance();
    newEncryption.setVersion(version);
    newEncryption.setAlgorithm(algorithm);
    newEncryption.setByteArrayValue(value);

    return newEncryption;
  }

  /**
   * Create the <code>
   * com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body</code> XML
   * object
   *
   * @param dataTransfer the <code>DataTransferRequestType</code> element
   * @return the <code>
   *     com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body</code> XML
   *     object
   */
  // TODO COMPLETE
  public static com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body
      createEbicsRequestBody(DataTransferRequestType dataTransfer, PreValidation preValidation) {
    com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body newBody =
        com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body.Factory
            .newInstance();
    newBody.setDataTransfer(dataTransfer);
    newBody.setPreValidation(preValidation);
    return newBody;
  }

  /**
   * Create the <code>
   * com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body</code> XML
   * object
   *
   * @param dataTransfer the <code>DataTransferRequestType</code> element
   * @return the <code>
   *     com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body</code> XML
   *     object
   */
  public static com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body
      createEbicsRequestBody(DataTransferRequestType dataTransfer) {
    com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body newBody =
        com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body.Factory
            .newInstance();
    newBody.setDataTransfer(dataTransfer);
    return newBody;
  }

  /**
   * Create the <code>
   * com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body</code> XML
   * object
   *
   * @return the <code>
   *     com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body</code> XML
   *     object
   */
  public static com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body
      createEbicsRequestBody() {
    com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body newBody =
        com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body.Factory
            .newInstance();

    return newBody;
  }

  /**
   * Create the <code>
   * com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body</code> XML
   * object
   *
   * @param transferReceipt the <code>TransferReceipt</code> element
   * @return the <code>
   *     com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body</code> XML
   *     object
   */
  public static com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body
      createEbicsRequestBody(TransferReceipt transferReceipt) {
    com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body newBody =
        com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body.Factory
            .newInstance();
    newBody.setTransferReceipt(transferReceipt);

    return newBody;
  }

  /**
   * Create the <code>DataTransferRequestType</code> XML object
   *
   * @param dataEncryptionInfo the <code>DataEncryptionInfo</code> element
   * @param signatureData the <code>SignatureData</code> element
   * @return the <code>DataTransferRequestType</code> XML object
   */
  public static DataTransferRequestType createDataTransferRequestType(
      DataEncryptionInfo dataEncryptionInfo, SignatureData signatureData) {
    DataTransferRequestType newDataTransferRequestType =
        DataTransferRequestType.Factory.newInstance();
    newDataTransferRequestType.setDataEncryptionInfo(dataEncryptionInfo);
    newDataTransferRequestType.setSignatureData(signatureData);

    return newDataTransferRequestType;
  }

  /**
   * Create the <code>StaticHeaderType</code> XML object
   *
   * @param hostId the host ID
   * @param transactionId the transaction ID
   * @return the <code>StaticHeaderType</code> XML object
   */
  public static StaticHeaderType createStaticHeaderType(String hostId, byte[] transactionId) {
    StaticHeaderType newStaticHeaderType = StaticHeaderType.Factory.newInstance();
    newStaticHeaderType.setHostID(hostId);
    newStaticHeaderType.setTransactionID(transactionId);

    return newStaticHeaderType;
  }

  /**
   * Create the <code>DataTransferRequestType</code> XML object
   *
   * @param orderData the <code>
   *     com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData</code> element
   * @return the <code>DataTransferRequestType</code> XML object
   */
  public static DataTransferRequestType createDataTransferRequestType(
      com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData orderData) {
    DataTransferRequestType newDataTransferRequestType =
        DataTransferRequestType.Factory.newInstance();
    newDataTransferRequestType.setOrderData(orderData);

    return newDataTransferRequestType;
  }

  /**
   * Create the <code>com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData
   * </code> XML object
   *
   * @param orderDataValue the order data value
   * @return the <code>com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData
   *     </code> XML object
   */
  public static com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData
      createTransferRequestTypeOrderData(byte[] orderDataValue) {
    com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData newOrderData =
        com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData.Factory
            .newInstance();
    newOrderData.setByteArrayValue(orderDataValue);

    return newOrderData;
  }

  /**
   * Create the <code>DataEncryptionInfo</code> XML object
   *
   * @param authenticate should authenticate?
   * @param encryptionPubKeyDigest the <code>EncryptionPubKeyDigest</code> element
   * @param transactionKey the transaction key
   * @return the the <code>DataEncryptionInfo</code> XML object
   */
  public static DataEncryptionInfo createDataEncryptionInfo(
      boolean authenticate, EncryptionPubKeyDigest encryptionPubKeyDigest, byte[] transactionKey) {
    DataEncryptionInfo newDataEncryptionInfo = DataEncryptionInfo.Factory.newInstance();
    newDataEncryptionInfo.setAuthenticate(authenticate);
    newDataEncryptionInfo.setEncryptionPubKeyDigest(encryptionPubKeyDigest);
    newDataEncryptionInfo.setTransactionKey(transactionKey);

    return newDataEncryptionInfo;
  }

  /**
   * Create the <code>EncryptionPubKeyDigest</code> XML object
   *
   * @param version the encryption version
   * @param algorithm the encryption algorithm
   * @param value the encryption value
   * @return the <code>EncryptionPubKeyDigest</code> XML object
   */
  public static EncryptionPubKeyDigest createEncryptionPubKeyDigest(
      String version, String algorithm, byte[] value) {
    EncryptionPubKeyDigest newEncryptionPubKeyDigest = EncryptionPubKeyDigest.Factory.newInstance();
    newEncryptionPubKeyDigest.setVersion(version);
    newEncryptionPubKeyDigest.setAlgorithm(algorithm);
    newEncryptionPubKeyDigest.setByteArrayValue(value);

    return newEncryptionPubKeyDigest;
  }

  /**
   * Create the <code>com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData
   * </code> XML object
   *
   * @param oderData the order data value
   * @return the the <code>
   *     com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData</code> XML
   *     object
   */
  public static com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData
      createEbicsRequestOrderData(byte[] oderData) {
    com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData newOrderData =
        com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData.Factory
            .newInstance();
    newOrderData.setByteArrayValue(oderData);

    return newOrderData;
  }

  /**
   * Create the <code>SignatureData</code> XML object
   *
   * @param authenticate should authenticate?
   * @param signatureData the signature data value
   * @return the <code>SignatureData</code> XML object
   */
  public static SignatureData createSignatureData(boolean authenticate, byte[] signatureData) {
    SignatureData newSignatureData = SignatureData.Factory.newInstance();
    newSignatureData.setAuthenticate(authenticate);
    newSignatureData.setByteArrayValue(signatureData);

    return newSignatureData;
  }

  /**
   * Create the <code>TransferReceipt</code> XML object
   *
   * @param authenticate should authenticate?
   * @param receiptCode the receipt code
   * @return the <code>TransferReceipt</code> XML object
   */
  public static TransferReceipt createTransferReceipt(boolean authenticate, int receiptCode) {
    TransferReceipt newTransferReceipt = TransferReceipt.Factory.newInstance();
    newTransferReceipt.setAuthenticate(authenticate);
    newTransferReceipt.setReceiptCode(receiptCode);

    return newTransferReceipt;
  }

  /**
   * Qualifies a valid member of a substitution group. This method tries to use the built-in {@link
   * XmlObject#substitute(QName, SchemaType)} and if succesful returns a valid substitution which is
   * usable (not disconnected). If it fails, it uses low-level {@link XmlCursor} manipulation to
   * qualify the substitution group. Note that if the latter is the case the resulting document is
   * disconnected and should no longer be manipulated. Thus, use it as a final step after all markup
   * is included.
   *
   * <p>If newType is null, this method will skip {@link XmlObject#substitute(QName, SchemaType)}
   * and directly use {@link XmlCursor}. This can be used, if you are sure that the substitute is
   * not in the list of (pre-compiled) valid substitutions (this is the case if a schema uses
   * another schema's type as a base for elements. E.g. om:Observation uses gml:_Feature as the base
   * type).
   *
   * @param xobj the abstract element
   * @param newInstance the new {@link QName} of the instance
   * @param newType the new schemaType. if null, cursors will be used and the resulting object will
   *     be disconnected.
   * @return if successful applied {@link XmlObject#substitute(QName, SchemaType)} a living object
   *     with a type == newType is returned. Otherwise null is returned as you can no longer
   *     manipulate the object.
   */
  public static XmlObject qualifySubstitutionGroup(
      XmlObject xobj, QName newInstance, SchemaType newType) {
    XmlObject substitute = null;

    if (newType != null) {
      substitute = xobj.substitute(newInstance, newType);
      if (substitute != null
          && substitute.schemaType() == newType
          && substitute.getDomNode().getLocalName().equals(newInstance.getLocalPart())) {
        return substitute;
      }
    }

    XmlCursor cursor = xobj.newCursor();
    cursor.setName(newInstance);
    QName qName = new QName("http://www.w3.org/2001/XMLSchema-instance", "type");
    cursor.removeAttribute(qName);
    cursor.toNextToken();
    if (cursor.isNamespace()) {
      cursor.removeXml();
    }

    cursor.dispose();

    return null;
  }
}
