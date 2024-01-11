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
package com.axelor.apps.bankpayment.ebics.client;

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

import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.EmptyMutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.OrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.ProductElementType;
import com.axelor.apps.account.ebics.schema.h003.UnsecuredRequestStaticHeaderType;
import com.axelor.apps.bankpayment.ebics.xml.DefaultEbicsRootElement;
import com.axelor.apps.bankpayment.ebics.xml.EbicsXmlFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import java.util.Optional;

/**
 * The <code>UnsecuredRequestElement</code> is the common element used for key management requests.
 *
 * @author hachani
 */
public class UnsecuredRequestElement extends DefaultEbicsRootElement {

  /**
   * Constructs a Unsecured Request Element.
   *
   * @param session the ebics session.
   * @param orderType the order type (INI | HIA).
   * @param orderId the order id, if null a random one is generated.
   */
  public UnsecuredRequestElement(EbicsSession session, OrderType orderType, byte[] orderData) {
    super(session);
    this.orderType = orderType;
    this.orderData = orderData;
  }

  @Override
  public void build() throws AxelorException {
    Header header;
    Body body;
    EmptyMutableHeaderType mutable;
    UnsecuredRequestStaticHeaderType xstatic;
    ProductElementType productType;
    OrderDetailsType orderDetails;
    DataTransfer dataTransfer;
    OrderData orderData;
    EbicsUnsecuredRequest request;

    orderDetails =
        EbicsXmlFactory.createOrderDetailsType(
            "DZNNN", session.getUser().getNextOrderId(), orderType.getOrderType());

    productType =
        EbicsXmlFactory.creatProductElementType(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getLanguage).orElse(null),
            session.getProduct().getName());

    try {
      xstatic =
          EbicsXmlFactory.createUnsecuredRequestStaticHeaderType(
              session.getBankID(),
              session.getUser().getEbicsPartner().getPartnerId(),
              session.getUser().getUserId(),
              productType,
              orderDetails,
              session.getUser().getSecurityMedium());
      mutable = EbicsXmlFactory.createEmptyMutableHeaderType();

      header = EbicsXmlFactory.createHeader(true, mutable, xstatic);

      orderData = EbicsXmlFactory.createOrderData(this.orderData);
      dataTransfer = EbicsXmlFactory.createDataTransfer(orderData);
      body = EbicsXmlFactory.createBody(dataTransfer);
      request = EbicsXmlFactory.createEbicsUnsecuredRequest(header, body, 1, "H003");

      document = EbicsXmlFactory.createEbicsUnsecuredRequestDocument(request);

    } catch (AxelorException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "UnsecuredRequest.xml";
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private OrderType orderType;
  private byte[] orderData;
}
