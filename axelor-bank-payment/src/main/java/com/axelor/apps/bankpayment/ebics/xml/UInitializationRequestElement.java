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

import com.axelor.apps.account.ebics.schema.h003.DataEncryptionInfoType.EncryptionPubKeyDigest;
import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType;
import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.DataEncryptionInfo;
import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.SignatureData;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.FULOrderParamsType;
import com.axelor.apps.account.ebics.schema.h003.FileFormatType;
import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.ParameterDocument.Parameter;
import com.axelor.apps.account.ebics.schema.h003.ParameterDocument.Parameter.Value;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderOrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderOrderDetailsType.OrderType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests.Authentication;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests.Encryption;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.Product;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.EbicsPartnerRepository;
import com.axelor.apps.bankpayment.ebics.certificate.KeyUtil;
import com.axelor.apps.bankpayment.ebics.client.EbicsSession;
import com.axelor.apps.bankpayment.ebics.client.EbicsUtils;
import com.axelor.apps.bankpayment.ebics.client.OrderAttribute;
import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.apps.bankpayment.ebics.io.Splitter;
import com.axelor.exception.AxelorException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.XMLConstants;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>UInitializationRequestElement</code> is the common initialization element for all ebics
 * file uploads.
 *
 * @author Hachani
 */
public class UInitializationRequestElement extends InitializationRequestElement {

  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * Constructs a new <code>UInitializationRequestElement</code> for uploads initializations.
   *
   * @param session the current ebics session.
   * @param orderType the upload order type
   * @param userData the user data to be uploaded
   * @throws EbicsException
   */
  public UInitializationRequestElement(
      EbicsSession session,
      com.axelor.apps.bankpayment.ebics.client.OrderType orderType,
      byte[] userData,
      byte[] userSignatureData)
      throws AxelorException {
    super(session, orderType, generateName(orderType));
    this.userData = userData;
    this.userSignatureData = userSignatureData;
    keySpec = new SecretKeySpec(nonce, "EAS");
    splitter = new Splitter(userData);
  }

  @Override
  public void buildInitialization() throws AxelorException {
    EbicsRequest request;
    Header header;
    Body body;
    MutableHeaderType mutable;
    StaticHeaderType xstatic;
    Product product;
    BankPubKeyDigests bankPubKeyDigests;
    Authentication authentication;
    Encryption encryption;
    DataTransferRequestType dataTransfer;
    DataEncryptionInfo dataEncryptionInfo;
    SignatureData signatureData;
    EncryptionPubKeyDigest encryptionPubKeyDigest;
    StaticHeaderOrderDetailsType orderDetails;
    FULOrderParamsType fULOrderParams;
    OrderType orderType;
    FileFormatType fileFormat;
    List<Parameter> parameters;

    EbicsUser ebicsUser = session.getUser();
    EbicsPartner ebicsPartner = ebicsUser.getEbicsPartner();

    if (ebicsPartner.getEbicsTypeSelect() == EbicsPartnerRepository.EBICS_TYPE_TS) {

      EbicsUser signatoryUser = session.getSignatoryUser();

      userSignature =
          new UserSignature(
              signatoryUser, generateName("UserSignature"), "A005", userData, userSignatureData);
    } else {
      userSignature =
          new UserSignature(ebicsUser, generateName("UserSignature"), "A005", userData, null);
    }

    userSignature.build();

    log.debug("user signature pretty print : {}", userSignature.toString());

    userSignature.validate();

    log.debug("user signature pretty print : {}", userSignature.toString());

    splitter.readInput(true, keySpec);

    mutable = EbicsXmlFactory.createMutableHeaderType("Initialisation", null);
    product =
        EbicsXmlFactory.createProduct(
            session.getProduct().getLanguage(), session.getProduct().getName());
    authentication =
        EbicsXmlFactory.createAuthentication(
            "X002",
            "http://www.w3.org/2001/04/xmlenc#sha256",
            decodeHex(KeyUtil.getKeyDigest(session.getBankX002Key())));
    encryption =
        EbicsXmlFactory.createEncryption(
            "E002",
            "http://www.w3.org/2001/04/xmlenc#sha256",
            decodeHex(KeyUtil.getKeyDigest(session.getBankE002Key())));
    bankPubKeyDigests = EbicsXmlFactory.createBankPubKeyDigests(authentication, encryption);
    orderType = EbicsXmlFactory.createOrderType(type.getOrderType());
    fileFormat =
        EbicsXmlFactory.createFileFormatType(
            Locale.FRANCE.getCountry(), session.getSessionParam("FORMAT"));
    fULOrderParams = EbicsXmlFactory.createFULOrderParamsType(fileFormat);
    parameters = new ArrayList<Parameter>();
    if (Boolean.valueOf(session.getSessionParam("TEST")).booleanValue()) {
      Parameter parameter;
      Value value;

      value = EbicsXmlFactory.createValue("String", "TRUE");
      parameter = EbicsXmlFactory.createParameter("TEST", value);
      parameters.add(parameter);
    }

    if (Boolean.valueOf(session.getSessionParam("EBCDIC")).booleanValue()) {
      Parameter parameter;
      Value value;

      value = EbicsXmlFactory.createValue("String", "TRUE");
      parameter = EbicsXmlFactory.createParameter("EBCDIC", value);
      parameters.add(parameter);
    }

    if (parameters.size() > 0) {
      fULOrderParams.setParameterArray(parameters.toArray(new Parameter[parameters.size()]));
    }

    OrderAttribute orderAttribute = new OrderAttribute(type, ebicsPartner.getEbicsTypeSelect());
    orderAttribute.build();

    orderDetails =
        EbicsXmlFactory.createStaticHeaderOrderDetailsType(
            ebicsUser.getNextOrderId(),
            orderAttribute.getOrderAttributes(),
            orderType,
            fULOrderParams);
    xstatic =
        EbicsXmlFactory.createStaticHeaderType(
            session.getBankID(),
            nonce,
            splitter.getSegmentNumber(),
            ebicsPartner.getPartnerId(),
            product,
            ebicsUser.getSecurityMedium(),
            ebicsUser.getUserId(),
            Calendar.getInstance(),
            orderDetails,
            bankPubKeyDigests);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    encryptionPubKeyDigest =
        EbicsXmlFactory.createEncryptionPubKeyDigest(
            "E002",
            "http://www.w3.org/2001/04/xmlenc#sha256",
            decodeHex(KeyUtil.getKeyDigest(session.getBankE002Key())));

    System.out.println(
        "signature ----------------------------------------------------------------------------");
    System.out.println(userSignature.toString());

    // USE PREVALIDATION
    //    PreValidation preValidation = PreValidation.Factory.newInstance();
    //    preValidation.setAuthenticate(true);
    //    DataDigestType dataDigest = DataDigestType.Factory.newInstance();
    //    dataDigest.setSignatureVersion("A005");
    //    dataDigest.setStringValue("XXXXXXX);
    //    preValidation.setDataDigestArray(new DataDigestType[] {dataDigest});

    signatureData =
        EbicsXmlFactory.createSignatureData(
            true, EbicsUtils.encrypt(EbicsUtils.zip(userSignature.prettyPrint()), keySpec));

    dataEncryptionInfo =
        EbicsXmlFactory.createDataEncryptionInfo(
            true, encryptionPubKeyDigest, generateTransactionKey());
    dataTransfer = EbicsXmlFactory.createDataTransferRequestType(dataEncryptionInfo, signatureData);

    // USE PREVALIDATION
    //    body = EbicsXmlFactory.createEbicsRequestBody(dataTransfer, preValidation);

    body = EbicsXmlFactory.createEbicsRequestBody(dataTransfer);

    request = EbicsXmlFactory.createEbicsRequest(1, "H003", header, body);
    document = EbicsXmlFactory.createEbicsRequestDocument(request);

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    try {
      this.save(bout);
    } catch (JDOMException e) {
      // TODO Bloc catch généré automatiquement
      e.printStackTrace();
    }

    System.out.println(
        "Requete signature ----------------------------------------------------------------------------");
    System.out.println(bout.toString());
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.ebics.org/H003", XMLConstants.DEFAULT_NS_PREFIX);

    return super.toByteArray();
  }

  /**
   * Returns the user signature data.
   *
   * @return the user signature data.
   */
  public UserSignature getUserSignature() {
    return userSignature;
  }

  /**
   * Returns the content of a given segment.
   *
   * @param segment the segment number
   * @return the content of the given segment
   */
  public ContentFactory getContent(int segment) {
    return splitter.getContent(segment);
  }

  /**
   * Returns the total segment number.
   *
   * @return the total segment number.
   */
  public int getSegmentNumber() {
    return splitter.getSegmentNumber();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private byte[] userData;
  private byte[] userSignatureData;
  private UserSignature userSignature;
  private SecretKeySpec keySpec;
  private Splitter splitter;
}
