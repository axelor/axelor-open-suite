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

import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.ebics.client.OrderType;
import com.axelor.apps.bankpayment.ebics.exception.ReturnCode;
import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.apps.base.AxelorException;

/**
 * The <code>DInitializationResponseElement</code> is the response element for ebics downloads
 * initializations.
 *
 * @author Hachani
 */
public class DInitializationResponseElement extends InitializationResponseElement {

  /**
   * Constructs a new <code>DInitializationResponseElement</code> object
   *
   * @param factory the content factory
   * @param orderType the order type
   * @param name the element name
   */
  public DInitializationResponseElement(
      ContentFactory factory, OrderType orderType, String name, EbicsUser ebicsUser) {
    super(factory, orderType, name, ebicsUser);
  }

  @Override
  public void build() throws AxelorException {
    String bodyRetCode;

    super.build();
    if (!returnCode.isOk()) {
      return;
    }
    bodyRetCode = response.getBody().getReturnCode().getStringValue();
    returnCode = ReturnCode.toReturnCode(bodyRetCode, "");
    numSegments = (int) response.getHeader().getStatic().getNumSegments();
    if (numSegments > 0) {
      segmentNumber = (int) response.getHeader().getMutable().getSegmentNumber().getLongValue();
      lastSegment = response.getHeader().getMutable().getSegmentNumber().getLastSegment();
      transactionKey =
          response.getBody().getDataTransfer().getDataEncryptionInfo().getTransactionKey();
      orderData = response.getBody().getDataTransfer().getOrderData().getByteArrayValue();
    }
  }

  /**
   * Returns the total segments number.
   *
   * @return the total segments number.
   */
  public int getSegmentsNumber() {
    return numSegments;
  }

  /**
   * Returns The current segment number.
   *
   * @return the segment number.
   */
  public int getSegmentNumber() {
    return segmentNumber;
  }

  /**
   * Checks if it is the last segment.
   *
   * @return True is it is the last segment.
   */
  public boolean isLastSegment() {
    return lastSegment;
  }

  /**
   * Returns the transaction key.
   *
   * @return the transaction key.
   */
  public byte[] getTransactionKey() {
    return transactionKey;
  }

  /**
   * Returns the order data.
   *
   * @return the order data.
   */
  public byte[] getOrderData() {
    return orderData;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private int numSegments;
  private int segmentNumber;
  private boolean lastSegment;
  private byte[] transactionKey;
  private byte[] orderData;
}
