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

import com.axelor.apps.account.ebics.schema.h003.EbicsResponseDocument;
import com.axelor.apps.account.ebics.schema.h003.EbicsResponseDocument.EbicsResponse;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.ebics.customer.OrderType;
import com.axelor.apps.bankpayment.ebics.exception.ReturnCode;
import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.exception.AxelorException;

/**
 * The <code>InitializationResponseElement</code> is the common element for transfer initialization
 * responses.
 *
 * @author Hachani
 */
public class InitializationResponseElement extends DefaultResponseElement {

  /**
   * Constructs a new <code>InitializationResponseElement</code> element.
   *
   * @param factory the content factory
   * @param orderType the order type
   * @param name the element name
   */
  public InitializationResponseElement(
      ContentFactory factory, OrderType orderType, String name, EbicsUser ebicsUser) {
    super(factory, name, ebicsUser);
    this.orderType = orderType;
  }

  @Override
  public void build() throws AxelorException {
    String code;
    String text;

    parse(factory);
    response = ((EbicsResponseDocument) document).getEbicsResponse();
    code = response.getHeader().getMutable().getReturnCode();
    text = response.getHeader().getMutable().getReportText();
    returnCode = ReturnCode.toReturnCode(code, text);
    transactionId = response.getHeader().getStatic().getTransactionID();
  }

  /**
   * Returns the transaction ID.
   *
   * @return the transaction ID.
   */
  public byte[] getTransactionId() {
    return transactionId;
  }

  /**
   * Returns the order type.
   *
   * @return the order type.
   */
  public String getOrderType() {
    return orderType.getOrderType();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  protected EbicsResponse response;
  private OrderType orderType;
  private byte[] transactionId;
}
