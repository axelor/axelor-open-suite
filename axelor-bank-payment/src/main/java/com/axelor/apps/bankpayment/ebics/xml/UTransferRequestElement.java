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

import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType;
import com.axelor.apps.account.ebics.schema.h003.DataTransferRequestType.OrderData;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsRequestDocument.EbicsRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.MutableHeaderType.SegmentNumber;
import com.axelor.apps.account.ebics.schema.h003.StaticHeaderType;
import com.axelor.apps.bankpayment.ebics.client.EbicsSession;
import com.axelor.apps.bankpayment.ebics.client.OrderType;
import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.apps.bankpayment.ebics.io.IOUtils;
import com.axelor.exception.AxelorException;
import java.io.ByteArrayOutputStream;
import org.jdom.JDOMException;

/**
 * The <code>UTransferRequestElement</code> is the root element for all ebics upload transfers.
 *
 * @author Hachani
 */
public class UTransferRequestElement extends TransferRequestElement {

  /**
   * Constructs a new <code>UTransferRequestElement</code> for ebics upload transfer.
   *
   * @param session the current ebics session
   * @param orderType the upload order type
   * @param segmentNumber the segment number
   * @param lastSegment i it the last segment?
   * @param transactionId the transaction ID
   * @param content the content factory
   */
  public UTransferRequestElement(
      EbicsSession session,
      OrderType orderType,
      int segmentNumber,
      boolean lastSegment,
      byte[] transactionId,
      ContentFactory content) {
    super(session, generateName(orderType), orderType, segmentNumber, lastSegment, transactionId);
    this.content = content;
  }

  @Override
  public void buildTransfer() throws AxelorException {
    EbicsRequest request;
    Header header;
    Body body;
    MutableHeaderType mutable;
    SegmentNumber segmentNumber;
    StaticHeaderType xstatic;
    OrderData orderData;
    DataTransferRequestType dataTransfer;

    segmentNumber = EbicsXmlFactory.createSegmentNumber(this.segmentNumber, lastSegment);
    mutable = EbicsXmlFactory.createMutableHeaderType("Transfer", segmentNumber);
    xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(), transactionId);
    header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
    orderData = EbicsXmlFactory.createEbicsRequestOrderData(IOUtils.getFactoryContent(content));
    dataTransfer = EbicsXmlFactory.createDataTransferRequestType(orderData);
    body = EbicsXmlFactory.createEbicsRequestBody(dataTransfer); // TODO CHECK
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
        "Requete data ----------------------------------------------------------------------------");
    System.out.println(bout.toString());
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private ContentFactory content;
}
