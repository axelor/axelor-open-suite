/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.crypto.spec.SecretKeySpec;

import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType;
import com.axelor.apps.account.ebics.schema.h003.FULOrderParamsType;
import com.axelor.apps.account.ebics.schema.h003.FileFormatType;
import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderOrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderOrderDetailsType.OrderType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.DataEncryptionInfoType.EncryptionPubKeyDigest;
import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.DataEncryptionInfo;
import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.SignatureData;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.ParameterDocument.Parameter;
import com.axelor.apps.account.ebics.schema.h003.ParameterDocument.Parameter.Value;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.Product;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests.Authentication;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests.Encryption;
import com.axelor.apps.bankpayment.ebics.certificate.KeyUtil;
import com.axelor.apps.bankpayment.ebics.client.EbicsSession;
import com.axelor.apps.bankpayment.ebics.client.EbicsUtils;
import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.apps.bankpayment.ebics.io.Splitter;
import com.axelor.exception.AxelorException;

/**
 * The <code>UInitializationRequestElement</code> is the common initialization
 * element for all ebics file uploads.
 *
 * @author Hachani
 *
 */
public class UInitializationRequestElement extends InitializationRequestElement {

  /**
   * Constructs a new <code>UInitializationRequestElement</code> for uploads initializations.
   * @param session the current ebics session.
   * @param orderType the upload order type
   * @param userData the user data to be uploaded
   * @throws EbicsException
   */
  public UInitializationRequestElement(EbicsSession session,
                                       com.axelor.apps.bankpayment.ebics.client.OrderType orderType,
                                       byte[] userData)
    throws AxelorException
  {
    super(session, orderType, generateName(orderType));
    this.userData = userData;
    keySpec = new SecretKeySpec(nonce, "EAS");
    splitter = new Splitter(userData);
  }

  @Override
  public void buildInitialization() throws AxelorException {
    EbicsRequest			request;
    Header 				header;
    Body				body;
    MutableHeaderType 			mutable;
    StaticHeaderType 			xstatic;
    Product 				product;
    BankPubKeyDigests 			bankPubKeyDigests;
    Authentication 			authentication;
    Encryption 				encryption;
    DataTransferRequestType 		dataTransfer;
    DataEncryptionInfo 			dataEncryptionInfo;
    SignatureData 			signatureData;
    EncryptionPubKeyDigest 		encryptionPubKeyDigest;
    StaticHeaderOrderDetailsType 	orderDetails;
    FULOrderParamsType			fULOrderParams;
    OrderType 				orderType;
    FileFormatType 			fileFormat;
    List<Parameter>			parameters;

    userSignature = new UserSignature(session.getUser(),
				      generateName("UserSignature"),
	                              "A005",
	                              userData);
    userSignature.build();
    userSignature.validate();

    splitter.readInput(true, keySpec);

    mutable = EbicsXmlFactory.createMutableHeaderType("Initialisation", null);
    product = EbicsXmlFactory.createProduct(session.getProduct().getLanguage(), session.getProduct().getName());
    authentication = EbicsXmlFactory.createAuthentication("X002",
	                                                  "http://www.w3.org/2001/04/xmlenc#sha256",
	                                                  decodeHex(KeyUtil.getKeyDigest(session.getBankX002Key())));
    encryption = EbicsXmlFactory.createEncryption("E002",
	                                          "http://www.w3.org/2001/04/xmlenc#sha256",
	                                          decodeHex(KeyUtil.getKeyDigest(session.getBankE002Key())));
    bankPubKeyDigests = EbicsXmlFactory.createBankPubKeyDigests(authentication, encryption);
    orderType = EbicsXmlFactory.createOrderType(type.getOrderType());
    fileFormat = EbicsXmlFactory.createFileFormatType(Locale.FRANCE.getCountry(),
	                                              session.getSessionParam("FORMAT"));
    fULOrderParams = EbicsXmlFactory.createFULOrderParamsType(fileFormat);
    parameters = new ArrayList<Parameter>();
    if (Boolean.valueOf(session.getSessionParam("TEST")).booleanValue()) {
      Parameter 		parameter;
      Value			value;

      value = EbicsXmlFactory.createValue("String", "TRUE");
      parameter = EbicsXmlFactory.createParameter("TEST", value);
      parameters.add(parameter);
    }

    if (Boolean.valueOf(session.getSessionParam("EBCDIC")).booleanValue()) {
      Parameter 		parameter;
      Value			value;

      value = EbicsXmlFactory.createValue("String", "TRUE");
      parameter = EbicsXmlFactory.createParameter("EBCDIC", value);
      parameters.add(parameter);
    }

    if (parameters.size() > 0) {
      fULOrderParams.setParameterArray(parameters.toArray(new Parameter[parameters.size()]));
    }

    orderDetails = EbicsXmlFactory.createStaticHeaderOrderDetailsType(session.getUser().getNextOrderId(),
	                                                              "DZHNN",
	                                                              orderType,
	                                                              fULOrderParams);
    xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(),
	                                             nonce,
	                                             splitter.getSegmentNumber(),
	                                             session.getUser().getEbicsPartner().getPartnerId(),
	                                             product,
	                                             session.getUser().getSecurityMedium(),
	                                             session.getUser().getUserId(),
	                                             Calendar.getInstance(),
	                                             orderDetails,
	                                             bankPubKeyDigests);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    encryptionPubKeyDigest = EbicsXmlFactory.createEncryptionPubKeyDigest("E002",
								          "http://www.w3.org/2001/04/xmlenc#sha256",
								          decodeHex(KeyUtil.getKeyDigest(session.getBankE002Key())));
    signatureData = EbicsXmlFactory.createSignatureData(true, EbicsUtils.encrypt(EbicsUtils.zip(userSignature.prettyPrint()), keySpec));
    dataEncryptionInfo = EbicsXmlFactory.createDataEncryptionInfo(true,
	                                                          encryptionPubKeyDigest,
	                                                          generateTransactionKey());
    dataTransfer = EbicsXmlFactory.createDataTransferRequestType(dataEncryptionInfo, signatureData);
    body = EbicsXmlFactory.createEbicsRequestBody(dataTransfer);
    request = EbicsXmlFactory.createEbicsRequest(1,
	                                         "H003",
	                                         header,
	                                         body);
    document = EbicsXmlFactory.createEbicsRequestDocument(request);
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.ebics.org/H003", "");

    return super.toByteArray();
  }

  /**
   * Returns the user signature data.
   * @return the user signature data.
   */
  public UserSignature getUserSignature() {
    return userSignature;
  }

  /**
   * Returns the content of a given segment.
   * @param segment the segment number
   * @return the content of the given segment
   */
  public ContentFactory getContent(int segment) {
    return splitter.getContent(segment);
  }

  /**
   * Returns the total segment number.
   * @return the total segment number.
   */
  public int getSegmentNumber() {
    return splitter.getSegmentNumber();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private byte[]			userData;
  private UserSignature			userSignature;
  private SecretKeySpec			keySpec;
  private Splitter			splitter;
}
