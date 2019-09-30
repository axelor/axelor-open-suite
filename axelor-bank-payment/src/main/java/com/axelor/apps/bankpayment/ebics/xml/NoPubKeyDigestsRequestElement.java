/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument;
import com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.EmptyMutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.NoPubKeyDigestsRequestStaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.OrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.ProductElementType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureType;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.ebics.client.EbicsSession;
import com.axelor.apps.bankpayment.ebics.client.EbicsUtils;
import com.axelor.apps.bankpayment.ebics.client.OrderAttribute;
import com.axelor.apps.bankpayment.ebics.client.OrderType;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Calendar;

/**
 * The <code>NoPubKeyDigestsRequestElement</code> is the root element for a HPB ebics server
 * request.
 *
 * @author hachani
 */
public class NoPubKeyDigestsRequestElement extends DefaultEbicsRootElement {

  /**
   * Construct a new No Public Key Digests Request element.
   *
   * @param session the current ebics session.
   */
  public NoPubKeyDigestsRequestElement(EbicsSession session) {
    super(session);
  }

  /**
   * Returns the digest value of the authenticated XML portions.
   *
   * @return the digest value.
   * @throws EbicsException Failed to retrieve the digest value.
   */
  public byte[] getDigest() throws AxelorException {
    addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");

    try {
      return MessageDigest.getInstance("SHA-256", "BC").digest(EbicsUtils.canonize(toByteArray()));
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new AxelorException(e.getCause(), TraceBackRepository.TYPE_TECHNICAL, e.getMessage());
    }
  }

  /**
   * Sets the authentication signature of the <code>NoPubKeyDigestsRequestElement</code>
   *
   * @param authSignature the the authentication signature.
   */
  public void setAuthSignature(SignatureType authSignature) {
    ((EbicsNoPubKeyDigestsRequestDocument) document)
        .getEbicsNoPubKeyDigestsRequest()
        .setAuthSignature(authSignature);
  }

  /**
   * Sets the signature value of the request.
   *
   * @param signature the signature value
   */
  public void setSignatureValue(byte[] signature) {
    ((EbicsNoPubKeyDigestsRequestDocument) document)
        .getEbicsNoPubKeyDigestsRequest()
        .getAuthSignature()
        .setSignatureValue(EbicsXmlFactory.createSignatureValueType(signature));
  }

  @Override
  public void build() throws AxelorException {
    EbicsNoPubKeyDigestsRequest request;
    Body body;
    Header header;
    EmptyMutableHeaderType mutable;
    NoPubKeyDigestsRequestStaticHeaderType xstatic;
    ProductElementType product;
    OrderDetailsType orderDetails;

    EbicsUser ebicsUser = session.getUser();
    EbicsPartner ebicsPartner = ebicsUser.getEbicsPartner();

    OrderAttribute orderAttribute =
        new OrderAttribute(OrderType.HPB, ebicsPartner.getEbicsTypeSelect());
    orderAttribute.build();

    product =
        EbicsXmlFactory.creatProductElementType(
            session.getProduct().getLanguage(), session.getProduct().getName());
    orderDetails =
        EbicsXmlFactory.createOrderDetailsType(
            orderAttribute.getOrderAttributes(), null, OrderType.HPB.getOrderType());
    xstatic =
        EbicsXmlFactory.createNoPubKeyDigestsRequestStaticHeaderType(
            session.getBankID(),
            EbicsUtils.generateNonce(),
            Calendar.getInstance(),
            ebicsPartner.getPartnerId(),
            ebicsUser.getUserId(),
            product,
            orderDetails,
            ebicsUser.getSecurityMedium());
    mutable = EbicsXmlFactory.createEmptyMutableHeaderType();
    header = EbicsXmlFactory.createDigestsRequestHeader(true, mutable, xstatic);
    body = EbicsXmlFactory.createDigestsRequestBody();
    request = EbicsXmlFactory.createEbicsNoPubKeyDigestsRequest(1, "H003", header, body);
    document = EbicsXmlFactory.createEbicsNoPubKeyDigestsRequestDocument(request);
  }

  @Override
  public byte[] toByteArray() {
    //    setSaveSuggestedPrefixes("http://www.w3.org/2000/09/xmldsig#", "ds");  //TODO TO CHECK

    setSaveSuggestedPrefixes("http://www.w3.org/2000/09/xmldsig#", "ds");
    setSaveSuggestedPrefixes("http://www.ebics.org/H003", "");

    //
    //    addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");
    //
    //    setSaveSuggestedPrefixes("http://www.ebics.org/H003", XMLConstants.DEFAULT_NS_PREFIX);

    return super.toByteArray();
  }

  @Override
  public String getName() {
    return "NoPubKeyDigestsRequest.xml";
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

}
