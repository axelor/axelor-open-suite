/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.ebics.xml;

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

import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.FDLOrderParamsType;
import com.axelor.apps.account.ebics.schema.h003.FDLOrderParamsType.DateRange;
import com.axelor.apps.account.ebics.schema.h003.FileFormatType;
import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.ParameterDocument.Parameter;
import com.axelor.apps.account.ebics.schema.h003.ParameterDocument.Parameter.Value;
import com.axelor.apps.account.ebics.schema.h003.StandardOrderParamsType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderOrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderOrderDetailsType.OrderType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests.Authentication;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.BankPubKeyDigests.Encryption;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType.Product;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.ebics.certificate.KeyUtil;
import com.axelor.apps.bankpayment.ebics.client.EbicsSession;
import com.axelor.apps.bankpayment.ebics.client.OrderAttribute;
import com.axelor.apps.base.AxelorException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * The <code>DInitializationRequestElement</code> is the common initialization for all ebics
 * downloads.
 *
 * @author Hachani
 */
public class DInitializationRequestElement extends InitializationRequestElement {

  /**
   * Constructs a new <code>DInitializationRequestElement</code> for downloads initializations.
   *
   * @param session the current ebics session
   * @param type the download order type (FDL, HTD, HPD)
   * @param startRange the start range download
   * @param endRange the end range download
   * @throws EbicsException
   */
  public DInitializationRequestElement(
      EbicsSession session,
      com.axelor.apps.bankpayment.ebics.client.OrderType type,
      Date startRange,
      Date endRange)
      throws AxelorException {
    super(session, type, generateName(type));
    this.startRange = startRange;
    this.endRange = endRange;
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
    OrderType orderType;
    StaticHeaderOrderDetailsType orderDetails;

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

    EbicsUser ebicsUser = session.getUser();
    EbicsPartner ebicsPartner = ebicsUser.getEbicsPartner();

    OrderAttribute orderAttribute = new OrderAttribute(type, ebicsPartner.getEbicsTypeSelect());
    orderAttribute.build();

    if (type.equals(com.axelor.apps.bankpayment.ebics.client.OrderType.FDL)) {
      FDLOrderParamsType fDLOrderParamsType;
      FileFormatType fileFormat;

      fileFormat =
          EbicsXmlFactory.createFileFormatType(
              Locale.FRANCE.getCountry().toUpperCase(), session.getSessionParam("FORMAT"));
      fDLOrderParamsType = EbicsXmlFactory.createFDLOrderParamsType(fileFormat);

      if (startRange != null && endRange != null) {
        DateRange range;
        range = EbicsXmlFactory.createDateRange(startRange, endRange);
        fDLOrderParamsType.setDateRange(range);
      }

      if (Boolean.getBoolean(session.getSessionParam("TEST"))) {
        Parameter parameter;
        Value value;

        value = EbicsXmlFactory.createValue("String", "TRUE");
        parameter = EbicsXmlFactory.createParameter("TEST", value);
        fDLOrderParamsType.setParameterArray(new Parameter[] {parameter});
      }
      orderDetails =
          EbicsXmlFactory.createStaticHeaderOrderDetailsType(
              ebicsUser.getNextOrderId(),
              orderAttribute.getOrderAttributes(),
              orderType,
              fDLOrderParamsType);
    } else {
      StandardOrderParamsType standardOrderParamsType;

      standardOrderParamsType = EbicsXmlFactory.createStandardOrderParamsType();
      // FIXME Some banks cannot handle OrderID element in download process. Add parameter in
      // configuration!!!
      orderDetails =
          EbicsXmlFactory.createStaticHeaderOrderDetailsType(
              ebicsUser.getNextOrderId(), // session.getUser().getPartner().nextOrderId(),
              orderAttribute.getOrderAttributes(),
              orderType,
              standardOrderParamsType);
    }
    xstatic =
        EbicsXmlFactory.createStaticHeaderType(
            session.getBankID(),
            nonce,
            ebicsPartner.getPartnerId(),
            product,
            ebicsUser.getSecurityMedium(),
            ebicsUser.getUserId(),
            Calendar.getInstance(),
            orderDetails,
            bankPubKeyDigests);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    body = EbicsXmlFactory.createEbicsRequestBody();
    request = EbicsXmlFactory.createEbicsRequest(1, "H003", header, body);
    document = EbicsXmlFactory.createEbicsRequestDocument(request);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private Date startRange;
  private Date endRange;
}
