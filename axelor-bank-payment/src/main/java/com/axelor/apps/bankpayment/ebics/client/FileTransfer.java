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

import com.axelor.apps.bankpayment.db.EbicsTransferState;
import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.apps.bankpayment.ebics.io.ByteArrayContentFactory;
import com.axelor.apps.bankpayment.ebics.io.Joiner;
import com.axelor.apps.bankpayment.ebics.xml.DInitializationRequestElement;
import com.axelor.apps.bankpayment.ebics.xml.DInitializationResponseElement;
import com.axelor.apps.bankpayment.ebics.xml.DTransferRequestElement;
import com.axelor.apps.bankpayment.ebics.xml.DTransferResponseElement;
import com.axelor.apps.bankpayment.ebics.xml.DefaultEbicsRootElement;
import com.axelor.apps.bankpayment.ebics.xml.InitializationResponseElement;
import com.axelor.apps.bankpayment.ebics.xml.ReceiptRequestElement;
import com.axelor.apps.bankpayment.ebics.xml.ReceiptResponseElement;
import com.axelor.apps.bankpayment.ebics.xml.TransferResponseElement;
import com.axelor.apps.bankpayment.ebics.xml.UInitializationRequestElement;
import com.axelor.apps.bankpayment.ebics.xml.UTransferRequestElement;
import com.axelor.apps.base.AxelorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import org.jdom.JDOMException;

/**
 * Handling of file transfers. Files can be transferred to and fetched from the bank. Every transfer
 * may be performed in a recoverable way. For convenience and performance reasons there are also
 * methods that do the whole transfer in one method call. To use the recoverable transfer mode, you
 * may set a working directory for temporarily created files.
 *
 * <p>EBICS specification 2.4.2 - 6.2 Encryption at application level
 *
 * <p>In the event of an upload transaction, a random symmetrical key is generated in the customer
 * system that is used exclusively within the framework of this transaction both for encryption of
 * the ES’s and for encryption of the order data. This key is encrypted asymmetrically with the
 * financial institution’s public encryption key and is transmitted by the customer system to the
 * bank system during the initialization phase of the transaction.
 *
 * <p>Analogously, in the case of a download transaction a random symmetrical key is generated in
 * the bank system that is used for encryption of the order data that is to be downloaded and for
 * encryption of the bank-technical signature that has been provided by the financial institution.
 * This key is asymmetrically encrypted and is transmitted by the bank system to the customer system
 * during the initialization phase of the transaction. The asymmetrical encryption takes place with
 * the technical subscriber’s public encryption key if the transaction’s EBICS messages are sent by
 * a technical subscriber. Otherwise the asymmetrical encryption takes place with the public
 * encryption key of the non-technical subscriber, i.e. the submitter of the order.
 *
 * @author Hachani
 */
public class FileTransfer {

  /**
   * Constructs a new FileTransfer session
   *
   * @param session the user session
   */
  public FileTransfer(EbicsSession session) {
    this.session = session;
  }

  /**
   * Initiates a file transfer to the bank.
   *
   * @param content The bytes you want to send.
   * @param orderType As which order type
   * @throws IOException
   * @throws EbicsException
   */
  public void sendFile(byte[] content, OrderType orderType, byte[] signature)
      throws IOException, AxelorException {
    HttpRequestSender sender;
    UInitializationRequestElement initializer;
    InitializationResponseElement response;
    int httpCode;
    EbicsTransferState state;

    sender = new HttpRequestSender(session);

    initializer = new UInitializationRequestElement(session, orderType, content, signature);

    initializer.build();

    initializer.validate();
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    try {
      initializer.save(bout);
    } catch (JDOMException e) {
      // TODO Bloc catch généré automatiquement
      e.printStackTrace();
    }
    System.out.println(
        "Requete ----------------------------------------------------------------------------");
    System.out.println(bout.toString());

    httpCode = sender.send(new ByteArrayContentFactory(initializer.prettyPrint()));

    EbicsUtils.checkHttpCode(httpCode);
    response =
        new InitializationResponseElement(
            sender.getResponseBody(),
            orderType,
            DefaultEbicsRootElement.generateName(orderType),
            session.getUser());
    response.build();
    response.report(new EbicsRootElement[] {initializer, response});
    state = new EbicsTransferState();
    state.setNumSegments(initializer.getSegmentNumber());
    state.setTransactionId(response.getTransactionId());
    int segNumber = 1;
    while (segNumber <= state.getNumSegments()) {
      state.setSegmentNumber(segNumber);
      if (segNumber == state.getSegmentNumber()) {
        state.setLastSegment(true);
      }
      sendFile(
          initializer.getContent(segNumber),
          segNumber,
          state.getLastSegment(),
          state.getTransactionId(),
          orderType);
      segNumber++;
    }
  }

  /**
   * Sends a segment to the ebics bank server.
   *
   * @param factory the content factory that contain the segment data.
   * @param segmentNumber the segment number
   * @param lastSegment is it the last segment?
   * @param transactionId the transaction Id
   * @param orderType the order type
   * @throws IOException
   * @throws EbicsException
   */
  public void sendFile(
      ContentFactory factory,
      int segmentNumber,
      boolean lastSegment,
      byte[] transactionId,
      OrderType orderType)
      throws IOException, AxelorException {
    UTransferRequestElement uploader;
    HttpRequestSender sender;
    TransferResponseElement response;
    int httpCode;

    uploader =
        new UTransferRequestElement(
            session, orderType, segmentNumber, lastSegment, transactionId, factory);
    sender = new HttpRequestSender(session);
    uploader.build();
    uploader.validate();
    httpCode = sender.send(new ByteArrayContentFactory(uploader.prettyPrint()));
    EbicsUtils.checkHttpCode(httpCode);
    response =
        new TransferResponseElement(
            sender.getResponseBody(),
            DefaultEbicsRootElement.generateName(orderType),
            session.getUser());
    response.build();
    response.report(new EbicsRootElement[] {uploader, response});
  }

  /**
   * Fetches a file of the given order type from the bank. You may give an optional start and end
   * date. This type of transfer will run until everything is processed. No transaction recovery is
   * possible.
   *
   * @param orderType type of file to fetch
   * @param start optional begin of fetch term
   * @param end optional end of fetch term
   * @param dest where to put the data
   * @throws IOException communication error
   * @throws EbicsException server generated error
   */
  public void fetchFile(OrderType orderType, Date start, Date end, OutputStream dest)
      throws IOException, AxelorException {
    HttpRequestSender sender;
    DInitializationRequestElement initializer;
    DInitializationResponseElement response;
    ReceiptRequestElement receipt;
    ReceiptResponseElement receiptResponse;
    int httpCode;
    EbicsTransferState state;
    Joiner joiner;

    sender = new HttpRequestSender(session);
    initializer = new DInitializationRequestElement(session, orderType, start, end);
    initializer.build();
    initializer.validate();
    httpCode = sender.send(new ByteArrayContentFactory(initializer.prettyPrint()));
    EbicsUtils.checkHttpCode(httpCode);
    response =
        new DInitializationResponseElement(
            sender.getResponseBody(),
            orderType,
            DefaultEbicsRootElement.generateName(orderType),
            session.getUser());
    response.build();
    response.report(new EbicsRootElement[] {initializer, response});
    state = new EbicsTransferState();
    state.setSegmentNumber(response.getSegmentsNumber());
    state.setTransactionId(response.getTransactionId());
    state.setSegmentNumber(response.getSegmentNumber());
    joiner = new Joiner(session.getUser());
    joiner.append(response.getOrderData());
    while (state.getSegmentNumber() <= state.getNumSegments()) {
      if (state.getSegmentNumber() == state.getNumSegments()) {
        state.setLastSegment(true);
      }
      fetchFile(
          orderType,
          state.getSegmentNumber(),
          state.getLastSegment(),
          state.getTransactionId(),
          joiner);
      state.setSegmentNumber(state.getSegmentNumber() + 1);
    }

    joiner.writeTo(dest, response.getTransactionKey());
    receipt =
        new ReceiptRequestElement(
            session, state.getTransactionId(), DefaultEbicsRootElement.generateName(orderType));
    receipt.build();
    receipt.validate();
    httpCode = sender.send(new ByteArrayContentFactory(receipt.prettyPrint()));
    EbicsUtils.checkHttpCode(httpCode);
    receiptResponse =
        new ReceiptResponseElement(
            sender.getResponseBody(),
            DefaultEbicsRootElement.generateName(orderType),
            session.getUser());
    receiptResponse.build();
    receiptResponse.report(new EbicsRootElement[] {receipt, receiptResponse});
  }

  /**
   * Fetches a given portion of a file.
   *
   * @param orderType the order type
   * @param segmentNumber the segment number
   * @param lastSegment is it the last segment?
   * @param transactionId the transaction ID
   * @param joiner the portions joiner
   * @throws IOException communication error
   * @throws EbicsException server generated error
   */
  public void fetchFile(
      OrderType orderType,
      int segmentNumber,
      boolean lastSegment,
      byte[] transactionId,
      Joiner joiner)
      throws IOException, AxelorException {
    DTransferRequestElement downloader;
    HttpRequestSender sender;
    DTransferResponseElement response;
    int httpCode;

    sender = new HttpRequestSender(session);
    downloader =
        new DTransferRequestElement(session, orderType, segmentNumber, lastSegment, transactionId);
    downloader.build();
    downloader.validate();
    httpCode = sender.send(new ByteArrayContentFactory(downloader.prettyPrint()));
    EbicsUtils.checkHttpCode(httpCode);
    response =
        new DTransferResponseElement(
            sender.getResponseBody(),
            orderType,
            DefaultEbicsRootElement.generateName(orderType),
            session.getUser());
    response.build();
    response.report(new EbicsRootElement[] {downloader, response});
    joiner.append(response.getOrderData());
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private EbicsSession session;
}
