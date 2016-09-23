/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package com.axelor.apps.account.ebics.xml;

import java.util.Calendar;

import javax.crypto.spec.SecretKeySpec;

import org.jdom.JDOMException;

import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType;
import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.StandardOrderParamsType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderOrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.DataEncryptionInfoType.EncryptionPubKeyDigest;
import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.DataEncryptionInfo;
import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.SignatureData;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderOrderDetailsType.OrderType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.Product;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests.Authentication;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests.Encryption;
import com.axelor.apps.account.ebics.client.EbicsSession;
import com.axelor.apps.account.ebics.client.EbicsUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;


/**
 * The <code>SPRRequestElement</code> is the request element
 * for revoking a subscriber
 *
 * @author Hachani
 *
 */
public class SPRRequestElement extends InitializationRequestElement {

  /**
   * Constructs a new SPR request element.
   * @param session the current ebic session.
   */
  public SPRRequestElement(EbicsSession session) throws AxelorException {
    super(session, com.axelor.apps.account.ebics.client.OrderType.SPR, "SPRRequest.xml");
    keySpec = new SecretKeySpec(nonce, "EAS");
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
    OrderType 				orderType;
    StandardOrderParamsType		standardOrderParamsType;
    UserSignature			userSignature;

    userSignature = new UserSignature(session.getUser(),
				      generateName("SIG"),
	                              session.getConfiguration().getSignatureVersion(),
	                              " ".getBytes());
    userSignature.build();
    userSignature.validate();

    mutable = EbicsXmlFactory.createMutableHeaderType("Initialisation", null);
    product = EbicsXmlFactory.createProduct(session.getProduct().getLanguage(), session.getProduct().getName());
    authentication = EbicsXmlFactory.createAuthentication(session.getConfiguration().getAuthenticationVersion(),
	                                                  "http://www.w3.org/2001/04/xmlenc#sha256",
	                                                  decodeHex( session.getUser().getEbicsPartner().getEbicsBank().getX002Digest().getBytes() ) );
    encryption = EbicsXmlFactory.createEncryption(session.getConfiguration().getEncryptionVersion(),
	                                          "http://www.w3.org/2001/04/xmlenc#sha256",
	                                          decodeHex(session.getUser().getEbicsPartner().getEbicsBank().getE002Digest().getBytes()));
    bankPubKeyDigests = EbicsXmlFactory.createBankPubKeyDigests(authentication, encryption);
    orderType = EbicsXmlFactory.createOrderType(type.getOrderType());
    standardOrderParamsType = EbicsXmlFactory.createStandardOrderParamsType();
    orderDetails = EbicsXmlFactory.createStaticHeaderOrderDetailsType(session.getUser().getEbicsPartner().getNextOrderId(),
	                                                              "UZHNN",
	                                                              orderType,
	                                                              standardOrderParamsType);
    xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(),
	                                             nonce,
	                                             0,
	                                             session.getUser().getEbicsPartner().getPartnerId(),
	                                             product,
	                                             session.getUser().getSecurityMedium(),
	                                             session.getUser().getUserId(),
	                                             Calendar.getInstance(),
	                                             orderDetails,
	                                             bankPubKeyDigests);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    encryptionPubKeyDigest = EbicsXmlFactory.createEncryptionPubKeyDigest(session.getConfiguration().getEncryptionVersion(),
								          "http://www.w3.org/2001/04/xmlenc#sha256",
								          decodeHex(session.getUser().getEbicsPartner().getEbicsBank().getE002Digest().getBytes()));
    try {
		signatureData = EbicsXmlFactory.createSignatureData(true, EbicsUtils.encrypt(EbicsUtils.zip(userSignature.prettyPrint()), keySpec));
	} catch (JDOMException e) {
		throw new AxelorException(e.getMessage(), IException.CONFIGURATION_ERROR);
	}
    dataEncryptionInfo = EbicsXmlFactory.createDataEncryptionInfo(true,
	                                                          encryptionPubKeyDigest,
	                                                          generateTransactionKey());
    dataTransfer = EbicsXmlFactory.createDataTransferRequestType(dataEncryptionInfo, signatureData);
    body = EbicsXmlFactory.createEbicsRequestBody(dataTransfer);
    request = EbicsXmlFactory.createEbicsRequest(session.getConfiguration().getRevision(),
	                                         session.getConfiguration().getVersion(),
	                                         header,
	                                         body);
    document = EbicsXmlFactory.createEbicsRequestDocument(request);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private SecretKeySpec			keySpec;
  private static final long 		serialVersionUID = -6742241777786111337L;
}
