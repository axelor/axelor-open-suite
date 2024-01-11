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

import com.axelor.apps.bankpayment.ebics.certificate.KeyStoreManager;
import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.apps.bankpayment.ebics.io.ByteArrayContentFactory;
import com.axelor.apps.bankpayment.ebics.service.EbicsUserService;
import com.axelor.apps.bankpayment.ebics.xml.HIARequestElement;
import com.axelor.apps.bankpayment.ebics.xml.HPBRequestElement;
import com.axelor.apps.bankpayment.ebics.xml.HPBResponseOrderDataElement;
import com.axelor.apps.bankpayment.ebics.xml.INIRequestElement;
import com.axelor.apps.bankpayment.ebics.xml.KeyManagementResponseElement;
import com.axelor.apps.bankpayment.ebics.xml.SPRRequestElement;
import com.axelor.apps.bankpayment.ebics.xml.SPRResponseElement;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Everything that has to do with key handling. If you have a totally new account use <code>
 * sendINI()</code> and <code>sendHIA()</code> to send you newly created keys to the bank. Then wait
 * until the bank activated your keys. If you are migrating from FTAM. Just send HPB, your EBICS
 * account should be usable without delay.
 *
 * @author Hachani
 */
public class KeyManagement {

  /**
   * Constructs a new <code>KeyManagement</code> instance with a given ebics session
   *
   * @param session the ebics session
   */
  public KeyManagement(EbicsSession session) {
    this.session = session;
  }

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  /**
   * Sends the user's signature key (A005) to the bank. After successful operation the user is in
   * state "initialized".
   *
   * @param orderId the order ID. Let it null to generate a random one.
   * @throws EbicsException server generated error message
   * @throws IOException communication error
   * @throws AxelorException
   * @throws JDOMException
   */
  public void sendINI() throws IOException, AxelorException, JDOMException {
    INIRequestElement request;
    KeyManagementResponseElement response;
    HttpRequestSender sender;
    int httpCode;
    sender = new HttpRequestSender(session);
    log.debug("HttpRequestSender OK");
    request = new INIRequestElement(session);
    log.debug("INIRequestElement OK");
    request.build();
    log.debug("build OK");
    request.validate();
    log.debug("validate OK");
    // session.getConfiguration().getTraceManager().trace(request);
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    log.debug("send OK");
    EbicsUtils.checkHttpCode(httpCode);
    log.debug("checkHttpCode OK");
    response =
        new KeyManagementResponseElement(
            sender.getResponseBody(), "INIResponse", session.getUser());
    log.debug("KeyManagementResponseElement OK");
    response.build();
    log.debug("build OK");
    // session.getConfiguration().getTraceManager().trace(response);
    response.report(new EbicsRootElement[] {request, response});
    log.debug("report OK");
  }

  /**
   * Sends the public part of the protocol keys to the bank.
   *
   * @param orderId the order ID. Let it null to generate a random one.
   * @throws IOException communication error
   * @throws JDOMException
   * @throws EbicsException server generated error message
   */
  public void sendHIA() throws IOException, AxelorException, JDOMException {
    HIARequestElement request;
    KeyManagementResponseElement response;
    HttpRequestSender sender;
    int httpCode;

    sender = new HttpRequestSender(session);
    request = new HIARequestElement(session);
    request.build();
    request.validate();
    //    session.getConfiguration().getTraceManager().trace(request);
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    EbicsUtils.checkHttpCode(httpCode);
    response =
        new KeyManagementResponseElement(
            sender.getResponseBody(), "HIAResponse", session.getUser());
    response.build();
    //    session.getConfiguration().getTraceManager().trace(response);
    response.report(new EbicsRootElement[] {request, response});
  }

  /**
   * Sends encryption and authentication keys to the bank. This order is only allowed for a new user
   * at the bank side that has been created by copying the A005 key. The keys will be activated
   * immediately after successful completion of the transfer.
   *
   * @param orderId the order ID. Let it null to generate a random one.
   * @throws IOException communication error
   * @throws GeneralSecurityException data decryption error
   * @throws AxelorException
   * @throws JDOMException
   * @throws EbicsException server generated error message
   */
  public X509Certificate[] sendHPB()
      throws IOException, GeneralSecurityException, AxelorException, JDOMException {

    HPBRequestElement request;
    KeyManagementResponseElement response;
    HttpRequestSender sender;
    HPBResponseOrderDataElement orderData;
    ContentFactory factory;
    int httpCode;

    sender = new HttpRequestSender(session);
    request = new HPBRequestElement(session);
    request.build();
    request.validate();
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    EbicsUtils.checkHttpCode(httpCode);
    response =
        new KeyManagementResponseElement(
            sender.getResponseBody(), "HBPResponse", session.getUser());
    response.build();
    response.report(new EbicsRootElement[] {request, response});
    EbicsUserService userService = Beans.get(EbicsUserService.class);
    factory =
        new ByteArrayContentFactory(
            EbicsUtils.unzip(
                userService.decrypt(
                    session.getUser(), response.getOrderData(), response.getTransactionKey())));
    orderData = new HPBResponseOrderDataElement(factory, session.getUser());
    orderData.build();

    return createCertificates(orderData);
  }

  private X509Certificate[] createCertificates(HPBResponseOrderDataElement orderData)
      throws AxelorException, GeneralSecurityException, IOException {

    KeyStoreManager keystoreManager = new KeyStoreManager();
    if (session.getUser().getPassword() != null) {
      keystoreManager.load("", session.getUser().getPassword().toCharArray());
    } else {
      keystoreManager.load("", null);
    }

    String certId = session.getBankID() + "-E002";
    keystoreManager.setCertificateEntry(
        certId, new ByteArrayInputStream(orderData.getBankE002Certificate()));
    X509Certificate certificateE002 = keystoreManager.getCertificate(certId);

    certId = session.getBankID() + "-X002";
    keystoreManager.setCertificateEntry(
        certId, new ByteArrayInputStream(orderData.getBankX002Certificate()));
    X509Certificate certificateX002 = keystoreManager.getCertificate(certId);

    return new X509Certificate[] {certificateE002, certificateX002};
  }

  /**
   * Sends the SPR order to the bank. After that you have to start over with sending INI and HIA.
   *
   * @throws IOException Communication exception
   * @throws AxelorException
   * @throws JDOMException
   * @throws EbicsException Error message generated by the bank.
   */
  public void lockAccess() throws IOException, AxelorException, JDOMException {
    HttpRequestSender sender;
    SPRRequestElement request;
    SPRResponseElement response;
    int httpCode;

    sender = new HttpRequestSender(session);
    request = new SPRRequestElement(session);
    request.build();
    request.validate();
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    EbicsUtils.checkHttpCode(httpCode);
    response = new SPRResponseElement(sender.getResponseBody(), session.getUser());
    response.build();
    response.report(new EbicsRootElement[] {request, response});
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private EbicsSession session;
}
