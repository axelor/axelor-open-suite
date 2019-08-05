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

import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType.SegmentNumber;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType;
import com.axelor.apps.bankpayment.ebics.customer.EbicsSession;
import com.axelor.apps.bankpayment.ebics.customer.OrderType;
import com.axelor.exception.AxelorException;

/**
 * The <code>DTransferRequestElement</code> is the common elements for all ebics downloads.
 *
 * @author Hachani
 */
public class DTransferRequestElement extends TransferRequestElement {

  /**
   * Constructs a new <code>DTransferRequestElement</code> element.
   *
   * @param session the current ebics session
   * @param type the order type
   * @param segmentNumber the segment number
   * @param lastSegment is it the last segment?
   * @param transactionId the transaction ID
   */
  public DTransferRequestElement(
      EbicsSession session,
      OrderType type,
      int segmentNumber,
      boolean lastSegment,
      byte[] transactionId) {
    super(session, generateName(type), type, segmentNumber, lastSegment, transactionId);
  }

  @Override
  public void buildTransfer() throws AxelorException {
    EbicsRequest request;
    Header header;
    Body body;
    MutableHeaderType mutable;
    SegmentNumber segmentNumber;
    StaticHeaderType xstatic;

    segmentNumber = EbicsXmlFactory.createSegmentNumber(this.segmentNumber, lastSegment);
    mutable = EbicsXmlFactory.createMutableHeaderType("Transfer", segmentNumber);
    xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(), transactionId);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    body = EbicsXmlFactory.createEbicsRequestBody();
    request = EbicsXmlFactory.createEbicsRequest(1, "H003", header, body);
    document = EbicsXmlFactory.createEbicsRequestDocument(request);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

}
