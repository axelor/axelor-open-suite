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

import com.axelor.apps.account.ebics.client.OrderType;
import com.axelor.apps.account.ebics.exception.ReturnCode;
import com.axelor.apps.account.ebics.interfaces.ContentFactory;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

/**
 * The <code>DInitializationResponseElement</code> is the response element
 * for ebics downloads initializations.
 *
 * @author Hachani
 *
 */
public class DInitializationResponseElement extends InitializationResponseElement {

  /**
   * Constructs a new <code>DInitializationResponseElement</code> object
   * @param factory the content factory
   * @param orderType the order type
   * @param name the element name
   */
  public DInitializationResponseElement(ContentFactory factory,
                                        OrderType orderType,
                                        String name)
  {
    super(factory, orderType, name);
  }

  @Override
  public void build() throws AxelorException {
    String	bodyRetCode;

    super.build();
    bodyRetCode = response.getBody().getReturnCode().getStringValue();
    returnCode = ReturnCode.toReturnCode(bodyRetCode, "");
    if (returnCode.equals(ReturnCode.EBICS_NO_DOWNLOAD_DATA_AVAILABLE)) {
      throw new AxelorException(ReturnCode.EBICS_NO_DOWNLOAD_DATA_AVAILABLE.getText(), IException.FUNCTIONNAL);
    }
    numSegments = (int)response.getHeader().getStatic().getNumSegments();
    segmentNumber = (int)response.getHeader().getMutable().getSegmentNumber().getLongValue();
    lastSegment = response.getHeader().getMutable().getSegmentNumber().getLastSegment();
    transactionKey = response.getBody().getDataTransfer().getDataEncryptionInfo().getTransactionKey();
    orderData = response.getBody().getDataTransfer().getOrderData().getByteArrayValue();
  }

  /**
   * Returns the total segments number.
   * @return the total segments number.
   */
  public int getSegmentsNumber() {
    return numSegments;
  }

  /**
   * Returns The current segment number.
   * @return the segment number.
   */
  public int getSegmentNumber() {
    return segmentNumber;
  }

  /**
   * Checks if it is the last segment.
   * @return True is it is the last segment.
   */
  public boolean isLastSegment() {
    return lastSegment;
  }

  /**
   * Returns the transaction key.
   * @return the transaction key.
   */
  public byte[] getTransactionKey() {
    return transactionKey;
  }

  /**
   * Returns the order data.
   * @return the order data.
   */
  public byte[] getOrderData() {
    return orderData;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private int				numSegments;
  private int				segmentNumber;
  private boolean			lastSegment;
  private byte[]			transactionKey;
  private byte[]			orderData;
  private static final long 		serialVersionUID = -6013011772863903840L;
}
